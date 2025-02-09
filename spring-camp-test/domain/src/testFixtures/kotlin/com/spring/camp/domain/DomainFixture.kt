package com.spring.camp.domain

import java.math.BigDecimal
import java.time.LocalDate

object DomainFixture {

    fun product(): Product {
        return Product(
            productName = "Savannah Hanson",
            category = "facilis",
            brand = "nonumy",
            price = 100.toBigDecimal(),
            discountRate = 0.toBigDecimal(),
            currency = "KRW",
            isActive = true,
            region = "North America",
            description = "Initial price record for Sample Product",
            cost = 0.toBigDecimal(),
            supplier = "Acme Supplier",
            taxRate = 0.toBigDecimal(),
            unit = "persius",
            additionalFee = 0.toBigDecimal()

        )
    }

    fun productHistory(
        productId: Long = 1L,
        productName: String = "Sample Product",
        category: String = "Electronics",
        brand: String = "Acme",
        price: BigDecimal = 1_000.toBigDecimal(),
        discountRate: BigDecimal = 0.toBigDecimal(),
        effectiveStartDate: LocalDate = LocalDate.of(2024, 1, 1),
        effectiveEndDate: LocalDate = LocalDate.of(2025, 1, 1),
        currency: String = "KRW",
        isActive: Boolean = false,
        region: String = "North America",
        description: String? = "Initial price record for Sample Product",
        cost: BigDecimal? = BigDecimal("250.00"),
        supplier: String? = "Acme Supplier",
        taxRate: BigDecimal? = BigDecimal("0.08"),
        unit: String = "piece",
        additionalFee: BigDecimal = 10.toBigDecimal(),
    ): ProductHistory {
        return ProductHistory(
            productId = productId,
            productName = productName,
            category = category,
            brand = brand,
            price = price,
            discountRate = discountRate,
            effectiveStartDate = effectiveStartDate,
            effectiveEndDate = effectiveEndDate,
            currency = currency,
            isActive = isActive,
            region = region,
            description = description,
            cost = cost,
            supplier = supplier,
            taxRate = taxRate,
            unit = unit,
            additionalFee = additionalFee,
        )
    }

    fun order(
        customerName: String = "홍길동",
        orderDate: LocalDate = LocalDate.of(2024, 4, 4),
        totalAmount: Double = 8.9,
        status: String = "READY",
        shippingAddress: String = "서울 특별시 xxx 구, xxx로 123",
        billingAddress: String = "서울 특별시 xxx 구, xxx로 123",
        paymentMethod: String = "CARD",
        shippingCost: Double = 10.11,
        taxAmount: Double = 12.13,
        discount: Double = 14.15,
        notes: String? = null,
    ): Order {
        return Order(
            customerName = customerName,
            orderDate = orderDate,
            totalAmount = totalAmount,
            status = status,
            shippingAddress = shippingAddress,
            billingAddress = billingAddress,
            paymentMethod = paymentMethod,
            shippingCost = shippingCost,
            taxAmount = taxAmount,
            discount = discount,
            notes = notes,
        )
    }

    fun coupon(
        code: String = "COUPON_CODE",
        discount: Double = 10.0,
//        order: Order
    ): Coupon {
        return Coupon(
            code = code,
            id = null
        )
    }
}


data class Coupon(
    val id: Long? = null,
    val code: String
)