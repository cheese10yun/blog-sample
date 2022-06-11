# Spring Cloud Gateway Error Handling & Filter

Spring Cloud Gateway를 활용하여 여러 API를 서비싱 하는 경우 해당 API들은 이전에 포스팅한 [Spring Guide - Exception 전략](https://cheese10yun.github.io/spring-guide-exception/)으로 통일된 Error Response를 갖게 할 수 있습니다. 하지만 게이트웨이 내부에서 발생한 예외에 대한 Error Response를 핸들링하지 않게 되는 경우는 통일된 메시지를 갖지 못하게 됩니다. 예를 들어 해당 리소스를 찾을 수 없는 예외의 경우 아래와 같이 응답됩니다.

```
http://localhost:5555/ASDASD

HTTP/1.1 404 Not Found
content-length: 0

<Response body is empty>


Response code: 404 (Not Found); Time: 145ms; Content length: 0 bytes
```

라우팅 되는 서비스 API와는 Error Response 형식이 맞지 않아 문제가 발생할 수 있습니다. 최종적으로는 아래와 같은 형식으로 라우팅하는 서비스 API와 통일된 Error Response를 내려주는 전략에 대한 내용을 정리해 보았습니다.


```
http://localhost:5555/ASDASD

HTTP/1.1 200 OK
transfer-encoding: chunked
Content-Type: application/json

{
  "message": "404 NOT_FOUND",
  "status": 404,
  "errors": [],
  "code": "C001"
}
Response file saved.
```


## GlobalExceptionHandler

게이트웨이 내부에서 발생하는 예외에 대한 전체적인 핸들링을 담당하는 객체입니다.

### ErrorResponse 및 예외 클래스 정의

우선 ErrorResponse 객체 정의 및 예외 클래스를 정의합니다. 해당 내용은 [Spring Guide - Exception 전략](https://cheese10yun.github.io/spring-guide-exception/)과 유사합니다.

```kotlin
class ErrorResponse(
    val message: String,
    val status: Int,
    val errors: List<FieldError> = emptyList(),
    val code: String,
) {

    // Error Code 기반 생성자
    constructor(
        message: String? = null,
        code: ErrorCode
    ) : this(
        message = message ?: code.message,
        status = code.status,
        code = code.code
    )
}

class FieldError(
    val field: String,
    val value: String,
    val reason: String
)

enum class ErrorCode(
    val status: Int,
    val code: String,
    val message: String
) {
    FRAME_WORK_INTERNAL_ERROR(500, "C001", "프레임워크 내부 예외"),
    UNDEFINED_ERROR(500, "C002", "정의하지 않은 예외"),
}

open class BusinessException(
    override val message: String? = null,
    val errorCode: ErrorCode
) : RuntimeException()
```

Error Respones 객체를 서비스 API와 동일하게 만듭니다. 대부분의 객체는 Error Code 기반 생성자 기반 생성자로 만들게 생성되며 BusinessException는 게이트웨이 내부의 서비스 로직에 최상위 Exception으로 해당 객체를 기반으로 내부 예외가 핸들링됩니다. 만약 게이트웨이에 로직이 거의 없고 라우팅만 사용하는 경우에는 정의하지 않아도 무방합니다.

Code C001은 스프링 게이트웨이 내부에서 발생하는 내부 예외, C002는 정의하지 않은 예외들에 대한 코드입니다. 해당 코드들도 서비스 특성에 맞게 설정하면 됩니다.


### 예외 핸들링

Spring Cloud Gateway에서는 `@ControllerAdvice`와 같은 것을 지원해주지 않아 `ErrorWebExceptionHandler`를 구현하는 것으로 직접 만들어야 합니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-msa/docs/images/ErrorWebExceptionHandler.png)


```kotlin
@Component
@Order(-1)// 내부 bean 보다 우선 순위를 높여 해당 빈이 동작하게 설정
class GlobalExceptionHandler(
    private val objectMapper: ObjectMapper
) : ErrorWebExceptionHandler {

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        val response = exchange.response
        response.headers.contentType = MediaType.APPLICATION_JSON

        val errorResponse = when (ex) {
            // Spring Web Server 관련 오류의 경우 Spring 오류 메시지를 사용
            is ResponseStatusException -> ErrorResponse(code = ErrorCode.FRAME_WORK_INTERNAL_ERROR)
            is BusinessException -> {
                response.statusCode = HttpStatus.valueOf(ex.errorCode.status)
                ErrorResponse(code = ex.errorCode)
            }
            // 그외 오류는 ErrorCode.UNDEFINED_ERROR 기반으로 메시지를 사용
            else -> {
                response.statusCode = HttpStatus.valueOf(ErrorCode.UNDEFINED_ERROR.status)
                ErrorResponse(code = ErrorCode.UNDEFINED_ERROR)
            }
        }

        return response.writeWith(
            Jackson2JsonEncoder(objectMapper).encode(
                Mono.just(errorResponse),
                response.bufferFactory(),
                ResolvableType.forInstance(errorResponse),
                MediaType.APPLICATION_JSON,
                Hints.from(Hints.LOG_PREFIX_HINT, exchange.logPrefix)
            )
        )
    }
}
```

해당 객체는 예외가 발생하면 발생한 Exception 인스턴스에 맞게 Jackson 기반으로 Error Response를 생성하여 최종 응답 메시지를 만들어 내려주게 됩니다.

#### ResponseStatusException 핸들링

`ResponseStatusException`는 게이트웨이 내부 예외 중 HTTP와 관련된 예외 객체로 예를 들어 게이트웨이에서 라우팅에 등록하지 않거나 없는 주소로 접근하는 경우, 등록되지 않은 HTTP method로 요청하는 경우 등 HTTP와 관련된 예외의 경우 사용되는 예외 객체입니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-msa/docs/images/404.png)

브레이크 포인트를 걸고 없는 페이지를 호출하면 `ex` 객체에 `ResponseStatusException`는 객체가 있는 것을 확인할 수 있습니다. 해당 객체로 위에서 정의한 ErrorResponse를 생성하여 아래와 같은 형식으로 응답합니다.

```
http://localhost:5555/ASDASD

HTTP/1.1 200 OK
transfer-encoding: chunked
Content-Type: application/json

{
  "message": "404 NOT_FOUND",
  "status": 404,
  "errors": [],
  "code": "C001"
}
Response file saved.
```

message는 `ResponseStatusException`에서 넘겨주는 message를 그대로 사용하고 있습니다. 만약 유저에게 공개되어 있는 API인 경우에는 이런 시스템 내부에서 넘겨주는 메시지를 그대로 사용하는 것보다 내부적으로 정제된 ErrorCode의 message를 사용하여 정제된 메시지가 내려가게 하는 것도 좋은 방법입니다. 프로젝트 특성에 맞게 선택하면 됩니다.


```
http://localhost:5555/actuator

HTTP/1.1 200 OK
transfer-encoding: chunked
Content-Type: application/json

{
  "message": "405 METHOD_NOT_ALLOWED \"Request method 'POST' not supported\"",
  "status": 405,
  "errors": [],
  "code": "C001"
}
```

위 메시지는 정의되지 않은 HTTP method로 호출했을 때 응답입니다.

#### BusinessException

BusinessException는 비즈니스 예외가 발생하는 경우를 핸들링을 진행하기 위한 객체입니다. 예를 들어 게이트웨이에서 인증 관련된 내부로 직을 진행하다 오류가 발생했을 경우 핸들링하기 위한 객체입니다. 우선 모든 요청에 대한 필터를 추가하는 로직을 간단하게 작성해 보겠습니다.

#### Filter

```yml
# application.yml
spring:
    cloud:
        gateway:
            ...
            default-filters:
                -   name: GlobalFilter
                    args:
                        preLogger: false
                        postLogger: true
```

`default-filters`를 등록합니다. `name`에는 해당 클래스의 이름, `args`는 해당 객체에 넘겨줄 arguemnt를 정의합니다. 사용 방법은 아래에서 자세히 설명하겠습니다.

```kotlin
@Component
class GlobalFilter : AbstractGatewayFilterFactory<GlobalFilter.Config>(Config::class.java) {
    
    private val log by logger()

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->

            log.info("preLogger: ${config.preLogger}")
            log.info("postLogger: ${config.postLogger}")

            chain.filter(exchange)
        }
    }

    class Config(
        val preLogger: Boolean,
        val postLogger: Boolean
    )
}
```

`application.yml`에서 정의한 이름인 GlobalFilter으로 클래스명을 지정하고 `args`에서 정의한 Argument를 할당할 클래스인 Config 클래스를 지정하고 해당 필드 이름으로 변수를 지정합니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-msa/docs/images/config.png) 

해당 필터와 Argument가 정상적으로 동작하는 것을 확인할 수 있습니다. 해당 필터에 인증 관련된 비즈니스 로직이 있다고 가정하고 간단하게 샘플 코드를 만들어 만들어 보겠습니다.


```kotlin
class UnauthorizedException(errorCode: ErrorCode) : BusinessException(errorCode = errorCode)

@Component
class GlobalFilter : AbstractGatewayFilterFactory<GlobalFilter.Config>(Config::class.java) {

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            // 인증 관련 로직이 있다고 가정 하고, 인증이 실패하는 경우 라고 가정
            if (true){
                // UNAUTHORIZED_ERROR(401, "C003", "인증에 실패했습니다"), ErrorCode 추가 
                throw UnauthorizedException(ErrorCode.UNAUTHORIZED_ERROR)
            }

            chain.filter(exchange)
        }
    }
}
```

인증 관련된 UnauthorizedException 예외 클래스를 생성합니다. BusinessException 해당 객체를 그대로 사용해도 무방합니다. 해당 예외가 발생했을 경우 정의된 `UNAUTHORIZED_ERROR` 객체를 넘겨주기만 하면 적절한 ErrorResponse가 내려가게 됩니다.

해당 해당 예외가 발생하면 `GlobalExceptionHandler` 객체에서 `BusinessException` 객체로 예외 객체가 할당되어 핸들링됩니다.


![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-msa/docs/images/error-code.png)

`BusinessException`을 상속한 `UnauthorizedException` 객체와 해당 객체에 
`UNAUTHORIZED_ERROR` ErrorCode 값이 할당되어 있는 것을 확인할 수 있습니다. 해당 정보 기반으로 최종적으로 Error Response는 다음과 같이 내려지게 됩니다.

```
http://localhost:5555/a-service/actuator

HTTP/1.1 401 Unauthorized
transfer-encoding: chunked
Content-Type: application/json

{
  "message": "인증에 실패했습니다",
  "status": 401,
  "errors": [],
  "code": "C003"
}
```

## 그 외 오류

```kotlin
val errorResponse = when (ex) {
    // Spring Web Server 관련 오류의 경우 Spring 오류 메시지를 사용
    is ResponseStatusException -> ErrorResponse(code = ErrorCode.FRAME_WORK_INTERNAL_ERROR)
    is BusinessException -> {
        response.statusCode = HttpStatus.valueOf(ex.errorCode.status)
        ErrorResponse(code = ex.errorCode)
    }
    // 그외 오류는 ErrorCode.UNDEFINED_ERROR 기반으로 메시지를 사용
    else -> {
        response.statusCode = HttpStatus.valueOf(ErrorCode.UNDEFINED_ERROR.status)
        ErrorResponse(code = ErrorCode.UNDEFINED_ERROR)
    }
}
```
해당 코드에서 `else`에 해당하며 정의하지 않은 예외에 대한 부분에 대한 핸들링입니다. 예를 들어 Spring Cloud Gateway 등 내부 예외, 새롭게 추가한 의존성의 내부 예외, 우리가 직접 작성한 예외지만 `BusinessException`를 기반으로 하지 않은 예외 코드 등이 있습니다.

```kotlin
@Component
class GlobalFilter : AbstractGatewayFilterFactory<GlobalFilter.Config>(Config::class.java) {

    private val log by logger()

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            // 코틀린에서 제공해주는 check 메서드
            check(config.preLogger) { "check 메서드..." }

            chain.filter(exchange)
        }
    }
}
```

위 코드처럼 코틀린 자체적으로 지원해 주는 `check`메서드를 사용하는 경우 `IllegalStateException`을 예외를 발생시킵니다. 이런 경우 BusinessException에 의해서 핸들링되지 않기 때문에 `else` 항목으로 잡히게 되며 `ErrorCode.UNDEFINED_ERROR`에 의해 ErrorResponse가 내려가게 됩니다.

필요하다면 `IllegalStateException` 객체를 분기문에 추가하여 핸들링 할 수도 있습니다. 하지만 모든 예외에 대한 핸들링을 할 수 없기 때문에 우리가 정의하지 않은 예외에 대해서는 메시지를 통일화하는 작업은 필요합니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/spring-msa/docs/images/error-code-1.png)


`check`메서드로 발생시킨 `IllegalStateException` 객체와 해당 메시지가 보이는 것을 확인할 수 있습니다. 해당 객체로 최종적으로 아래와 같은 메시지로 응답을 내려줍니다.

```
http://localhost:5555/a-service/actuator

HTTP/1.1 500 Internal Server Error
transfer-encoding: chunked
Content-Type: application/json

{
  "message": "정의하지 않은 예외",
  "status": 500,
  "errors": [],
  "code": "C002"
}
```

## 정리
게이트웨이로 여러 내부 API에 대한 서비싱을 제공하고 있다면 내부 서비스들의 Error Response의 통일과 게이트웨이 자체도 서비스 내부의 Error Response와 통일하는 것이 본 내용의 핵심입니다. 그 틀안에서 예외 핸들링 전략은 각 서비스 특성과 처한 환경에 맞게 진행하면 된다고 생각합니다. 위에서 설명한 방법도 게이트웨이에 서비스 관련된 로직이 많지 않다고 가정하고 설명한 전략입니다. 라우팅 관련된 설정으로만 구성된 게이트웨이인 경우 예외 클래스를 따로 정의하고 예외 코드를 정의하지 않고 간단하게 구성하는 것이 더 효율적이라고 판단하며 인증 관련된 코드 및 기타 코드들이 많아 예외를 더 체계적으로 관리하게 된다면 위 전략 보다 더 디테일한 전략이 필요할 거 같습니다.