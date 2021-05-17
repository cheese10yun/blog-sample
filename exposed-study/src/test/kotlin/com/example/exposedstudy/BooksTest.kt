package com.example.exposedstudy

import org.jetbrains.exposed.sql.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@SpringBootTest
@Transactional
class BooksTest {

    @Test
    fun `create`() {
        val book = insertBook("name", BigDecimal.TEN, 1L)
        val bookId = book[Books.id].value
        println(bookId)
    }

    @Test
    fun `reader`() {
        insertBook("name", BigDecimal.TEN, 1L)

        Books
                .select { Books.title eq "name" }
                .forEach { book ->
                    println(book[Books.title])
                }
    }

    @Test
    fun `slice`() {
        insertBook("name", BigDecimal.TEN, 1L)

        val map = Books
                .slice(Books.id, Books.title)
                .selectAll().map {
                    it[Books.id] to it[Books.title]
                }

        for (pair in map) {
            println(pair)
        }
    }

    @Test
    fun `withDistinct`() {
        batchInsertBook((1..10).map { it })

        Books
                .slice(Books.price)
                .select { Books.price less 1000.toBigDecimal() }
                .withDistinct().map {
                    it[Books.price]
                }
    }


    @Test
    fun `update`() {
        insertBook("name", BigDecimal.TEN, 1L)

        Books.update(
                { Books.title eq "title" }
        ) {
            it[title] = "new-title"
        }
    }

    @Test
    fun `count`() {
        val data = (1..10).map { it }
        batchInsertBook(data)
        Books
                .slice(Books.price)
                .select { Books.price less 1000.toBigDecimal() }
                .withDistinct().map {
                    it[Books.price]
                }


        val count = Books
                .select { Books.title eq "title" }
                .count()

        println(count)
    }

    @Test
    fun `order by`() {
        batchInsertBook((1..10).map { it })

        Books
                .selectAll().orderBy(Books.price to SortOrder.DESC)
                .forEach {
                    println(it[Books.price])
                }
    }

    @Test
    fun `Group By`() {
        batchInsertBook((1..10).map { it })

        Books
                .slice(Books.id.count(), Books.title)
                .selectAll()
                .groupBy(Books.title)
                .forEach {
                    println(it[Books.id.count()])
                    println(it[Books.title])
                }
    }

    @Test
    fun limit() {
        val data = (1..10).map { it }
        batchInsertBook(data)

        Books
                .select { Books.title eq "title" }
                .limit(1, 10)
                .forEach {
                    println(it[Books.id])
                    println(it[Books.title])
                }
    }

    @Test
    fun `join`() {
        val writerId = insertWriter("yun", "yun@asd.com")[Writers.id].value
        (1..5).map {
            insertBook("$it-title", BigDecimal.TEN, writerId)
        }

        (Books innerJoin Writers)
                .slice(
                        Books.id,
                        Books.title,
                        Books.price,
                        Writers.name,
                        Writers.email,
                )
                .selectAll()
                .forEach {
                    println("bookId: ${it[Books.id]}, title: ${it[Books.title]}, writerName: ${it[Writers.name]}, writerEmail: ${it[Writers.email]}")
                }


    }

    private fun batchInsertBook(data: List<Int>) {
        Books.batchInsert(
                data
        ) {
            this[Books.writerId] = 1L
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
        book[this.writerId] = writerId
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