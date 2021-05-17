package com.example.exposedstudy

import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@SpringBootTest
@Transactional
class ExposedDaoTest {

    @Test
    fun create() {
        val writer = Writer.new {
            name = "name"
            email = "ad@ad.com"
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
        }

        println(writer)
    }

    @Test
    fun read() {

        val all = Writer.all()

        insertWriter("name", "asd@asd.com")

        Writer.find { Writers.name eq "name" }
            .forEach {
                println(it)
            }
    }

    private fun batchInsertBook(data: List<Int> = (1..10).map { it }) {
        Books.batchInsert(
            data
        ) {
            this[Books.writer] = 1L
            this[Books.title] = "$it-title"
            this[Books.price] = it.toBigDecimal()
            this[Books.createdAt] = LocalDateTime.now()
            this[Books.updatedAt] = LocalDateTime.now()
        }
    }

    private fun insertBook(
        title: String,
        price: BigDecimal,
        writerId: Long = 1L
    ) = Books.insert { book ->
        book[this.writer] = writerId
        book[this.title] = title
        book[this.price] = price
        book[this.createdAt] = LocalDateTime.now()
        book[this.updatedAt] = LocalDateTime.now()
    }

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