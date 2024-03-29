package com.example.exposedstudy

import org.assertj.core.api.BDDAssertions.then
import org.jetbrains.exposed.dao.id.EntityID
import java.math.BigDecimal
import java.time.LocalDateTime
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.groupConcat
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.junit.jupiter.api.Test

class ExposedDaoTest : ExposedTestSupport() {

    @Test
    fun create() {
        val writer = Writer.new {
            this.register("    asd    ", "asd")
        }

        writer.name
        println(writer.name)
    }

    @Test
    fun `Writers name test`() {
        //given
        val name = "    yun kim   "
        val email = "email@asd.com"

        //when
        val entityID = Writers.insertAndGetId { writer ->
            writer[this.name] = name
            writer[this.email] = email
            writer[this.createdAt] = LocalDateTime.now()
            writer[this.updatedAt] = LocalDateTime.now()
        }

        //then
        val writer = Writer.findById(entityID)!!



        then(writer.name).isEqualTo("yun kim")
    }

    @Test
    fun `groupConcat`() {
        //given
        val name = "yun kim"
        Writers.batchInsert((1..2)) {
            this[Writers.name] = name
            this[Writers.email] = "email-${it}@asd.com"
        }.map {
            it[Writers.id]

        }

        //when
        val groupConcat = Writers.email.groupConcat(distinct = true)
        val result = Writers
            .slice(
                groupConcat,
                Writers.name,
            )
            .select {
                Writers.name eq name
            }
            .map {
                Pair(
                    it[Writers.name], it[groupConcat]
                )
            }
            .first()

        //then
        then(result.first).isEqualTo(name)
        then(result.second).isEqualTo("email-1@asd.com,email-2@asd.com")
    }

    @Test
    fun `groupConcat enum`() {
        //given
        val entityID = insertWriter("name", "asd")[Writers.id]
        batchInsertBook(entityID = entityID)


        //when
        val groupConcatEnum = Books.status.groupConcatEnum(distinct = true)
        val result = Books
            .slice(Books.title, groupConcatEnum)
            .select { Books.writer.eq(entityID) }
//            .map { Pair(it[Books.title], it[groupConcatEnum] ) }
//            .first()


        //then
        println("")

    }

    @Test
    fun `read`() {
        val writerId = insertWriter("asd", "asd@asd")[Writers.id].value
        batchInsertBook(writerId = writerId)
        val books = Book.find { Books.title like "%title%" }
        for (book in books) {
            println(book)
        }
    }

    @Test
    fun `sort`() {
        (1..10).map { insertWriter("$it asd", "$it asd@asd") }

        Writer.all().sortedByDescending { it.id }
            .forEach { println(it) }
    }

    @Test
    fun update() {
        (1..10).map {
            insertWriter("$it asd", "$it asd@asd")
        }

        val writers = Writer.all().sortedByDescending { it.id }

        for (writer in writers) {
            writer.updateProfile("new", "123@asd.com")
        }

        Writer.all().sortedByDescending { it.id }
            .forEach { println(it) }
    }

    @Test
    fun `delete`() {
        (1..10).map {
            insertWriter("$it asd", "$it asd@asd")
        }

        Writer.all()
            .forEach { it.delete() }
    }

    @Test
    fun `wrapRows`() {
        val writerId = insertWriter("yun", "yun@asd.com")[Writers.id].value
//        (1..5).map {
//            insertBook("$it-title", BigDecimal.TEN, writerId)
//        }
//
//        val query = (Books innerJoin Writers)
//                .slice(
//                        Books.id,
//                        Books.title,
//                        Books.price,
//                        Writers.name,
//                        Writers.email,
//                )
//                .selectAll()

        val query = Writers.selectAll()

        Writer.wrapRows(query)
            .forEach { println(it) }

    }


    private fun batchInsertBook(
        data: List<Int> = (1..10).map { it },
        writerId: Long,
    ) {
        Books.batchInsert(
            data
        ) {
            this[Books.writer] = writerId
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
        writerId: Long = 1L,
        bookStatus: BookStatus = BookStatus.NONE

    ) = Books.insert { book ->
        book[this.writer] = writerId
        book[this.title] = title
        book[this.status] = bookStatus
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

    private fun batchInsertBook(data: List<Int> = (1..10).map { it }, entityID: EntityID<Long>) {
        Books.batchInsert(
            data
        ) {
            this[Books.writer] = entityID
            this[Books.title] = "$it-title"
            this[Books.status] = if (it % 2 == 0) BookStatus.NONE else BookStatus.READY
            this[Books.price] = it.toBigDecimal()
            this[Books.createdAt] = LocalDateTime.now()
            this[Books.updatedAt] = LocalDateTime.now()
        }
    }

}

