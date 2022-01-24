package com.batch.task

import com.batch.payment.domain.book.Book
import com.batch.task.support.listener.JobReportListener
import com.batch.task.support.logger
import java.time.LocalDateTime
import javax.persistence.EntityManagerFactory
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

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
        jpaPagingItemReader: JpaPagingItemReader<Book>,
        bookStatusLatestWriter: ItemWriter<Book>
    ) =
        stepBuilderFactory["readerPerformanceStep"]
            .chunk<Book, Book>(CHUNK_SIZE)
            .reader(jpaPagingItemReader)
            .writer(bookStatusLatestWriter)
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
    fun bookStatusLatestWriter(
        bookStatusLatestService: BookStatusLatestService
    ) = ItemWriter<Book> { books ->

        val bookIds = books.mapNotNull { it.id }
        bookStatusLatestService.getLatestBookStatus(bookIds)
        log.info("item size ${books.size}")
    }
}