package com.batch.task

import com.batch.payment.domain.book.BookStatus
import com.batch.payment.domain.book.Books
import com.batch.task.support.logger
import java.time.LocalDateTime
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.stereotype.Component

@Component
class JobDataSetUpListener(
    private val exposedDataBase: Database
) : JobExecutionListener {
    val log by logger()

    override fun beforeJob(jobExecution: JobExecution) {
        transaction(
            exposedDataBase
        ) {
            Books.batchInsert(
                data = (1..DATA_SET_UP_SIZE),
                shouldReturnGeneratedValues = false
            ) { _ ->
                this[Books.status] = BookStatus.AVAILABLE_RENTAL
                this[Books.createdAt] = LocalDateTime.now()
                this[Books.updatedAt] = LocalDateTime.now()
            }
        }
    }

    override fun afterJob(jobExecution: JobExecution): Unit = Unit
}