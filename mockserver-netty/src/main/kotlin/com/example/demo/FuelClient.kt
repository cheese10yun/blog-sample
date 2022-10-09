package com.example.demo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.jackson.responseObject
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


class OrderQueryService() {

    fun findOrderNumber(orderNumber: String): Order {

        return Order(
            amount = 123.toBigDecimal()
        )
    }
}

class OrderService(
    private val productQueryService: ProductQueryService,
    private val exchangeRateClientImpl: ExchangeRateClientImpl
) {

    fun order(
        productId: Long,
        orderDate: LocalDate,
        orderAmount: BigDecimal,
    ): String {
        val product = productQueryService.findById(productId)
        val exchangeRateResponse = exchangeRateClientImpl.getExchangeRate(orderDate, "USD", "KRW")

        // 영속화
        val orderNumber = save(
            productId,
            exchangeRateResponse.amount,
        )

        return orderNumber
    }

    private fun save(productId: Long, amount: BigDecimal): String {

        return "order-number"
    }
}

data class Order(
    val amount: BigDecimal
) {

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

