# 외부 인프라스트럭처를 테스트하는 자세

외부 인프라를 의존하는 로직의 경우 테스트 코드를 작성하기 어려운 부분이 있습니다. 예를 들어 특정 날짜 기준으로 환율 정보를 기반으로 금액을 계산 하고, 그 환율 정보를 외부 HTTP Server에 질의 한다고 하면 HTTP Server가 외부 인프라라에 해당 합니다. 이런 경우 대부분 Mocking을 하여 테스트 코드를 작성하게 됩니다. 이전 포스팅 [Mockserver Netty 사용해서 HTTP 통신 Mocking 하기](https://cheese10yun.github.io/mock-server-netty/)에서 다룬적이 있습니다. 해당 포스팅은 라이브러리에 대한 사용법을 설명한 것이고 본 포스팅은 외부 인프라스트럭처를 효율적으로 관리하는 방법론에 대한 개인적인 생각을 정리했습니다. 


## 기본적인 Mocking 테스트 방법

```kotlin
class OrderService(
    private val productQueryService: ProductQueryService,
    private val exchangeRateClient: ExchangeRateClient
) {

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

        "$host/sample"
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

로직은 상품을 주문시 금액을 환율을 조회하여 해당 환율로 주문 정보를 저장하는 것입니다. 로직에 대한 테스트 코드는 다음과 같습니다.

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
로컬 환경에 mock server를 띄워 given절에 작성한 요청/응답이 실제 동작하게 합니다. 다른 라이브러리도 대부분 동일하게 동작합니다.

## 문제점

물론 위 처럼 정말 간단한 코드는 위 형식 처럼 작성하는 것이 적절한 해결 법이 될 수 있다고 생각합니다. 하지만 로직이 복잡하고 테스트 대역폭을 넓펴 다양한 테스트를 진행하기 위해서는 어려움이 있습니다. `2022-02-02` 기준일로 조회 했지만 그 밖의 기준일의 조회에 어려움이 있습니다. 

예를 들어 배치 애플리케이션 처럼 다양한 데이터에 대한 처리에 대한 테스트 코드를 작성한다고 하면 요청과 응답에 대한 날짜에 대해서 mocking을 해야하며 이는 불편함을 넘어서 다양한 케이스에 대한 테스트 대역폭을 넓히는 것에 큰 어려움이 발생하게 됩니다.

## Interface를 기준으로 깔아 끼우자

결과적으로 다양한 케이스를 커버하기 위해서는 실제 Mock 서버 커버하는 것은 비효율적이고 무엇보다 중요한 것은 order라는 관점에서 실제 외부 HTTP 통신이 큰 관심사가 아니라고 생각합니다. 

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

![](https://camo.githubusercontent.com/84b48ecfaf7bbf0e48ce86d41bba8ee2725aebfee6162bea8df52fa4690ac323/68747470733a2f2f692e696d6775722e636f6d2f5a6b796b76396d2e706e67)

![](https://camo.githubusercontent.com/9ecf2ea623bd896bbf3440326d3f1e562672c575fa31e9ca33dcfe78b47a4263/68747470733a2f2f692e696d6775722e636f6d2f546447596c386e2e706e67)


## 그렇다면 Mocking은 불필요한 것일까?

외부 인프라스트럭처를 사용하는 경우라면 Mocking은 피할 수 없는 방법입니다. 우리가 직접적으로 컨트롤하지 못하는 영역에 대해서는 반드시 Mocking이 필요하게 됩니다. 

그렇다면 해당 테스트의 관심사에 대해서 생각해볼 필요가 있습니다. HTTP 통신을 하는 경우라면 관심사는 요청/응답 입니다. 

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
해당 관심사는 요청/응답 입니다. 해당 테스트에 대해서 더 디테일하게 보면 해당 파라미터에 대한 요청이 제대로 들어가고 응답을 받고 그 응답을 시리얼라이즈를 제대로 진행 사킬 수 있는지 관심사입니다.


```kotlin
@Test
internal fun `Mock 객체를 기반으로 테스트`() {
    //given
    
    //when
    val orderNumber = orderService.order(1L, LocalDate.of(2022, 2, 2), 100.toBigDecimal())

    //then
    val findOrder = orderQueryService.findOrderNumber(orderNumber)
    then(findOrder.amount).isEqualByComparingTo(140000.toBigDecimal())
}
```