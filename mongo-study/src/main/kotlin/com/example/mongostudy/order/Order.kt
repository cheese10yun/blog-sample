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

) : Auditable()

enum class OrderStatus {
    PENDING, SHIPPED, DELIVERED, CANCELLED
}

interface OrderRepository : MongoRepository<Order, ObjectId>, OrderCustomRepository

interface OrderCustomRepository

class OrderCustomRepositoryImpl(private val mongoTemplate: MongoTemplate) : OrderCustomRepository