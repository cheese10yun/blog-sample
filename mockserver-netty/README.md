# Mockserver Netty 사용해서 HTTP 통신 Mocking 하기

테스트 코드를 작성할 할 때 외부 서비스와 통신하는 구간은 Mocking 해서 해서 테스트를 진행하게 됩니다. 대표적으로 HTTP 외부 통신이 있습니다. 이와 비슷한 내용으로 [RestTemplate Mock 기반 테스트하기](https://cheese10yun.github.io/rest-template-mock-test/)을 포스팅을 했었습니다. 그와 비슷한 주제로 Mockserver Netty 기반으로 Mock Test를 진행하는 방법에 대해서 다루어보겠습니다.


## Mockserver Netty 장점
직관적이고 쉽게 학습하기 좋은 점도 있지만, `MockRestServiceServer`에 비해서 가장 큰 장점으로 생각되는 것은 HTTP Client에 대한 제약이 생대적으로 없는 부분입니다. `MockRestServiceServer` 이름에서부터 설명되지만 `ResTemplate` 테스트를 위한 서비스입니다. 물론 RestTemplate만을 사용하면 괜찮은 도구라고 생각합니다. 전용 테스트 서비스이니 `RestTemplate`와 핏이 잘 맞는 부분은 큰 장점입니다. 하지만 `RestTemplate` 이외의 HTTP Client에 대한 테스트를 진행하기 어려운 점이 있습니다.

무 것보다도 `RestTemplate`의존성이 `spring-boot-starter-web`에 종속해 이따 보니 웹서버 모듈이 아닌 경우에는 `RestTemplate`를 사용하기 어려운 부분이 있습니다. 특히 프로젝트가 고도화되면 모듈을 세부적으로 나누는 작업을 진행하다 보면 `IO` 관련된 모듈을 따로 만들게 되는데 이때 `spring-boot-starter-web` 의존성을 갖는 것이 바람직하지 않는 경우가 있습니다.

## 사용법

### 의존성 추가

```groovy
dependencies {
    implementation("org.mock-server:mockserver-netty:5.11.1")
    implementation("org.mock-server:mockserver-client-java:5.11.1")

    implementation("com.github.kittinunf.fuel:fuel:2.3.0")
    implementation("com.github.kittinunf.fuel:fuel-jackson:2.3.0")
}
```
`mockserver-netty,` `mockserver-client-java`의존성을 추가합니다. `fuel`은 Kotlin 기반 HTTP Client 라이브러리으로 실제 HTTP 통신하는 코드를 작성하기 위해 추가했습니다.

### Sample Code

```kotlin
@RestController
@RequestMapping("/sample")
class SampleApi {

    @GetMapping
    fun getSample() = Sample("foo", "bar")

    data class Sample(
        val foo: String,
        val bar: String
    )
}
```
HTTP 호출을 하기 위해서 간단하게 컨트롤러 코드를 작성합니다.

```kotlin
class FuelClient(
    private val host: String = "http://localhost:8080",
    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .apply { this.propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE }
) {

    fun getSample(): SampleResponse = "$host/sample"
        .httpGet()
        .response()
        .first.responseObject<SampleResponse>(objectMapper)
        .third.get()
}

data class SampleResponse(
    val foo: String,
    val bar: String
)
```
`fuel` 기반으로 HTTP 통신을 하는 코드로 위 컨트롤러를 호출하게 됩니다.

### Test code
```kotlin
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // (1)
internal class FuelClientTest {

    private lateinit var mockServer: ClientAndServer // (2)

    @BeforeAll
    fun startServer() {
        this.mockServer = ClientAndServer.startClientAndServer(8080) //(3)
    }

    @AfterAll
    fun stopServer() {
        this.mockServer.stop()
    }


    @Test
    internal fun `getSample test`() {
        //given
        val client = FuelClient() // (4)
        val responseBody = """
                    {
                      "foo": "foo",
                      "bar": "bar"
                    }
                """.trimIndent()

        //when
        mockServer.`when`(
            HttpRequest.request() // (5)
                .withMethod("GET")
                .withPath("/sample")
        ).respond(
            HttpResponse.response() // (6)
                .withBody(responseBody)
                .withStatusCode(200)
        )

        //then
        val sample = client.getSample() // (7)
    
        // (8)
        then(sample.foo).isEqualTo("foo")
        then(sample.bar).isEqualTo("bar")
    }
}
```

* (1): `@TestInstance(TestInstance.Lifecycle.PER_CLASS)`을 통해서 테스트마다 인스턴스를 생성하는 것을 방지합니다. 그 결과 `@AfterAll, @BeforeAll` 메서드를 static으로 설정하지 않아도 됩니다.
* (2): `mockServer` 인스턴스를 `lateinit`으로 지정합니다.
* (3): `@BeforeAll`을 통해서 테스트 코드가 실행 이전, `8080`포트로 Mock Server, Client를 생성합니다.
* (4): HTTP 통신하는 클라이언트 객체 `FuelClient` 생성합니다. 스프링 Bean이 아니기 때문에 직접 객체를 생성합니다.
* (5): Mock HTTP Request를 지정합니다. `GET` 메서드로 `/sample`을 호출합니다.
* (6): Mock HTTP Response를 지정합니다. 응답받을 HTTP Status Code, Response Body를 작성합니다.
* (7): Mo