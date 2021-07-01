package com.example.exposedstudy

import java.time.LocalDateTime
import org.jetbrains.exposed.sql.batchInsert
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
            this[Books.writer] = 1L
            this[Books.title] = "$it-title"
            this[Books.price] = it.toBigDecimal()
            this[Books.createdAt] = LocalDateTime.now()
            this[Books.updatedAt] = LocalDateTime.now()
        }
    }
}