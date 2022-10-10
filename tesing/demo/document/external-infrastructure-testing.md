# 외부 인프라스트럭처를 테스트하는 자세

외부 인프라를 의존하는 로직의 경우 테스트 코드를 작성하기 어려운 부분이 있습니다. 예를 들어 특정 날짜 기준으로 환율 정보를 기반으로 금액을 계산하고, 그 환율 정보를 외부 HTTP Server에 질의한다고 하면 HTTP Server가 외부 인프라라에 해당합니다. 이런 경우 대부분 Mocking을 하여 테스트 코드를 작성하게 됩니다. 이전 포스팅 [Mockserver Netty 사용해서 HTTP 통신 Mocking 하기](https://cheese10yun.github.io/mockservernetty/)에서 다룬 적이 있습니다. 해당 포스팅은 라이브러리에 대한 사용법을 설명한 것이고 본 포스팅은 외부 인프라스트럭처를 효율적으로 관리하는 방법론에 대한 개인적인 생각을 정리했습니다. 외부 인프라의 대표적인 예가 HTTP Server를 호출하는 것이라서 설명을 그 기준으로 하는 것이고 다른 외부 인프라를 사용하더라도 해당 방법이 적절한 대한이 될 수 있습니다.


## 기본적인 HTTP Mocking 테스트 방법

```kotlin
class OrderService(
    private val productQueryService: ProductQueryService,
    private val exchangeRateClient: ExchangeRateClient
) {

    /**
     * 주문 객체를 영속화를 진행하고, 해당 금액에 맞는 환율 정보를 가져온다
     */
    fun order(
        productId: Long,
        orderDate: LocalDate,
        orderAmount: BigDecimal,
    ): String {
        val product = productQueryService.findById(productId)
        val exchangeRateResponse = exchangeRateClient.getExchangeRate(orderDate, "USD", "KRW")

        // 영속화
        val orderNumber = save(
            productId,
            exchangeRateResponse.amount,
        )

        return orderNumber
    }
}

class ExchangeRateClient(
   ...
) {

    fun getExchangeRate(
        targetDate: LocalDate,
        currencyForm: String,
        currencyTo: String,
    ) =

        /**
         * 외부 HTTP Server에 질의하여 환율 정보를 가져온다.
         */
        "$host//exchange-rate"
            .httpGet(
                parameters = listOf(
                    "targetDate" to targetDate,
                    "currencyForm" to currencyForm,
                    "currencyTo" to currencyTo
                )
            )
            .response()
            .first.responseObject<ExchangeRateResponse>(objectMapper)
            .third.get()
}

data class ExchangeRateResponse(
    val amount: BigDecimal
)
```

로직은 상품을 주문 시 금액을 환율을 조회하여 해당 환율로 주문 정보를 저장하는 것입니다. 로직에 대한 테스트 코드는 다음과 같습니다.

```kotlin
@Test
internal fun `환율 정보 기반으로 주문 금액 계산하여 주문 생성`() {
    //given
    val responseBody = """
                    {
                      "amount": "140000"
                    }
                """.trimIndent()
    mockServer.`when`(
        HttpRequest.request()
            .withMethod("GET")
            .withPath("/exchange-rate")
            .withPathParameters(
                listOf(
                    Parameter.param("targetDate", "2022-02-02"),
                    Parameter.param("currencyForm", "USD"),
                    Parameter.param("currencyTo", "KRW")
                )
            )

    ).respond(
        HttpResponse.response()
            .withBody(responseBody)
            .withStatusCode(200)
    )

    //when
    val orderNumber = orderService.order(1L, LocalDate.of(2022, 2, 2), 100.toBigDecimal())

    //then
    val findOrder = orderQueryService.findOrderNumber(orderNumber)
    then(findOrder.amount).isEqualByComparingTo(140000.toBigDecimal())
}
```

로컬 환경에 Mock Server를 띄워 Given 절에 미리 요청/응답을 정의한 것으로 동작하게 합니다.

## 문제점

### 다양한 케이스에한 커버리지

물론 위처럼 정말 간단한 코드는 위 형식처럼 작성하는 것이 적절한 해결법이 될 수 있다고 생각합니다. 하지만 로직이 복잡하여 다양한 케이스에 대한 커버리지를 확보하기 위해서는 어려움이 있습니다. `20220202` 기준일 이외의 테스트 시 Given 절에 해당하는 요청/응답을 미리 정의해야 합니다. 1개의 객체를 처리하는 것은 어렵지 않으나 여러 객체를 처리하기 위해서는 어려움이 있습니다. 그리고 `getExchangeRate()` 메서드를 사용하는 모든 구간에서 동일하게 Mocking을 진행해야 합니다.


### 테스트의 관심사

`order()`메서드의 주 관심사는 주문을 객체를 환율 정보를 기반으로 영속화하는 것입니다. 그렇다는 것은 `order()` 테스트하는 관점도 해당 관점에서 진행해야 한다고 생각합니다. **즉 HTTP 통신의 관심사는 아니라고 생각하며 테스트 코드는 해당 관심사에 맞는 테스트를 진행해야 한다고 생각합니다.** 


## 해결 방법 Interface 

결과적으로 다양한 케이스를 커버하기 위해서는 실제 Mock 서버로 커버하는 것은 비효율적이고 무엇보다 중요한 것은 **order라는 관점에서 실제 외부 HTTP 통신이 큰 관심사가 아니라고 생각합니다.** 그렇다면 Mocking을 진행한다면 HTTP 통신에 대한 Mocking을 하는 것보다 객체 자체를 Mocking 하는 것이 더 좋다고 생각합니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/tesing/demo/images/http-mock.png)

Interface를 두고 실제 HTTP 통신을 하는 실제 구현체는 ExchangeRateClientImpl에서 진행하고, 테스트 코드 또는 일부 환경에서 구현체는 ExchangeRateClientMock을 사용하게 합니다. 

```kotlin
@Configuration
class AppConfiguration {
    @Bean
    @Profile("production | sandbox") // 특정 환경에서만 등록 하는 경우
//    @Profile("!test") // 특정 환경만 제외하는 경우
    fun exchangeRateClient() = ExchangeRateClientImpl()
}

interface ExchangeRateClient {
    fun getExchangeRate(
        targetDate: LocalDate,
        currencyForm: String,
        currencyTo: String,
    ): ExchangeRateResponse
}

class ExchangeRateClientImpl : ExchangeRateClient {
    

    override fun getExchangeRate(
        targetDate: LocalDate,
        currencyForm: String,
        currencyTo: String,
    ) =
        ...
        ...
}
```
`@Configuration`을 통해서 Bean으로 등록할 실제 구현 객체를 작성합니다. 이때 `@Profile`을 통해서 특정 환경에서만 등록시킬지, 특정 환경에서만 제외할지 프로젝트에 특성에 맞게 조절하면 됩니다. 다음으로는 테스트에서 사용할 Mock 구현체로 테스트에서만 사용하는 것이라고 하면 test scope에 위치 시켜 실제 동작하는 코드 환경과 분리시키는 것이 바람직합니다.

```
└── src
    ├── main
    │   ├── kotlin
    │   │   └── com
    │   │       └── example
    │   │           └── demo
    │   │               └── ExchangeRateClientImpl.kt
    │   └── resources
    │       └── application.yml
    └── test
        └── kotlin
            └── com
                └── example
                    └── demo
                        └── ExchangeRateClientMock.kt
```
위 경로처럼 test scope에서 ExchangeRateClientMock 객체를 위치 시켜 **테스트 환경 외에는 해당 객체에 접근 못하도록 합니다.**

```kotlin
@TestConfiguration
class TestSupport {

    @Bean
    @Profile("test")
    fun exchangeRateClient() = ExchangeRateClientMock()
}


class ExchangeRateClientMock : ExchangeRateClient {

    override fun getExchangeRate(targetDate: LocalDate, currencyForm: String, currencyTo: String): ExchangeRateResponse {
        return when (targetDate) {
            LocalDate.of(2022, 2, 2) -> ExchangeRateResponse(12000.12.toBigDecimal())
            LocalDate.of(2022, 2, 3) -> ExchangeRateResponse(13000.12.toBigDecimal())
            LocalDate.of(2022, 2, 4) -> ExchangeRateResponse(14000.12.toBigDecimal())
            else -> ExchangeRateResponse(15000.12.toBigDecimal())
        }
    }
}
```

테스트 환경에서 사용할 설정 클래스인 `@TestConfiguration` 객체를 만들고 테스트 환경에서만 Bean이 등록 가능하도록 설정합니다. 그리고 실제 Mocking의 Given 절에 해당하는 코드를 작성합니다. 즉 20220202 날짜의 경우 USD > KRW 환율이 12000으로 내려오게 설정합니다. order에 관심사는 해날 날짜에 환율 정보 기반으로 주문 금액이 정상적으로 영속화의 여부이기 때문에 미리 정의된 환율 정보 기반으로 정상적으로 금액이 잘 되었는지 검증하는 Then 절에 해당하는 테스트 코드를 작성합니다. Mock 구현체를 test 스코프에 위치 시켰지만 일반 main 위치 시키는 경우도 있습니다. 예를 사용해야 하는 외부 인프라가 특정 환경을 갖추지 못하는 경우, 외부 호출 경우 비용이 추가적으로 발생하는 경우 등등 이런 경우에서는 main 클래스에 위치 시키고 해당 환경에서 Mock 객체를 동작 시키는 것도 가능합니다. 물론 Mockito와 같은 테스트 도구를 이용하면 위와 같은 비슷한 방법으로 테스트가 가능합니다. 하지만 몇 가지 문제점들과 실제 객체를 Mockg 하는 방법이 더 효율적이라고 생각하여 위에서 설명한 방법으로 외부 인프라스트럭처 테스트를 진행합니다.

1. 특정 환경에서 다양한 이유로 해당 외부 인프라스트럭처를 이용하지 못하는 경우 Mock 객체로 갈아 끼우는 게 편리하다.
2. Mockito를 사용하면 단위 테스트 개념으로 실제 order가 영속화되었고 그 영속 화가 끝난 이후 데이터를 조회하여 실제로 원하는 형식으로 들어갔는지 테스트까지 어려우며, 내부 인프라에 대한 테스트가 또 필요 한 경우 거의 비슷한 테스트를 또 진행해야 합니다.
3. `@Mockbean`을 사용하는 경우 스프링 빈 컨텍스트가 n 번 올라가기 때문에 속도적인 측면에서 많은 손해가 발생한다.
4. 외부 인프라를 의존하는 코드들에 모든 테스트에 Mockito와 같은 Given 절을 작성해야 합니다. 이는 HTTP Server를 Mocking 테스트와 동일하게 발생합니다.
5. 다양한 케이스에 대해서 애플리케이션 로직으로 처리가 가능합니다. ExchangeRateClientMock 객체의 getExchangeRate 메서드는 정해진 날짜 외에는 `15000.12`이 응답하게 했습니다. 이처럼 다양한 케이스에 대해서 추가적으로 애플리케이션 단에서 직관적으로 추가 및 변경이 가능합니다.

이러한 이유들로 저는 Mockito와 같은 테스트 도구를 사용하지 않고 실제 Mock 객체를 직접 정의하여 사용합니다.

## 그렇다면 Mocking은 불필요한 것일까?

그렇다고 HTTP Server를 Mocking 하여 테스트하는 것은 의미가 없다고 할 수는 없습니다. 결국 중요한 것은 테스트의 관심사입니다. order 입장에서는 해당 행위가 큰 관심사가 아닐 수 있겠지만 `getExchangeRate()`메서드에서는 중요한 관점입니다. **즉 해당 테스트의 중요 관심사라면 Mocking 하여 테스트하는 것이 바람직합니다.** `getExchangeRate()` 관심사는 요청/응답입니다. 즉 요청한 HTTP 파라미터들이 미리 정의된 값으로 나갔고 해당 요청에 따른 응답이 오면 그 응답을 적절하게 deserialize 하여 자신을 호출한 객체로 넘겨 줄 수 있는 지입니다. 

```kotlin
@Test
internal fun `환율 정보 HTTP 통신 테스트`() {
    //given
    val responseBody = """
                {
                  "amount": "140000"
                }
            """.trimIndent()
    
    mockServer.`when`(
        HttpRequest.request()
            .withMethod("GET")
            .withPath("/exchange-rate")
            .withPathParameters(
                listOf(
                    Parameter.param("targetDate", "2022-02-02"),
                    Parameter.param("currencyForm", "USD"),
                    Parameter.param("currencyTo", "KRW")
                )
            )

    ).respond(
        HttpResponse.response()
            .withBody(responseBody)
            .withStatusCode(200)
    )

    //when

    val clientImpl = ExchangeRateClientImpl()
    clientImpl.getExchangeRate(
        targetDate = LocalDate.of(2022, 2, 2),
        currencyForm = "USD",
        currencyTo = "KRW",
    )

}
```

이러한 관심사에 맞게 Givin 절에서 요청과 응답을 미리 정의하고 `getExchangeRate()` 메서드를 호출하면 Json 응답을 deserialize 하여 객체로 전달이 잘 되는지에 대한 관심사에 대해서 테스트 코드를 작성합니다. 외부 인프라에 대한 테스트도 마찬가지로 책임과 역할을 명확하게 구분하고 분리해야 한다고 생각합니다. **그렇지 않으면 특정 객체에 책임이 과중되고 결국 전체적인 설계 디자인이 좋지 않게 됩니다. 이러한 현상(냄새)를 가장 빠르게 눈치챌 수 있는 게 테스트 코드라고 생각합니다.** 테스트 코드를 작성하다 보면 너무 많은 의존성이 필요 해지고 내가 검증하려는 관심사 외에도 다른 관심사에도 관여하게 되는 경우 문제가 있다고 빠른 피드백을 줄 수 있습니다. 저는 이것이 테스트 코드의 아주 큰 장점 중에 하나라고 생각합니다.

```kotlin
override fun getExchangeRate(
        targetDate: LocalDate,
        currencyForm: String,
        currencyTo: String,
): ExchangeRateResponse {
    val response = "http://localhost:8080/exchange-rate"
        .httpGet(
            parameters = listOf(
                "targetDate" to targetDate,
                "currencyForm" to currencyForm,
                "currencyTo" to currencyTo
            )
        )
        .response()

    if (response.second.statusCode / 100 != 2) {
        // HTTP Status Code 2xx 아닌 경우는 어떻게 
        throw IllegalStateException("HTTP Status Code: ${response.second.statusCode} ")
    }
    
    return response
        .first.responseObject<ExchangeRateResponse>(objectMapper)
        .third.get()
}
```

`getExchangeRate()` 메서드에서 2xx 응답이 아닌 경우 예외를 발생시키는 코드를 추가했다고 가정하면 해당 메서드를 사용하는 오류가 발생해도 다음 로직을 이어 가야 하는 경우 try catch 묶는 해당 코드에서 예외 처리를 리팩토링 해야 합니다. 이러한 현상도 결국 역할과 책임이 과중 됐다고 생각합니다. `getExchangeRate()` 메서드는 HTTP 통신으로 요청/응답에 대한 역할과 책임을 갖고 그 부분만 충실하게 하면 됩니다. 예외 처리에 대한 특히 비즈니스 로직에 의한 핸들링은 관여하지 않는 것이 좋은 설계라고 생각합니다.

### 책임, 역활 분리

[HTTP Client 책임 분리하기](https://cheese10yun.github.io/httpclient/)에서 포스팅한 적 있듯 내부, 외부 인프라스트럭처는 추상화 단계를 가지며 그 단계에서 본인의 역할과 책임에 대해서 성실하게 다 하는 것이 좋다고 생각합니다. 

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/msa-error-response/docs/http-client-layar.png)

JPA를 사용하는 것으로 예를 들면 JpaRepository 영역은 ExchangeRateClient과 동일한 영역이라고 생각합니다. 특정 키값으로 조회하여 없는 경우 같은 비즈니스 영역의 책임을 JpaRepository에서 진행하지 않고 우리 비즈니스 로직을 풀어내는 서비스 영역에서 진행하는 것과 마찬가지로 HTTP 외부 통신도 동일하게 해당 요청/응답에 관한 부분만 책임을 부여하고 그 외의 영역에서는 다른 서비스 객체를 만들고 그 객체에서 핸들링하는 것이 좋다고 생각합니다. 결과적으로 테스트 커버리지를 넓혀 다양한 케이스에 대한 테스트 코드를 작성하기 위해서는 객체 간에 역할 책임을 명확하게 구분하고 본인의 영역에 대해서는 충실하게 수행하는 코드가 동반되어야 가능하다고 생각합니다.


## 정리

해당 테스트 방법이 대부분의 환경에서 적절한 대안이 될 수 있다고는 생각하지 않습니다. 그래도 제가 경험한 환경에서는 외부 인프라스트럭처에 대한 테스트에 대한 코드를 작성할 때는 하고자 하는 테스트의 중요 관심사가 아닌 경우 불필요한 Mocking에 많은 코드를 작성하는 것이 효율적이지 못하다고 생각했습니다. 그리고 무엇보다 중요하는 것은 어떤 계층을 어떻게 바라보고 어떻게 테스트해야 할 것인지에 대한 팀 내부 차원의 합의된 부분이 먼저 선행되어야 한다고 생각합니다. 다양한 테스트 기법을 공부하는 것도 좋지만 프로젝트로 일을 진행하고 있다면 팀 내에서 토론하여 최소한의 합의를 먼저 구하고 테스트에 대한 기법을 적용하는 것이 더 좋을 거 같습니다.