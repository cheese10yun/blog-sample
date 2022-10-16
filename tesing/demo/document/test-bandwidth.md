# 테스트 대역폭 늘리기

다양한 케이스에 대한 테스트 대역폭을 늘려서 테스트 코드를 작성하는 것은 중요한 작업입니다. 로직이 복잡하고 다양한 케이스에 대응하는 코드가 있다면 이러한 테스트 대역 폭은 더욱 중요합니다. 본 포스팅은 다양한 케이스에 대한 커버리지를 높이는 방법에 대한 방법에 관한 내용입니다.

## Sample Code

```kotlin
@Service
class OrderService(
    private val productQueryService: ProductQueryService,
    private val exchangeRateClientImpl: ExchangeRateClientImpl,
    private val couponQueryService: CouponQueryService,
    private val shopQueryService: ShopQueryService
) {

    fun order(
        productId: Long,
        orderDate: LocalDate,
        orderAmount: BigDecimal,
        shopId: Long,
        couponCode: String?
    ): String {
        // 상품 정보는 Elasticsearch에서 조회 
        val product = productQueryService.findById(productId)
        // 환율 정보는 Redis에서 조회 
        val exchangeRateResponse = exchangeRateClientImpl.getExchangeRate(orderDate, "USD", "KRW")
        // 쿠폰 정보는 MySql에서 조회
        val coupon = couponQueryService.findByCode(couponCode)
        // 가맹점 정보는 MySql에서 조회
        val shop = shopQueryService.findById(shopId)

        /**
         * 복잡한 로직...
         * 1. 상품 정보 조회 하여 금액 및 상품 재고 확인, 재고가 없는 경우 예외 처리 등등
         * 2. 환율 정보 조회 하여 특정 국가 환율로 계산
         * 3. 쿠폰 정보 조회하여 적용 가능한 상품인지 확인, 가맹점과 할인 금액 부담 비율 등등 계산
         * 4. 가맹점 정보 조회하여 수수료 정보등 조회
         */

        // 영속화
        val orderNumber = save(
            productId,
            exchangeRateResponse.amount,
        )

        return orderNumber
    }
}
```
주문에 대한 sample code가 위처럼 작성되어 있는 경우 적어도 10~20개의 시나리오에 대한 테스트는 필요하다고 생각합니다. 하지만 해당 테스트 코드를 작성하기 위해서는 많은 어려움이 있습니다. 서비스 구조가 커지면 특정 문제를 해결하기 위한 다양한 인프라를 갖추게 됩니다. 위 코드도 각 데이터 특성에 맞는 저장소에 저장하고 조회하고 있습니다. 이런 경우 테스트 코드를 작성하기 위해서는 Given 절에 해당하는 곳에서 해당 인프라에 대한 데이터 셋업이 반드시 필요합니다. **이 작업의 어려움 때문에 다양한 케이스의 테스트 코드 작성이 어렵다고 생각합니다.**


## 중요한 것은 관심사

테스트 코드를 작성하는 것은 해당 코드의 주요 관심사에 대한 테스트 코드를 작성하는 것입니다. 그렇다면 위 코드의 중요 관심사는 각각의 인프라에서의 조회, `복잡한 로직...`에의 데이터 처리입니다. 두 가지 관심사에 대한 테스트 코드를 작성해야 하고 그 다양한 테스트 케이스에 대한 커버를 해야 하기 때문에 어려움이 있는 것입니다. 물론 중요 관심사라는 것은 대부분 명확하게 나눠지지 않고 항상 애매합니다. 로직이 복잡할수록 이러한 현상이 나타납니다. 그러기 때문에 테스트 코드를 작성하다 보면 설계의 경계선에 대한 피드백을 받을 수 있다는 점이 매우 유용한 장점이라고 생각합니다.


### 각각의 인프라 조회 관심사

```kotlin
@Service
class OrderService(
    private val productQueryService: ProductQueryService,
    private val exchangeRateClientImpl: ExchangeRateClientImpl,
    private val couponQueryService: CouponQueryService,
    private val shopQueryService: ShopQueryService
) {

    fun order(
        productId: Long,
        orderDate: LocalDate,
        orderAmount: BigDecimal,
        shopId: Long,
        couponCode: String?
    ): String {
        // 상품 정보는 Elasticsearch에서 조회
        val product = productQueryService.findById(productId)
        // 환율 정보는 Redis에서 조회
        val exchangeRateResponse = exchangeRateClientImpl.getExchangeRate(orderDate, "USD", "KRW")
        // 쿠폰 정보는 MySql에서 조회
        val coupon = couponQueryService.findByCode(couponCode)
        // 가맹점 정보는 MySql에서 조회
        val shop = shopQueryService.findById(shopId)
        

        // 복잡한 로직... OrderServiceSupport 객체로 위임
        val order = OrderServiceSupport().order(...)

        val order = save(order)

        return order.orderNumber
    }
}
```

`복잡한 로직...`관련 로직을 OrderServiceSupport 객체로 위임합니다. 해당 코드의 주관심사는 다양한 인프라에 해당하는 조회를 정상적으로 진행했는지에 대한 코드를 작성합니다. 각각의 인프라 조회는 해당 서비스 로직에서 주요 관심사로 보고 테스트를 진행하기 때문에 order에서는 중복적인 테스트는 불필요하다고 생각하며 몇 가지 간단한 케이스에 대해서 Order 객체가 영속 화가 알맞게 되었는지 검증하는 테스트 코드를 작성합니다. 

### 복잡한 로직(데이터 처리) 관심사

```kotlin
/**
 * Spring Bean Context와 인프라스트럭처의 관련 코드가 없는 순수한 POJO
 */
class OrderServiceSupport {

    /**
     * 각각의 인프라의 조회 책임을 위임 하여 복잡한 로직 작성... 에대한 관심사만 갖는다.
     */
    fun order(
        product: Product,
        orderDate: LocalDate,
        orderAmount: BigDecimal,
        exchangeRateResponse: ExchangeRateResponse,
        shop: Shop,
        coupon: Coupon?,
    ): Order {

        /**
         * 복잡한 로직...
         * 1. 상품 정보 조회 하여 금액 및 상품 재고 확인, 재고가 없는 경우 예외 처리 등등
         * 2. 환율 정보 조회 하여 특정 국가 환율로 계산
         * 3. 쿠폰 정보 조회하여 적용 가능한 상품인지 확인, 가맹점과 할인 금액 부담 비율 등등 계산
         * 4. 가맹점 정보 조회하여 수수료 정보등 조회
         */
        return Order(
            ...
        )
    }
}

internal class OrderServiceSupportTest {

    @Test
    internal fun `쿠폰 적용 없는 주문 생성`() {
        //given
        // 순수하게 POJO 기반으로 데이터를 만들고 테스트를 진행하기 때문에 자연스럽게 비즈니스 로직에만 집중 할 수 있습니다.
        val product = Product(
            productId = ...,
            amount = ...,
            currency = ...
        )
        val orderDate = LocalDate.of(2022, 2, 2)
        val orderAmount = 100.toBigDecimal()
        val exchangeRateResponse = ExchangeRateResponse(
            1222.12.toBigDecimal()
        )
        val shop = Shop(
            feeRate = 0.023.toBigDecimal()
        )

        //when
        val order = OrderServiceSupport().order(
            product = product,
            orderDate = orderDate,
            orderAmount = orderAmount,
            exchangeRateResponse = exchangeRateResponse,
            shop = shop,
            coupon = null,
        )

        //then
        // 복잡한 로직..에 대한 검증
    }
}
```
복잡한 로직(데이터 처리) 관심사만 갖는 객체를 만들어 해당 데이터에 대한 처리를 진행합니다. **이때 해당 객체는 Spring Bean Context 및 인프라스트럭처의 관련 코드가 없이 동작하는 순수한 POJO 객체로 만들어 데이터 처리를 진행합니다.** Given 절에서 외부 환경에 대한 의존성이 없이 다양한 테스트 케이스를 쉽게 작성할 수 있으며 인프라 및 기타 환경에 구애받지 않기 때문에 비즈니스 로직의 관심사에만 자연스럽게 집중할 수 있게 됩니다.
 