# Error Response 서버로 전달하기

[Spring Guide - Exception 전략](https://cheese10yun.github.io/spring-guide-exception/) 이전에 API Server에 Exception handling에 대해서 정리한 적 있습니다. 이번 포스팅에서는 여러 서버를 호출해서 예외가 발생하는 경우 Exception handling에 대한 포팅입니다.

더 A API -> B API -> C API 호출을 진행하는 경우 C API에서 아래와 같은 예외가 발생하는 경우 A API에게 그대로 전달해야 하는 경우가 있습니다.

```json
{
  "message": " Invalid Input Value",
  "status": 400,
  "errors": [
    {
      "field": "name.last",
      "value": "",
      "reason": "must not be empty"
    },
    {
      "field": "name.first",
      "value": "",
      "reason": "must not be empty"
    }
  ],
  "code": "C001"
}
```

실제 코드 레벨에서 더 단순하게 구성해서 정리해보겠습니다.

## B Service에서 문제 발생

```kotlin
@RestController
@RequestMapping("/a-service")
class AServiceApi(
    private val userRegistrationService: UserRegistrationService
) {

    @PostMapping
    fun aService(@RequestBody dto: UserRegistrationRequest) =
        userRegistrationService.register(dto)
}

@Service
class UserRegistrationService(
    private val objectMapper: ObjectMapper
) {
    fun register(dto: UserRegistrationRequest) {
        val response = UserClient()
            .postUser(dto.name, dto.email)
            .run {
                Pair(
                    second.isSuccessful,
                    when {
                        second.isSuccessful -> null
                        else -> {
                            objectMapper.readValue(
                                second.body().toByteArray(),
                                ErrorResponse::class.java
                            )
                        }
                    }
                )
            }
        
        // 등록이 실패하는 경우는 어떻게 처리해야할까?
        // 서비스 레이어에서 b-service에서 발생한 예외를 어떻게 그대로 전달 할 수 있을까?
    }
}

class UserClient(
    private val host: String = "http://localhost:8080"
) {

    // 일반적으로는 다른 서버를 호출하지만 본 예제에서는 단순하게 구성하기 위해 자기 자신을 호출하게 진행
    fun postUser(name: String, email: String) =
        "$host/b-service"
            .httpPost()
            .header(Headers.CONTENT_TYPE, "application/json")
            .jsonBody(
                """
                    {
                      "name": "$name",
                      "email": "$email"
                    }
                    """.trimIndent()
            )
            .response()
}
```
a-service -> b-service 호출을 한다고 가정했을 경우 b-service에서 HTTP Status가 2xx가 아닌 경우, 특히 4xx가 발생하는 경우 요청한 여려의 필드 중에 1개 이상의 오류가 있는 경우가 빈번하게 있기 때문에 b-service의 Error 응답을 그대로 전달해 줘야 하는 경우가 있습니다.

그런데 문제가 있습니다. 서비스 로직에서 b-service에서 발생한 Error 응답을 그대로 클라이언트에게 전달할 수 있을까요? [Spring Guide - Exception 전략](https://cheese10yun.github.io/spring-guide-exception/)에서 사용한 `@ControllerAdvice`을 통해서 예외를 핸들링을 진행해 보겠습니다.

## 발생한 Error 응답을 그대로 전달 하기

### 예외 클래스 정의
```
class ApiException(
    val errorResponse: ErrorResponse
) : ServiceException(ErrorCode.SERVICE_ERROR, ErrorCode.SERVICE_ERROR.message) // Error Code가 실질적으로 진행하는 것은 없지만 필수 값이라 전달
```

비즈니스 예외를 관리하는 예외 클래스를 정의하고, Error JSON에 맞는 Error Response 객체를 필수 값으로 받게 합니다.

```kotlin
fun register(dto: UserRegistrationRequest) {
      val response = UserClient()
          .postUser(dto.name, dto.email)
          .run {
              Pair(
                  second.isSuccessful,
                  when {
                      second.isSuccessful -> null
                      else -> {
                          objectMapper.readValue(
                              second.body().toByteArray(),
                              ErrorResponse::class.java
                          )
                      }
                  }
              )
          }

      // 해당 코드 추가
      if (response.first.not()) {
          throw ApiException(response.second!!)
      }
  }
```

응답이 2xx가 아닌 경우 b-service에서 전달받은 ErrorResponse 객체를 예외에 전달하는 예외 코드를 추가합니다.


```kotlin
@ControllerAdvice
class GlobalExceptionHandler {
    ...
    @ExceptionHandler(ApiException::class)
    fun handleApiException(e: ApiException): ResponseEntity<ErrorResponse> {
        return ResponseEntity<ErrorResponse>(e.errorResponse, HttpStatus.valueOf(e.errorResponse.status))
    }
}
```
예외 클래스에서 전달받은 ErrorResponse를 그대로 사용하여 예외 응답을 내려주게 핸들링하는 코드를 추가합니다.

## 테스트

```
# 요청
POST http://localhost:8080/a-service
Content-Type: application/json

{
  "name": "",
  "email": "asd"
}

# 응답
http://localhost:8080/a-service

HTTP/1.1 400 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sat, 22 Jan 2022 17:51:08 GMT
Connection: close

{
  "message": " Invalid Input Value",
  "status": 400,
  "errors": [
    {
      "field": "email",
      "value": "asd",
      "reason": "올바른 형식의 이메일 주소여야 합니다"
    },
    {
      "field": "name",
      "value": "",
      "reason": "비어 있을 수 없습니다"
    }
  ],
  "code": "C001"
}
```
a-service -> b-service를 호출했고 요청 필드에 문제가 있어 b-service에서 내려준 응답을 그대로 a-service에서 내려주는 것을 디버깅 모드로 확인해 보겠습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/msa-error-response/img/error-1.png)

서비스 코드에서 400 응답을 받는 것을 확인할 수 있습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/msa-error-response/img/error-2.png)


ApiException에 대한 에러 핸들링이 GlobalExceptionHandler에서 정상적으로 동작하는 것을 확인할 수 있습니다. 전달받은 Error Response 객체도 정확하게 바인딩 됐습니다.

## 정리

여러 API를 호출하여 요구사항을 만족시키는 경우가 일반적입니다. 그러한 경우 Error Respone를 최초 호출한 클라이언트에게 전달해야 하는 경우가 있으며 이런 경우 위 같은 형식으로 해당 기능을 만족시킬 수 있습니다. 하지만 서버에 대한 통제권이 없고 Error Response에 대한 통일이 없다면 해당 방법은 어려운 부분이 있습니다. 또 Error Response에 대한 내용을 그대로 전달하는 것도 보안에 좋지 않기 때문에 회사 내부에서만 사용하는 경우가 아닌 유저향 서비스 시에는 조금 더 신중하게 Erro 응답을 전달해야 합니다.