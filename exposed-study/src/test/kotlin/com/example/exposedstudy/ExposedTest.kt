package com.example.exposedstudy

import java.math.BigDecimal
import java.time.LocalDateTime
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.junit.jupiter.api.Test

class ExposedTest: ExposedTestSupport() {

    @Test
    internal fun `book insert`() {

        val insert = Books.insert { book ->
            book[this.writer] = insertWriter("asd", "asd")[Writers.id]
            book[this.title] = "test"
            book[this.price] = 1000.toBigDecimal()
            book[this.status] = BookStatus.NONE
            book[this.createdAt] = LocalDateTime.now()
            book[this.updatedAt] = LocalDateTime.now()
        }

        val toList = Books.selectAll().forEach {
            Book.
        }

    }


//    private fun insertBook(
//        title: String,
//        price: BigDecimal,
//        writerId: Long = 1L
//    ) =
    private fun insertWriter(
        name: String,
        email: String
    ) = Writers.insert { writer ->
        writer[this.name] = name
        writer[this.email] = email
        writer[this.createdAt] = LocalDateTime.now()
        writer[this.updatedAt] = LocalDateTime.now()
    }
}