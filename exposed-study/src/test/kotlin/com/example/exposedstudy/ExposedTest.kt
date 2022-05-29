package com.example.exposedstudy

import java.time.LocalDateTime
import javax.sql.DataSource
import org.assertj.core.api.BDDAssertions.then
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.junit.jupiter.api.Test

class ExposedTest(
    private val dataSource: DataSource
) : ExposedTestSupport() {

    @Test
    fun `book insert`() {
        // SQL Log INSERT INTO book (created_at, price, status, title, updated_at, writer_id) VALUES ('2022-05-29T21:12:27.464364', 1000, 'NONE', 'test', '2022-05-29T21:12:27.464374', 37)
        val book = Books.insert { book ->
            book[this.writer] = insertWriter("asd", "asd")[Writers.id]
            book[this.title] = "test"
            book[this.price] = 1000.toBigDecimal()
            book[this.status] = BookStatus.NONE
            book[this.createdAt] = LocalDateTime.now()
            book[this.updatedAt] = LocalDateTime.now()
        }

        then(book[Books.status]).isEqualTo(BookStatus.NONE)
    }

    @Test
    fun `writer inser222t`() {
        val connect = Database.connect(dataSource)
        transaction(connect) {
            //given
            // SQL Query INSERT INTO writer (created_at, email, `name`, updated_at) VALUES ('2022-05-29T19:46:46.845358', 'asd@asd.com', 'name', '2022-05-29T19:46:46.845382')
            val insert = Writers.insert { writer ->
                writer[this.name] = "name"
                writer[this.email] = "asd@asd.com"
            }

            //when
            then(Writers.createdAt).isNotNull // 2022-05-29T19:46:46.845358
            then(Writers.updatedAt).isNotNull // 2022-05-29T19:46:46.845382
        }
    }

    @Test
    fun `writer test`() {
        val connect = Database.connect(dataSource)
        transaction(connect) {
            //given
            val insert = Writers.insert { writer ->
                writer[this.name] = "name"
                writer[this.email] = "asd@asd.com"
            }

            //when
            val id = insert[Writers.id]
            Writers.update({ Writers.id eq id })
            {
                it[this.email] = "new@asd.com"
                it[this.updatedAt] = LocalDateTime.now()
            }

            //then
            val findWriter = Writers.select(Writers.id eq id).first()
            then(insert[Writers.updatedAt]).isNotEqualTo(findWriter[Writers.updatedAt])
        }
    }

    @Test
    fun `writer test 2`() {
        val connect = Database.connect(dataSource)
        transaction(connect) {
            //given
            val insert = Writers.insert { writer ->
                writer[this.name] = "name"
                writer[this.email] = "asd@asd.com"
            }
            //when
            val id = insert[Writers.id]

            then(Writers.createdAt).isNotNull // 2022-05-29T19:46:46.845358
            then(Writers.updatedAt).isNotNull // 2022-05-29T19:46:46.845382

            //then
            Writers.update({ Writers.id eq id })
            {
                it[this.email] = "new@asd.com"
                it[this.updatedAt] = LocalDateTime.now()
            }

            val selectAll = Writers.select(Writers.id eq id)

            selectAll.forEach {
                println("===========Update==============")
                println("id: ${it[Writers.id]}")
                println("email: ${it[Writers.email]}")
                println("createdAt: ${it[Writers.createdAt]}")
                println("updatedAt: ${it[Writers.updatedAt]}")
                println("===========Update==============")
            }
        }
    }

    @Test
    fun `writer `() {
        val connect = Database.connect(dataSource)
        lateinit var id: EntityID<Long>
        lateinit var initUpdatedAt: LocalDateTime
        transaction(connect) {
            //given
            val writer = Writer.new {
                instance(
                    name = "22222",
                    email = "asd@asd"
                )
            }
            id = writer.id
            initUpdatedAt = writer.updatedAt
        }

        transaction(connect) {
            //given
            // SQL Query UPDATE writer SET email='new@sd.com', `name`='new', updated_at='2022-05-29T21:00:45.562978' WHERE id = 36
            val findWriter = Writer.findById(id)!!
            findWriter.name = "new"
            findWriter.email = "new@sd.com"
            findWriter.updatedAt = LocalDateTime.now() // 주석시 아래 검증 실패
        }

        transaction(connect) {
            //given
            val findWriter = Writer.findById(id)!!
            then(initUpdatedAt).isNotEqualTo(findWriter.updatedAt)
        }


    }

    @Test
    fun `연관관계 없는 조인`() {
        val connect = Database.connect(dataSource)
        transaction(connect) {
            val writer = Writers.insert {
                it[this.name] = "name"
                it[this.email] = "name@add.cpm"
            }
            val publisher = Publishers.insert {
                it[this.writerId] = writer[Writers.id].value
                it[this.corpName] = "corp name"
            }

            Publishers
                .join(
                    otherTable = Writers, // (1)
                    joinType = JoinType.LEFT, //(2)
                    additionalConstraint = { // (3)
                        Publishers.writerId eq Writers.id
                    }
                )
                .select {
                    Publishers.id eq publisher[Publishers.id].value
                }
                .forEach {
                    println("Publishers.id : ${it[Publishers.id]}") // Publishers.id : 7
                    println("Publishers.corpName : ${it[Publishers.corpName]}") // Publishers.corpName : corp name
                    println("Publishers.writerId : ${it[Publishers.writerId]}") // Publishers.writerId : 44
                    println("Writers.id : ${it[Writers.id]}") // Writers.id : 44
                    println("Writers.name : ${it[Writers.name]}") // Writers.name : name
                    println("Writers.email : ${it[Writers.email]}") // Writers.email : name@add.cpm
                }
        }
    }

    @Test
    fun `연관관계 없는 조인2`() {
        val connect = Database.connect(dataSource)
        transaction(connect) {
            val writer = Writers.insert {
                it[this.name] = "name"
                it[this.email] = "name@add.cpm"
            }
            val publisher = Publishers.insert {
                it[this.writerId] = writer[Writers.id].value
                it[this.corpName] = "corp name"
            }

            val needJoin = true

            Publishers
                .slice(
                    Publishers.id,
                    Publishers.corpName,
                    Publishers.writerId
                )
                .select {
                    Publishers.id eq publisher[Publishers.id].value
                }
                .apply {
                    if (needJoin) {
                        this.adjustColumnSet {
                            join(
                                otherTable = Writers, // (1)
                                joinType = JoinType.LEFT, //(2)
                                additionalConstraint = { // (3)
                                    Publishers.writerId eq Writers.id
                                }
                            )
                        }
                        this.adjustSlice {
                            slice(it.fields + Writers.id + Writers.name + Writers.email)
                        }
                    }
                }
                .forEach {
                    println("Publishers.id : ${it[Publishers.id]}") // Publishers.id : 8
                    println("Publishers.corpName : ${it[Publishers.corpName]}") // Publishers.corpName : corp name
                    println("Publishers.writerId : ${it[Publishers.writerId]}") // Publishers.writerId : 45
                    if (needJoin) {
                        println("Writers.id : ${it[Writers.id]}") // Writers.id : 45
                        println("Writers.name : ${it[Writers.name]}") // Writers.name : name
                        println("Writers.email : ${it[Writers.email]}") // Writers.email : name@add.cpm
                    }
                }
        }
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