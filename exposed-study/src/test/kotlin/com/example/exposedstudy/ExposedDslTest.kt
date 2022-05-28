package com.example.exposedstudy

import java.math.BigDecimal
import java.time.LocalDateTime
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.junit.jupiter.api.Test

class ExposedDslTest : ExposedTestSupport() {

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
//                .slice(Books.id, Books.title)
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
    fun `delete`() {
        batchInsertBook()
        Books.deleteWhere { Books.id greaterEq 1L }
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

        // SELECT book.id, book.title, book.price, writer.`name`, writer.email FROM book INNER JOIN writer ON writer.id = book.writer_id
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
                it.fieldIndex
                println("bookId: ${it[Books.id]}, title: ${it[Books.title]}, writerName: ${it[Writers.name]}, writerEmail: ${it[Writers.email]}")
            }
    }

    @Test
    fun name() {
        insertWriter("yun", "yun@asd.com")

        val query = Writers.selectAll()
    }

    @Test
    fun `alias`() {
        val writerId = insertWriter("yun", "yun@asd.com")[Writers.id].value
        (1..5).map {
            insertBook("$it-title", BigDecimal.TEN, writerId)
        }

        val selectAll: Query = (Books innerJoin Writers)
            .slice(
                Books.id.alias("book_id"),
                Books.title,
                Books.price,
                Writers.id.alias("writer_id"),
                Writers.name,
                Writers.email,
            )
            .selectAll()

        println(selectAll)


    }

    @Test
    fun `batch insert`() {
        val data = (1..10).map { it }
        Books.batchInsert(
            data,
            ignore = false,
            shouldReturnGeneratedValues = false
        ) {
            this[Books.writer] = 1L
            this[Books.title] = "$it-title"
            this[Books.status] = BookStatus.NONE
            this[Books.price] = it.toBigDecimal()
            this[Books.createdAt] = LocalDateTime.now()
            this[Books.updatedAt] = LocalDateTime.now()
        }
    }

    @Test
    fun `auto commit log test`() {
        insertBook("title", BigDecimal.ZERO)
        insertBook("title", BigDecimal.ZERO)
        insertBook("title", BigDecimal.ZERO)
        insertBook("title", BigDecimal.ZERO)
        insertBook("title", BigDecimal.ZERO)
        insertBook("title", BigDecimal.ZERO)
    }

    private fun batchInsertBook(data: List<Int> = (1..10).map { it }) {
        Books.batchInsert(
            data
        ) {
            this[Books.writer] = 1L
            this[Books.title] = "$it-title"
            this[Books.status] = BookStatus.NONE
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
        val insertWriter = insertWriter("asd", "asd")
        book[this.writer] = insertWriter[Writers.id]
        book[this.title] = title
        book[this.price] = price
        book[this.status] = BookStatus.NONE
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