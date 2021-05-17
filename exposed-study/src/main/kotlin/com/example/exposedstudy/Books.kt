package com.example.exposedstudy

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.math.BigDecimal

object Books : LongIdTable("book") {
    val writer = reference("writer_id", Writers)
    val title = varchar("title", 150)
    val price = decimal("price", 10, 4)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class Book(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Book>(Books)

    var writer by Writer referencedOn Books.writer
    var title by Books.title
    var price by Books.price
    var createdAt by Books.createdAt
    var updatedAt by Books.updatedAt
}

object Writers : LongIdTable("writer") {
    val name = varchar("name", 150)
    val email = varchar("email", 150)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class Writer(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Writer>(Writers)

    var name by Writers.name
    var email by Writers.email
    var createdAt by Writers.createdAt
    var updatedAt by Writers.updatedAt

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