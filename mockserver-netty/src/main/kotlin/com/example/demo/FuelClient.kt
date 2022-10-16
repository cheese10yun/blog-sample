package com.example.demo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.jackson.responseObject
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

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

data class ExchangeRateResponse(
    val amount: BigDecimal
)


@Service
class OrderQueryService() {

    fun findOrderNumber(orderNumber: String): Order {

        return Order(
            amount = 123.toBigDecimal()
        )
    }
}

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

//        // 영속화
//        val orderNumber = save(
//            productId,
//            exchangeRateResponse.amount,
//        )

        // 복잡한 로직...위임
        val order = OrderServiceSupport().order(
            product = product,
            orderDate = orderDate,
            orderAmount = orderAmount,
            exchangeRateResponse = exchangeRateResponse,
            shop = shop,
            coupon = coupon
        )

        val order = save(order)

        return order.orderNumber
    }

    private fun save(productId: Long, amount: BigDecimal): String {

        return "order-number"
    }
}

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
            amount = 123.toBigDecimal()
        )
    }
}

class CouponQueryService {

    fun findByCode(code: String): Coupon {
        return Coupon(
            discountAmount = BigDecimal.TEN,
            expiryDate = LocalDate.of(2022, 2, 2),
        )
    }
}

class ShopQueryService {

    fun findById(id: Long): Shop {
        return Shop(
            feeRate = 0.03.toBigDecimal()
        )
    }
}

data class Shop(
    val feeRate: BigDecimal
)

data class Coupon(
    val discountAmount: BigDecimal,
    val expiryDate: LocalDate
)

/**
 *
 */
data class Order(
    val amount: BigDecimal
) {
    var status: OrderStatus

    init {
        this.status = OrderStatus.READY
    }

    fun changeStatusShipping() {
        // 방어 적인 로직
        this.status = OrderStatus.READY
    }
}

enum class OrderStatus(val desc: String) {
    READY("준비"),
    DELIVERING("준비"),
    DELIVERY_COMPLETED("준비")
}

data class Product(
    val productId: Long,
    val amount: BigDecimal,
    val currency: String
)

interface ProductQueryService {

    fun findById(productId: Long): Product {
        // 로직으로 조회
        return Product(
            productId = 1L,
            amount = 100.toBigDecimal(),
            currency = "USD"
        )
    }
}

