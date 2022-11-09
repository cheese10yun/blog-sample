package com.example.exposedstudy

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Transaction
import java.time.LocalDateTime
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.BatchUpdateStatement
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BatchInsertService {

    @Transactional
    fun batch(ids: List<Int>) {
        Books.batchInsert(
            data = ids,
            ignore = false,
            shouldReturnGeneratedValues = false
        ) {
            val insertWriter = insertWriter("asd", "asd")
            this[Books.writer] = insertWriter[Writers.id]
            this[Books.title] = "$it-title"
            this[Books.status] = BookStatus.NONE
            this[Books.price] = it.toBigDecimal()
            this[Books.createdAt] = LocalDateTime.now()
            this[Books.updatedAt] = LocalDateTime.now()
        }
    }

    fun insertWriter(
        name: String,
        email: String
    ): InsertStatement<Number> {

        return Writers.insert { writer ->
            writer[this.name] = name
            writer[this.email] = email
            writer[this.createdAt] = LocalDateTime.now()
            writer[this.updatedAt] = LocalDateTime.now()
        }
    }

    @Transactional
    fun batchUpdate(
        ids: List<Long>
    ) {

        BatchUpdateStatement(Books).apply {
            ids.forEach {
                addBatch(EntityID(it, Books))
                this[Books.title] = "bul update"
            }
        }
            .execute(TransactionManager.current())

    }
}