package com.example.boot3mongo.order

import com.example.boot3mongo.mongo.Auditable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "orders")
data class Order(
    @Indexed(unique = true)
    @Field(name = "order_id")
    val orderId: String,

    @Field(name = "order_date")
    val orderDate: LocalDateTime,

    @Field(name = "product_name")
    val productName: String,

    @Field(name = "product_price")
    val productPrice: BigDecimal,

    @Field(name = "shipping_address")
    val shippingAddress: String,

    @Field(name = "order_status")
    val orderStatus: OrderStatus,

    @Field(name = "payment_method")
    val paymentMethod: String,

    @Field(name = "member_id")
    val memberId: String,

    @Field(name = "delivery_date")
    val deliveryDate: LocalDate,

    @Field(name = "quantity")
    val quantity: Int

) : Auditable()


enum class OrderStatus {
    PENDING, SHIPPED, DELIVERED, CANCELLED
}