package com.batch.task

import com.batch.payment.domain.book.Book
import com.batch.payment.domain.book.QBook
import com.batch.task.support.listener.JobReportListener
import com.batch.task.support.logger
import java.time.LocalDateTime
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JpaCursorItemReader
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.batch.item.querydsl.reader.QuerydslNoOffsetPagingItemReader
import org.springframework.batch.item.querydsl.reader.expression.Expression
import org.springframework.batch.item.querydsl.reader.options.QuerydslNoOffsetNumberOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

const val CHUNK_SIZE = 10
const val DATA_SET_UP_SIZE = 1_000

private val localDateTime = LocalDateTime.of(2021, 6, 1, 0, 0, 0)

@Configuration
class PerformanceJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    val log by logger()

    @Bean
    fun performanceJob(
        jobDataSetUpListener: JobDataSetUpListener,
        performanceStep: Step
    ) =
        jobBuilderFactory["performanceJob"]
            .incrementer(RunIdIncrementer())
            .listener(JobReportListener())
            .listener(jobDataSetUpListener)
            .start(performanceStep)
            .build()

    @Bean
    @JobScope
    fun performanceStep(
        transactionManager: PlatformTransactionManager,
        jpaPagingItemReader: JpaPagingItemReader<Book>,
        jpaCursorItemReader: JpaCursorItemReader<Book>,
        queryDslNoOffsetPagingReader: QuerydslNoOffsetPagingItemReader<Book>,
        bookStatusLatestBulkWriter: ItemWriter<Book>,
        bookStatusLatestWriter: ItemWriter<Book>

    ) =
        stepBuilderFactory["readerPerformanceStep"]
            .chunk<Book, Book>(CHUNK_SIZE)
//            .reader(jpaPagingItemReader)
//            .reader(jpaCursorItemReader)
            .reader(queryDslNoOffsetPagingReader)
//            .writer(bookStatusLatestBulkWriter)
            .writer(bookStatusLatestWriter)
            .transactionManager(transactionManager)
            .build()

    @Bean
    @StepScope
    fun jpaPagingItemReader(
        entityManagerFactory: EntityManagerFactory
    ) = JpaPagingItemReaderBuilder<Book>()
        .name("jpaPagingItemReader")
        .pageSize(CHUNK_SIZE)
        .entityManagerFactory(entityManagerFactory)
        .queryString("SELECT b FROM Book b where b.createdAt >= :createdAt ORDER BY b.createdAt DESC")
        .parameterValues(mapOf("createdAt" to localDateTime))
        .build()

    @Bean
    @StepScope
    fun jpaCursorItemReader(
        entityManagerFactory: EntityManagerFactory
    ) = JpaCursorItemReaderBuilder<Book>()
        .name("jpaCursorItemReader")
        .entityManagerFactory(entityManagerFactory)
        .queryString("SELECT b FROM Book b where b.createdAt >= :createdAt ORDER BY b.createdAt DESC")
        .parameterValues(mapOf("createdAt" to localDateTime))
        .build()


    @Bean
    @StepScope
    fun queryDslNoOffsetPagingReader(
        entityManagerFactory: EntityManagerFactory
    ): QuerydslNoOffsetPagingItemReader<Book> {
        // 1. No Offset Option
        val options = QuerydslNoOffsetNumberOptions<Book, Long>(QBook.book.id, Expression.ASC)
        // 2. Querydsl Reader
        return QuerydslNoOffsetPagingItemReader(entityManagerFactory, CHUNK_SIZE, options) {
            it.selectFrom(QBook.book)
                .where(
                    QBook.book.createdAt.goe(
                        localDateTime
                    )
                )
        }
    }


    @Bean
    @StepScope
    fun bookStatusLatestBulkWriter(
        bookStatusLatestService: BookStatusLatestService
    ) = ItemWriter<Book> { books ->
        val bookIds = books.mapNotNull { it.id }
        bookStatusLatestService.updateInLatestBookStatus(bookIds)
        log.info("item size ${books.size}")
    }

    @Bean
    @StepScope
    fun bookStatusLatestWriter(
        bookStatusLatestService: BookStatusLatestService
    ) = ItemWriter<Book> { books ->
//        val bookIds = books.mapNotNull { it.id }
        bookStatusLatestService.updateLatestBookStatus(books)
        log.info("item size ${books.size}")
    }


}