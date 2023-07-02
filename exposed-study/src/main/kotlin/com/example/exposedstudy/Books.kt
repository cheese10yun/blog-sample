package com.example.exposedstudy

import java.math.BigDecimal
import java.time.LocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object Books : LongIdTable("book") {
    val writer = reference("writer_id", Writers)
    val title = varchar("title", 150)
    val status = enumerationByName("status", 150, BookStatus::class)
    val price = decimal("price", 10, 4)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

enum class BookStatus {
    READY,
    NONE
}

class Book(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Book>(Books)

    var writer by Writer referencedOn Books.writer
    var title by Books.title
    var status by Books.status
    var price by Books.price
    var createdAt by Books.createdAt
    var updatedAt by Books.updatedAt

    override fun toString(): String {
        return "Book(writer=$writer, title='$title', price=$price, createdAt=$createdAt, updatedAt=$updatedAt)"
    }
}

object Writers : LongIdTable("writer") {
//    val name = varchar("name", 150).nullable()
    val name = registerColumn<String>(
        name = "name",
        type = TrimmingWhitespaceConverterColumnType(length = 150)
    ).nullable()

    val email = varchar("email", 150)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

object Publishers: LongIdTable("publisher") {
    val writerId = long("writer_id")
    val corpName = varchar("corp_name", 150)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

class Writer(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Writer>(Writers)

    var name by Writers.name
//        private set
    var email by Writers.email
//        private set
    var createdAt by Writers.createdAt
//        private set
    var updatedAt by Writers.updatedAt
//        private set

    fun instance(name: String, email: String){
        this.name = name
        this.email = email
    }

    fun updateProfile(name: String, email: String) {
        this.name = name
        this.email = email
        this.updatedAt = LocalDateTime.now()
    }

    fun register(name: String, email: String) {
        this.name = name
        this.email = email
        this.updatedAt = LocalDateTime.now()
        this.createdAt = LocalDateTime.now()
    }

    override fun toString(): String {
        return "Writer(name='$name', email='$email', createdAt=$createdAt, updatedAt=$updatedAt)"
    }
}

data class BookWithWriter(
    val title: String,
    val price: BigDecimal,
    val email: String,
    val name: String
)

object Orders : LongIdTable(name = "orders") {
    val amount = decimal("amount", 19, 4)
    val name = varchar("name", 100)
}

object Payments : LongIdTable(name = "payment") {
    val orderId = long("order_id")
    val amount = decimal("amount", 19, 4)
}

class Payment(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Payment>(Payments)

    var amount by Payments.amount
    var orderId by Payments.orderId

    override fun toString(): String {
        return "Payment(amount=$amount, orderId=$orderId)"
    }
}