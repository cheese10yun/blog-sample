package com.example.mongostudy.order

import com.example.mongostudy.mongo.Auditable
import com.example.mongostudy.mongo.MongoCustomRepositorySupport
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.repository.MongoRepository
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

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

interface OrderRepository : MongoRepository<Order, ObjectId>, OrderCustomRepository

interface OrderCustomRepository

class OrderCustomRepositoryImpl(mongoTemplate: MongoTemplate) : OrderCustomRepository, MongoCustomRepositorySupport<Order>(
    Order::class.java,
    mongoTemplate
)
