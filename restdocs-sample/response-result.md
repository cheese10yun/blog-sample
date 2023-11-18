# HTTP Client 블라블라

애플리케이션에서 로직을 처리하는 과정에서 다른 여러 서버들의 협력으로 로직 처리하게 됩니다. 이때 여러 서버들의 협력하는 방법중 하나로 HTTP 통신을 자주 사용하게 되며, 관련 HTTP Client 코드르 작성하게 됩니다. 이본 포스팅에서는 HTTP 클라이언트 코드 설계를 하는 방법에 대해서 다뤄보겠습니다.


## HTTP Client Sample

Spring 프레임워크를 사용한다면 가장 대중적으로 사용하는 라이브러리인 `RestTemplate`를 이용하여 HTTP 통신으로 Member를 조회하는 코드를 작성하면 아래와 같다   

```kotlin
data class Member(
    val id: Long,
    val name: String,
    val email: String
)

@Service
class MemberClient(
    private val restTemplate: RestTemplate 
) {

    fun getMember(memberId: Long): Member {
        val url = "http://example.com/api/members/$memberId"
        return restTemplate.getForObject(url, Member::class.java)!!
    }
}
```

해당 코드는 직관적이며 단순해 보이지만 실제 호출하는 곳에서 사용할때 애매한 부분이 있다.

```kotlin

fun xxx() {
    val member: Member = memberClient.getMember(1L)
    // 비지니스 로직
}
```

사용한는 곳에서에서 통신이 실패하는 케이스 즉 2xx가 아닌 경우 `member`를 정상적으로 응답 받지 못하는 경우에 대한 코드를 작성 해야합니다. 그런 코드를 대응하기 위해서 택할 수 있는 방법은 다음과 같이 방식입니다.

```kotlin
fun getMember(memberId: Long): ResponseEntity<Member> {
    val url = "http://example.com/api/members/$memberId"
    // GET 요청을 보내고 ResponseEntity로 응답을 받음
    return restTemplate.getForEntity(url, Member::class.java)
}

fun xxx() {
    val response = memberClient.getMember(1L) // 1번 회원 조회를 가정
    
    if (response.statusCode.is2xxSuccessful) {
        // 비즈니스 로직
    } else {
        // 2xx가 아닌 경우의 처리 로직
    }
}
```

`ResponseEntity<T>` 객체를 리턴하여, 사용하는 곳에서 http status code에 따른 분기문을 선택할 수 있게 리턴 타입을 변경하는 것이빈다.

그렇다면 이 코드는 좋은 코드일까요? 개인적으로는 좋은 코드라 보여지지 않습니다.

첫 번째 이유는 리턴 타입이 `ResponseEntity<T>`으로 스프링에 지나치게 의존적인 타입이 리턴되기 때문입니다. 

HTTP 클라이언트 라이브러리는 대체성이 강한 성격이 있어, 특정 라이브러리에 지나치게 의존적인 리턴 타입을 지정하게 되면 해당 라이브러리가 교체되는 경우 그 비용이 크게 들기 때문에 가급적 특정 라이브러리에 의존하지 않는 형식으로 리턴 타입을 결정하는 것이 좋습니다.

특히 멀티 모듈로 구성되어 있는 프로젝트에서 HTTP 통신을 담당하는 모듈을 분리해서 관리하는 경우, 이런식으로 특정 라이브러리의 리턴 타입을 지정하는 경우 라이브러리 교체가 되면 해당 모듈을 사용하는 모듈에서 직접적인 영향을 받게됩니다.

이러한 문제는 해당 모듈에서 이런 의존도를 관리하여, 해당 모듈을 사용하는 곳에서 그 변경의 여파를 최소화 해야합니다. 기것은 비담 모듈만의 문제는 아니며, 책임과 역할을 알맞게 부여하고 그에 알맞는 책임과 역할을 수행할 수 있도록 디자인 해야합니다.

![](https://tech.kakaopay.com/_astro/011.38d51c8e_dYW2O.png)


두 번째 이유는 사용하는 곳에서 HTTP 통신 이후에 결과에 대한 핸들링이 반족작이며, 불편하다는 점입니다.

`MemberClient`를 사용하는 입장에서는 어떤 경우에는 조회시 정상상적으로 2xx가 아닌 응답을 받는 경우 `MemberClient` 자체에서 예외를 발생 시켜, 사용 하는 곳에서 별다르게 신경쓰지 않고 싶을 때가 있고, 반대로 `MemberClient` 호출 이후 응답에 대한 결과를 넘겨 받아 사용하는 곳에서 직접 핸들링 하고 싶은 경우가 있을 수 있습니다.

이런 경우 `MemberClient` 로직에 `getMember` 메서드를 통신 실패시 예외를 발생 시키는 작업 메서드, 퉁신 실패에 따른 제어권을 넘겨주는 `ResponseEntity<T>`을 리턴하는 메서드를 따로 구현 하게 되기 때문입니다.


## 좋은 HTTP Client 조건

### 라이브러리 교체시 사용하는 곳에서 변경사항 최소화 하기

HTTP Client 라이브러리는 교체가 빈번하게 발생하는 라이브러리이기 때문에 리턴 타입에 특정 라이브러리의 객체로 리턴하게 되면 **변경에 취약하기 때문에 특정 라이브러리에 의존적이지 않게 리턴 타입을 지정하는 것이 좋습니다.**


### 호출하는 곳에서 편하게 제어

HTTP 통신 이후 성공, 실패에 따른 호출한 곳에서 HTTP 통신 이후 성공, 실패에 따른 핸들링을 쉽고 간편하게 제어할 수 있어야합니다. 여러 서비스들의 연속적인 호출의 경우 

오류가 발생한 곳의 Error Message를 명확하게 전달하는 것도 중요 합니다.

```
A -> B -> C

A <- B <- C
```

C의 오류가 A까지 전파되는게 중요합니다.


## 추천 HTTP 라이브러리

RestTemplate 같은 경우 코드의 직관성이 떨어지며, 불필요한 의존성 문제, 테스트시에 Application Context 필요한 문제 등등이 있기 때문에 코틀린을 사용한다면 HTTP Client [Fuel](https://github.com/kittinunf/fuel), [Ktor](https://github.com/ktorio/ktor) 라이브러리를 추천드립니다. 간단하고 HTTP Client를 많이 작성하지 않을거라면 Fuel를 권장드리고, 복잡하고 다양한 HTTP 통신을 진행한다면 Ktor을 추천드립니다. 

## 코틀린 Result 개념을 활용한 ResponseResult


코틀린에서 Result 타입은 함수가 성공적으로 결과를 반환했는지, 아니면 예외가 발생했는지를 포장하는 데 사용됩니다. 이는 함수가 예외를 던지는 대신, 성공적인 결과나 실패를 나타내는 객체를 반환하게 만들어, 오류 처리를 더 간결하고 안전하게 할 수 있게 도와줍니다.

### 코틀린 Result 

Result 타입의 장점
명시적인 오류 처리: 예외 대신 결과 객체를 사용함으로써, 오류가 발생할 수 있는 코드 부분을 명확히 식별할 수 있습니다.
함수 반환 값의 안전성: 함수가 예외를 던지지 않고 Result 객체를 반환함으로써, 함수의 사용자는 반환된 값을 안전하게 처리할 수 있습니다.
유연한 오류 처리: Result 타입은 오류 처리를 위한 다양한 메소드(getOrNull, getOrElse, getOrThrow 등)를 제공하여, 사용자가 상황에 맞게 오류를 처리할 수 있게 합니다.

```kotlin

fun fetchProfile(userId: String): Result<Profile> {
    return try {
        // 데이터 가져오기 성공
        val profile = getProfileFromServer(userId)
        Result.success(profile)
    } catch (e: Exception) {
        // 오류 발생
        Result.failure(e)
    }
}
```

```kotlin
val result = fetchProfile("user123")

// 성공한 경우 처리
result.onSuccess { profile ->
    println("Profile loaded: $profile")
}

// 실패한 경우 처리
result.onFailure { error ->
    println("Error occurred: ${error.message}")
}
```

이 예시에서 fetchProfile 함수는 Result 타입을 반환합니다. onSuccess 블록은 결과가 성공적일 때 실행되고, onFailure 블록은 오류가 발생했을 때 실행됩니다.

### 코틀린 Result 개념을 적용 시킨 ResponseResult

#### Code

```kotlin
sealed class ResponseResult<out T> {

    data class Success<out T>(val body: T) : ResponseResult<T>()
    data class Failure(val errorResponse: ErrorResponse) : ResponseResult<Nothing>()

    val isSuccess: Boolean
        get() = this is Success

    val isFailure: Boolean
        get() = this is Failure

    /**
     * [Success] 경우 콜백
     */
    inline fun onSuccess(action: (T) -> Unit): ResponseResult<T> {
        if (this is Success) {
            action(body)
        }
        return this
    }

    /**
     * api a -> api b -> api c ->
     * c -> b - a
     * ErrorResponse ->
     * [Failure] 경우 콜백, 사용하는 곳에서 ErrorResponse에 따라 예외를 발생 여부를 진행
     */
    inline fun onFailure(action: (ErrorResponse) -> Unit): ResponseResult<T> {
        if (this is Failure) {
            action(errorResponse)
        }
        return this
    }

    /**
     * [Failure] 경우에는 null이 응답 된다.
     */
    inline fun getOrNull(action: (T) -> @UnsafeVariance T): T? {
        return when (this) {
            is Success -> action(body)
            else -> null
        }
    }

    /**
     * [Failure] 경우에는 예외가 발행하기 때문에 Notnull을 보장한다.
     */
    inline fun getOrThrow(action: (T) -> @UnsafeVariance T): T {
        when (this) {
            is Success -> return action(body)
            is Failure -> {
                when {
                    errorResponse.status.isClientError() -> throw ServiceException(errorResponse = errorResponse, code = ErrorCode.INVALID_INPUT_VALUE )
                    else -> throw ServiceException(errorResponse = errorResponse, code = ErrorCode.SERVER_ERROR)
                }
            }
        }
    }
}
```

이 코틀린 코드는 `ResponseResult`라는 `sealed class`를 정의하고 있으며, 이는 제네릭 타입 `T`를 사용하는 결과 처리 클래스입니다. `ResponseResult` 클래스는 API 호출과 같은 작업의 결과를 나타내는 데 사용됩니다. 클래스는 두 가지 서브 클래스를 가지고 있으며, 여러 메소드를 제공합니다. 각 요소를 분석해보겠습니다.

### 서브 클래스

1. **Success**: 성공적인 결과를 나타내며, `body`라는 필드를 통해 결과 데이터를 포함합니다.
2. **Failure**: 실패를 나타내며, `errorResponse`라는 필드를 통해 오류 정보를 포함합니다.

### 프로퍼티

- `isSuccess`: 현재 인스턴스가 `Success`인지 여부를 반환합니다.
- `isFailure`: 현재 인스턴스가 `Failure`인지 여부를 반환합니다.

### 메소드

1. **onSuccess**: `Success` 인스턴스일 때 실행할 액션을 정의합니다. `action` 함수는 `body`를 인자로 받습니다.
2. **onFailure**: `Failure` 인스턴스일 때 실행할 액션을 정의합니다. `action` 함수는 `errorResponse`를 인자로 받습니다.
3. **getOrNull**: `Success` 인 경우에는 `action` 함수를 실행하고 결과를 반환하며, `Failure` 인 경우에는 `null`을 반환합니다.
4. **getOrThrow**: `Success` 인 경우에는 `action` 함수를 실행하고 결과를 반환합니다. `Failure` 인 경우에는 오류 상태에 따라 `ServiceException`을 던집니다. 여기서 오류 상태가 클라이언트 오류인지 서버 오류인지에 따라 다른 `ErrorCode`를 사용합니다.

### 오류 처리

`Failure`의 경우, `ErrorResponse`에 따라 예외 발생 여부를 결정합니다. `getOrThrow` 메소드에서는 `ErrorResponse`의 상태(`status`)를 확인하여 적절한 `ServiceException`을 던집니다. 이 예외는 사용자 정의 예외로 보이며, 오류 코드를 포함하고 있습니다.

### 결론

이 `ResponseResult` 클래스는 API 호출과 같은 작업의 결과를 더 유연하고 안전하게 처리할 수 있도록 설계되었습니다. 성공과 실패 케이스를 명확하게 구분하고, 각 상황에 맞는 로직을 실행할 수 있도록 메소드를 제공합니다. 또한, 예외 처리를 위한 메커니즘이 포함되어 있어, 오류 상황에 대한 세밀한 제어가 가능합니다. 


#### 사용법

### HTTP Client 라이브러리에 적용

#### ktor
```kotlin
suspend inline fun <reified T> HttpResponse.responseResult(): ResponseResult<T> {
    return when {
        status.isSuccess() -> ResponseResult.Success(body())
        else -> {
            val responseBody = bodyAsText()
            ResponseResult.Failure(
                when {
                    isServiceErrorResponseSerializeAble(responseBody) -> defaultObjectMapper.readValue(responseBody, ErrorResponse::class.java)
                    else -> defaultErrorResponse
                }
            )
        }
    }
}
```

### Fuel


### 공통 설정

```kotlin
/**
 *  표준 [ErrorResponse]를 Serialize 가능 여부
 */
fun isServiceErrorResponseSerializeAble(responseBody: String): Boolean {
    return when (val rootNode = defaultObjectMapper.readTree(responseBody)) {
        null -> false
        else -> rootNode.path("message").isTextual && rootNode.path("status").isNumber && rootNode.path("code").isTextual
    }
}


val defaultObjectMapper: ObjectMapper = ObjectMapper()
    .registerKotlinModule()
    .registerModules(JavaTimeModule(), Jdk8Module())
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .apply { this.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE }


val defaultErrorResponse = ErrorResponse(
    code = ErrorCode.INVALID_INPUT_VALUE
)
```

### HTTP Client 적용

```kotlin
fun getMember(memberId: Long): ResponseResult<Member> {
    return runBlocking {
        client
            .get("http://example.com/api/members/$memberId")
            .responseResult<Member>()
    }
}
```

### 예외 


## ResponseResult


### 라이브러리 교체시 사용하는 곳에서 변경사항 최소화 하기

### 호출하는 곳에서 편하게 제어

* [ ] onSuccess 콜백
* [ ] onFailure 콜백
* [ ] getOrNull null 처리 위임
* [ ] getOrThrow notnull을 보장 받고 싶은 패턴
* [ ] 오류 전달하기


### 손쉽게 테스트 코드 작성하는 방법



* [ ] 특정 라이브리에대 대해 의존적이지 않는다.
* [ ] 이전 Error Response를 전달 해야한다.
* [ ] 내부 Error, 외부 Error 을 구분 해야한다.
* [ ] 내부 Error, 외부 Error에 맞게 Error Response 디시리얼라이즈 정책을 정해야한다.
* [ ] 테스트 코드 mock 기반으로 작성
* [ ] Default Value