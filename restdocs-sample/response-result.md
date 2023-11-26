# MSA 환경에 효율적인 HTTP Client 설계 방법

현대의 애플리케이션 아키텍처에서는 하나의 서비스가 독립적으로 모든 기능을 처리하기보다는, 여러 서버들이 상호작용하며 각각의 역할을 수행하곤 합니다. 이러한 상호작용 과정에서 HTTP 통신은 서버 간의 협력을 위한 주요한 수단으로 자주 사용됩니다. 이때, 효율적이고 안정적인 HTTP 클라이언트 코드의 작성은 매우 중요한 고려사항이 됩니다. 따라서, 본 포스팅에서는 이러한 컨텍스트 하에 HTTP 클라이언트 코드를 설계하는 방법에 대해 자세히 살펴보고자 합니다. 특히 HTTP 통신에서는 실패 케이스가 불가피하므로, 실패 시 유연한 대처를 가능하게 하는 코드 설계, 통신 실패 및 다양한 시나리오에 대응하는 직관적이고 효과적인 방법에 대해서 다루어보겠습니다.

## HTTP 클라이언트 설계 시 고려해야 할 점

먼저 일반적으로 작성되는 HTTP 클라이언트 코드에서 발견되는 다양한 문제점들을 자세히 살펴보도록 하겠습니다.

### 예외 상황 처리의 고려

HTTP 클라이언트 코드 작성 시, 항상 실패 케이스에 대한 고려를 해야합니다. 이런 실패 케이스에 대한 어려움을 정리해보겠습니다.

```kotlin
data class MemberResponse(
    val id: Long,
    val name: String,
    val email: String
)

@Service
class MemberClient(
    private val restTemplate: RestTemplate
) {

    fun getMember(memberId: Long): MemberResponse {
        val url = "http://example.com/api/members/$memberId"
        return restTemplate.getForObject(url, MemberResponse::class.java)!!
    }
}
```

코드 예제는 `getMember` 함수는 회원의 ID를 인자로 받아 해당 회원의 정보를 조회합니다. 응답은 JSON 형태로 반환되며, 이를 `MemberResponse` 객체로 역직렬화여 객체를 리턴하는 단순한 코드입니다. 하지만 해당 코드를 사용하는 곳에서는 HTTP 4xx, 5xx 응답과 같은 비정상적인 케이스도 염두를 해야하기 때문에 단순하지만은 않습니다. 어떤 점들을 고려해야할지 살펴 보겠습니다.

```kotlin
fun `memberResponse 응답이 필수인 경우`(memberId: Long) {
    try {
        // 비즈니스로직 처리에 member 객체가 필수 값이다.
        val member: MemberResponse = memberClient.getMember(memberId)
    } catch (e: Exception) {
        throw IllegalArgumentException("....")
    }
}
```

`MemberResponse` 객체가 비즈니스 로직에 필수적일 때, 4xx나 5xx 같은 비정상적인 HTTP 응답 또는 기타 예외 상황이 발생하면 `getMember()` 함수로부터 `MemberResponse`를 받을 수 없게 됩니다. 이 경우, `try-catch` 블록을 사용하여 이러한 예외 상황을 처리하고, 발생한 오류를 명시적인 예외 메시지로 알리는 것이 한 가지 해결 방법입니다. 그러나 이로 인해 **`getMember` 함수를 사용하는 모든 코드 부분에 `try-catch` 블록을 적용하고, 각 상황에 맞는 예외를 던지는 책임이 사용자에게 전가됩니다.** 이 문제를 해결하기 위한 간단한 방법은 `MemberClient` 내에 아래와 같은 메서드를 제공하는 것입니다.

```kotlin
fun getMember(memberId: Long): MemberResponse? {
    // ..
    // 예외 케이스인 경우 null 리턴
    if (xxx) {
        return null
    }
    // 정상 케이스면 memberResponse 응답 
    return memberResponse
}

fun getMemberOrThrow(memberId: Long): MemberResponse {
    // ..
    // 예외 케이스인 경우 Exception 발생
    if (xxx) {
        throw IllegalArgumentException("...")
    }
    // 정상 케이스면 memberResponse 응답 
    return memberResponse
}

fun getMember(memberId: Long): ResponseEntity<Member> {
    val url = "http://localhost:8080/api/members/$memberId"
    // GET 요청을 보내고 ResponseEntity로 응답을 받음
    return restTemplate.getForEntity(url, Member::class.java)
}
```

그러나 세부적인 예외 처리가 필요한 경우, 이러한 메서드들만으로는 충분하지 않습니다. 오류 응답에 따른 추가적인 복구 정책과 예외 처리가 필요한 상황에서 단순한 null 반환 또는 예외 발생 방식은 불충분합니다. **즉, 클라이언트 코드가 구체적인 예외 처리 전략을 수립할 수 있도록, 오류에 대한 충분한 컨텍스트 정보를 제공하는 것이 필요합니다.**

### 라이브러리 교체시 변경 사항을 최소화 고려

HTTP 클라이언트 라이브러리는 다른 라이브러리에 비해 자주 교체될 가능성이 높으므로, 이에 따른 영향 범위를 최소화하는 설계가 중요합니다. 예를 들어, `RestTemplate`을 사용할 때 `ResponseEntity<T>`를 리턴 타입으로 사용하는 것은 라이브러리에 대한 의존성을 높이게 됩니다. 이는 HTTP 상태 코드를 포함하여 요청의 성공 여부를 판단하는 데 사용되지만, 라이브러리 교체 시 영향을 받는 코드 범위가 넓어지며, 특히 멀티 모듈 프로젝트에서는 교체 비용이 더욱 증가합니다.

![](https://tech.kakaopay.com/_astro/011.38d51c8e_dYW2O.png)

예를 들어, 다음과 같은 코드에서 `getMember()` 메서드의 리턴 타입이 `ResponseEntity<Member>`로 되어 있을 경우, 라이브러리 교체가 필요할 때 모든 관련 코드에 변경이 필요하게 됩니다:

```kotlin
fun order(memberId: Long): Order {
    // 주문을 진행시 유저 정보 조회
    val memberResponse: ResponseEntity<Member> = memberClient.getMember(memberId)
    // ... 비즈니스 로직
    return order
}
```

이 문제를 해결하기 위해서는 HTTP 통신을 담당하는 모듈이 이러한 의존성을 내부적으로 관리하도록 설계해야 합니다. 이렇게 하면 모듈을 사용하는 다른 부분에서는 변경의 영향을 받지 않으므로, 전체적인 시스템 설계에서 책임과 역할을 적절하게 배분할 수 있습니다. 따라서 HTTP 클라이언트 라이브러리의 교체 가능성을 염두에 두고 특정 라이브러리에 직접적으로 의존하지 않는 코드 설계가 바람직합니다.

### MSA 환경에서의 오류 전달 및 핸들링 고려

```mermaid
sequenceDiagram
    A API ->> B API: 요청
    B API ->> C API: 요청
    C API -->> B API: 오류 응답
    Note right of B API: {"message": "Invalid Value", "status": 400, "code": "C001"}
    B API -->> A API: 오류 응답
    Note right of A API: {"message": "Invalid Value", "status": 400, "code": "C001"}
```

MSA(마이크로서비스 아키텍처) 환경에서 특정 비즈니스 로직을 수행하기 위해 다수의 서비스들이 HTTP 통신을 통해 협력하는 경우가 많습니다. 이 과정에서 연속된 호출 흐름 중 오류가 발생할 경우, 그 오류 응답을 최초의 호출지까지 전달해야 할 필요성이 생깁니다. 이는 정확한 오류 메시지를 통해 문제를 식별하고 해결하여 후속 로직을 진행할 수 있도록 하기 위함입니다.

```kotlin
// B API Sample Code
fun getXXX(): Triple<Int, xxxResponse?, ErrorResponse?> {
    // .. HTTP 통신 이후 Status Code를 기준으로 응답 객채 or 오류 객체 전달
    return Triple(
        first = response.statusCodeValue,
        second = body,
        third = errorResponse
    )
}

// 클라이언트를 사용하는 코드에서 예외 핸들링
fun xxx() {
    val response = cClient.getXXX()
    if (response.is2xxSuccessful) {
        // 성공인 경우, Body Notnull 으로 단언
        val body = response.second!!
    } else {
        // 2xx가 아닌 경우의 Error Response Notnull 으로 단언
        val errorResponse = response.third!!
        throw Exception(errorResponse)
    }
}
```

예를 들어, C API 서버에서 오류가 발생했을 때, B API 서버는 오류 응답을 그대로 전달하기 위해 Triple 객체를 사용합니다. 이 객체는 HTTP 상태 코드, 응답 본문, 오류 응답을 포함하며, 호출하는 곳에서는 이 정보를 바탕으로 상세한 제어를 할 수 있습니다. 성공적인 응답과 오류 응답은 각각 본문과 오류 객체에 대한 Notnull 단언을 통해 처리됩니다. 오류 발생 시, B 서버는 C 서버로부터 전달받은 오류 메시지를 그대로 전달합니다. **그러나 이 방식은 직관적이지 않으며, nullable 처리와 오류 핸들링에 대한 책임이 외부로 전가되어 중복 코드와 과도한 부담을 야기합니다.**

### 고려해야 할 점 정리

1. **항상 고려해야 할 실패 케이스**: HTTP 2xx 이외의 실패 케이스에 대해 항상 주의를 기울여야 합니다.
2. **비즈니스 로직에 맞는 핸들링 제공**: 호출하는 곳에서 각각의 비즈니스 로직에 적합하게, 간결하고 일관된 방식으로 오류를 핸들링할 수 있도록 기능을 제공해야 합니다.
3. **라이브러리 교체시 영향 최소화**: HTTP 클라이언트 라이브러리를 교체할 때 발생할 수 있는 영향을 외부 객체나 모듈에 미치지 않도록 격리해야 합니다.
4. **분산 환경에서의 오류 메시지 전달**: 분산 환경에서 여러 API 호출이 이루어질 때, 오류를 정확하게 파악하고 메시지를 효과적으로 전달할 수 있어야 합니다.

-----------

## HTTP Client 개선

### HTTP Client 개선 : 코틀린의 Result 개념을 활용한

명시적인 오류 처리: 예외 대신 결과 객체를 사용함으로써, 오류가 발생할 수 있는 코드 부분을 명확히 식별할 수 있습니다. 함수 반환 값의 안전성: 함수가 예외를 던지지 않고 Result 객체를 반환함으로써, 함수의 사용자는 반환된 값을 안전하게 처리할 수 있습니다. 유연한 오류 처리: Result 타입은 오류 처리를 위한 다양한 메소드(getOrNull, getOrElse, getOrThrow 등)를 제공하여, 사용자가 상황에 맞게 오류를 처리할 수 있게 합니다.

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

이 예시에서 fetchProfile 함수는 Result 타입을 반환합니다. onSuccess 블록은 결과가 성공적일 때 실행되고, onFailure 블록은 오류가 발생했을 때 실행됩니다. 이러한 특징을 이용하면 HTTP Client 응답을 효율적으로 처리할 있어 HTTP Response에 전목 시켜보겠습니다.

### 코틀린 Result 개념을 활용한 ResponseResult

코틀린에서 Result 타입은 함수가 성공적으로 결과를 반환했는지, 아니면 예외가 발생했는지를 포장하는 데 사용됩니다. 이는 함수가 예외를 던지는 대신, 성공적인 결과나 실패를 나타내는 객체를 반환하게 만들어, 오류 처리를 더 간결하고 안전하게 할 수 있게 도와줍니다.

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
                    errorResponse.status.isClientError() -> throw ServiceException(errorResponse = errorResponse, code = ErrorCode.INVALID_INPUT_VALUE)
                    else -> throw ServiceException(errorResponse = errorResponse, code = ErrorCode.SERVER_ERROR)
                }
            }
        }
    }
}

data class ErrorResponse(
    val message: String,
    val code: String,
    val status: Int
) {
    val timestamp: LocalDateTime = LocalDateTime.now()
    ...
}

```

`ResponseResult`라는 `sealed class`를 정의하고 있습니다, 이는 제네릭 타입 `T`를 사용하는 결과 처리 클래스입니다. `ResponseResult` 클래스는 HTTP 통신 이후 작업의 결과를 나타내는 데 사용됩니다. HTTP 통신 이후 성공 응답을 주는 `Success<T>` 서브 클래스와, 2xx가 아닌 실패의 경우 `ErrorResponse`을 전달받는 `Failure` 서브 클래스의 두 가지 클래스를 가지고 있으며, 여러 메소드들을 제공하고 있습니다. 각 요소를에 대해서 더 설명드리겠습니다.

#### 서브 클래스

1. **Success**: 성공적인 결과를 나타내며, `body`라는 필드를 통해 결과 데이터를 포함합니다.
2. **Failure**: 실패를 나타내며, `errorResponse`라는 필드를 통해 오류 정보를 포함합니다.

#### 프로퍼티

- `isSuccess`: 현재 인스턴스가 `Success`인지 여부를 반환합니다.
- `isFailure`: 현재 인스턴스가 `Failure`인지 여부를 반환합니다.

#### 메소드

1. **onSuccess**: `Success` 인스턴스일 때 실행할 액션을 정의합니다. `action` 함수는 `body`를 인자로 받습니다.
2. **onFailure**: `Failure` 인스턴스일 때 실행할 액션을 정의합니다. `action` 함수는 `errorResponse`를 인자로 받습니다.
3. **getOrNull**: `Success` 인 경우에는 `action` 함수를 실행하고 결과를 반환하며, `Failure` 인 경우에는 `null`을 반환합니다.
4. **getOrThrow**: `Success` 인 경우에는 `action` 함수를 실행하고 결과를 반환합니다. `Failure` 인 경우에는 오류 상태에 따라 `ServiceException`을 던집니다. 여기서 오류 상태가 클라이언트 오류인지 서버 오류인지에 따라 다른 `ErrorCode`를 사용합니다.

#### 오류 처리

`Failure`의 경우, `ErrorResponse`에 따라 예외 발생 여부를 결정합니다. `getOrThrow` 메소드에서는 `ErrorResponse`의 상태(`status`)를 확인하여 적절한 `ServiceException`을 던집니다. 이 예외는 사용자 정의 예외로 보이며, 오류 코드를 포함하고 있습니다.

#### 정리

이 `ResponseResult` 클래스는 HTTP 호출과 같은 작업의 결과를 더 유연하고 안전하게 처리할 수 있도록 설계되었습니다. 성공과 실패 케이스를 명확하게 구분하고, 각 상황에 맞는 로직을 실행할 수 있도록 메소드를 제공합니다. 또한, 예외 처리를 위한 메커니즘이 포함되어 있어, 오류 상황에 대한 세밀한 제어가 가능합니다.

## 문제 해결

### 오류 응답에 대한 핸들링의 어려움

HTTP 클라이언트 코드는 항시 2xx가 아닌 경우애 대해서 염두를 하고 코드를 작성해야 합니다. 블라블라

#### 2xx 응답을 확정 하고 싶은 경우

```kotlin
@Test
fun `getOrThrow notnull 보장, 오류 발생시 오류 메시지를 그대로 전달`() {
    memberClient
        .getMember(1L)
        .getOrThrow { it }
}
```

### 2xx 아닌경우 null 바인딩 이후 로직 제어

```kotlin
@Test
fun `getOrNull 통신 실패시 null 응답,`() {
    val member: Member? = memberClient
        .getMember(1L)
        .getOrNull { it }

    // member null 여부에 따른 후속 조치 작업 진행
}
```

### 오류 발생시 예외 직접 핸들링하고 싶은 경우

```kotlin

@Test
fun `onFailure + onSuccess`() {
    val orThrow: ResponseResult<Member> = memberClient
        .getMember(1L)
        .onFailure { it: ErrorResponse ->
            // onFailure 오류 발생시 ErrorResponse 기반으로 예외 처리 진행
        }
        .onSuccess {
            it
        }
}
```

### 성공 or 실패 확인 케이스

```kotlin
@Test
fun `isSuccess + isFailure`() {
    // API PUT, POST 등에 사용
    val result1 = memberClient
        .getMember(1L)
        .isSuccess

    val result2 = memberClient
        .getMember(1L)
        .isFailure
}
```

### 통신 실패시 기본값 정책 케이스

```kotlin
@Test
fun `getOrDefault - 통시 실패시 기본 값 할당`() {
    val orDefault = memberClient
        .getMember(1L)
        .getOrDefault(
            default = Member(
                email = "",
                name = "",
                status = MemberStatus.NORMAL

            ),
            transform = { it }
        )
}
```

#### 여러가지 조합하여 핸들링 하고 싶은 경우

```kotlin
@Test
fun `조합`() {
    val orDefault = memberClient
        .getMember(1L)
        .onFailure { it: ErrorResponse ->
            // 실패 케이스 보상 트랜잭션 API 호출
        }
        .onSuccess {
            // 성공 이후 후속 작업 진행

        }
        .getOrDefault(
            // 혹시라도 오류 발생시 기본 값 응답
            default = Member(
                email = "",
                name = "",
                status = MemberStatus.NORMAL

            ),
            transform = { it }
        )
}
```

### HTTP Client는 라이브러리는 교체의 어려움 해결

RestTemplate의 사용은 직관성이 떨어지고, 불필요한 의존성 문제와 테스트 시 Application Context가 필요한 문제 등을 야기할 수 있습니다. 따라서 코틀린을 사용할 경우, HTTP 클라이언트 라이브러리로 [Fuel](https://github.com/kittinunf/fuel) 또는 [Ktor](https://github.com/ktorio/ktor)를 추천합니다. 단순하고 소규모의 HTTP 클라이언트 작업을 할 때는 Fuel이 적합하며, 보다 복잡하고 다양한 HTTP 통신이 필요한 상황에서는 Ktor를 사용하는 것이 좋습니다. **또한, `ResponseResult`는 특정 HTTP 클라이언트 라이브러리에 종속적이지 않게 구현되어 있어, 필요에 따라 RestTemplate를 계속 사용하는 것도 가능합니다.**

```kotlin
// ktor 확장 함수
suspend inline fun <reified T> HttpResponse.responseResult(): ResponseResult<T> {
    return when {
        status.isSuccess() -> ResponseResult.Success(body())
        else -> {
            val responseBody = bodyAsText()
            ResponseResult.Failure(
                when {
                    isErrorResponseSerializeAble(responseBody) -> defaultObjectMapper.readValue(responseBody, ErrorResponse::class.java)
                    else -> defaultErrorResponse
                }
            )
        }
    }
}

// RestTemplate 확장 함수
inline fun <reified T> ResponseEntity<String>.responseResult(): ResponseResult<T> {
    return when (this.statusCode.is2xxSuccessful) {
        true -> ResponseResult.Success(defaultObjectMapper.readValue<T>(body!!))
        else -> {
            val responseBody = this.body.toString()
            ResponseResult.Failure(
                when {
                    isErrorResponseSerializeAble(responseBody) -> defaultObjectMapper.readValue(responseBody, ErrorResponse::class.java)
                    else -> defaultErrorResponse
                }
            )
        }
    }
}
```

RestTemplate 경우 기본 설정이 2xx가 아닌 경우 예외를 발생 시키기 때문에 `ResponseErrorHandler`을 통해서 Custom 설정으로 변경이 필요하며, ResponseEntity 객체에서 2xx 경우에만 시리얼라이즈가 성공적으로 진행하 가능하기 때문에 `ResponseEntity<String>`으로 String 타입을 받고, 2xx 경우에 시리얼라이즈를 진행합니다. 이후 `responseResult<Member>()`으로 `<T>` 타입을 명시적으로 받아서 처리합니다.

### MSA 환경에서 오류 전파의 어려움 해결

외부나 다른 팀의 서버와 달리, 동일한 팀 내에서 운영되는 서버들의 오류 응답(Error Response)을 통일하는 것이 바람직합니다. 만약 팀 내에서도 서버별로 오류 메시지가 서로 다르면, 4xx 및 5xx 오류에 대한 처리가 더 복잡해집니다. 또한, 이러한 서버들과 연동하는 다른 팀도 4xx 및 5xx 오류에 대해 처리하는 복잡도가 높아질 수 있습니다. 따라서 같은 팀 내에서 서비스하는 서버들은 오류 응답을 통일하여 관리하는 것이 좋습니다. 이렇게 하면 오류 처리가 간소화되고, 다른 팀과의 협업도 원활해질 수 있습니다.

팀 내에서 서비스되는 서버들의 오류 응답을 통일화하는 것은 오류 처리를 간소화하고 코드 비용을 줄이는 데 도움이 됩니다. 예를 들어, 아래의 JSON 구조로 오류 응답이 통일되었다고 가정해 봅시다.

```json
{
  "message": "Invalid Value",
  "status": 400,
  "code": "C001"
}
```

이 경우 아래와 같이, 팀 내 서비스 간의 호출에서 오류가 발생했을 때

```
# 요청
A -> B -> C

# 응답
A <- B <- C
```

이 오류 메시지는 최초 호출지인 서비스 A까지 전달될 수 있어야 합니다. 오류 메시지가 통일되면 이러한 전달이 용이해지고, 관련 코드 비용도 감소합니다.

```kotlin
// 표준 ErrorResponse 직렬화 가능 여부 확인
fun isErrorResponseSerializeAble(responseBody: String): Boolean {
    return when (val rootNode = defaultObjectMapper.readTree(responseBody)) {
        null -> false
        else -> rootNode.path("message").isTextual && rootNode.path("status").isNumber && rootNode.path("code").isTextual
    }
}

// 기본 ErrorResponse 객체
val defaultErrorResponse = ErrorResponse(
    code = ErrorCode.INVALID_INPUT_VALUE
)
```

`responseResult` 함수에서 2xx가 아닌 경우, 응답받은 오류 응답이 팀 내 표준 오류 메시지인지 확인하고, 맞다면 `ErrorResponse` 객체로 직렬화합니다. 그렇지 않은 경우 기본 `ErrorResponse`를 사용합니다. 예를 들어, 서버가 다운되어 표준 오류 응답을 제공할 수 없는 상황에서 기본 응답을 사용합니다. 이외에도 다양한 상황에 대비한 방어적 로직이 필요합니다.

`ErrorResponse` 객체를 사용하면, 오류 발생 시 상세한 예외 처리가 가능해집니다. 예를 들어, `memberClient.getMember` 호출로부터 `ErrorResponse` 객체를 받게 되면, 오류가 발생했을 때 이 객체를 기반으로 추가적인 핸들링을 할 수 있습니다. 아래의 코드 예시는 이러한 상황을 보여줍니다.

```kotlin
fun xxx() {
    val member = memberClient.getMember(1L)

    member
        .onFailure { errorResponse: ErrorResponse ->
            // 오류 발생시 넘겨 받은 errorResponse 객체로 추가 핸들링 가능
        }
}
```

이 코드에서 `onFailure` 블록 내부에서 `ErrorResponse` 객체에 대한 추가 처리를 할 수 있습니다. 이렇게 오류에 대한 응답을 구체적으로 다룰 수 있게 되므로, 예외 상황에 대한 대응이 보다 정교하고 세밀하게 이루어질 수 있습니다.





-----

* [ ] onSuccess 콜백
* [ ] onFailure 콜백
* [ ] getOrNull null 처리 위임
* [ ] getOrThrow notnull을 보장 받고 싶은 패턴
* [ ] 오류 전달하기
* [ ] 특정 라이브리에대 대해 의존적이지 않는다.
* [ ] 이전 Error Response를 전달 해야한다.
* [ ] 내부 Error, 외부 Error 을 구분 해야한다.
* [ ] 내부 Error, 외부 Error에 맞게 Error Response 디시리얼라이즈 정책을 정해야한다.
* [ ] 테스트 코드 mock 기반으로 작성
* [ ] Default Value