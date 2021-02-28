package com.batch.task

import com.batch.payment.domain.payment.Payment
import com.batch.payment.domain.payment.QPayment
import com.batch.task.support.listener.JobReportListener
import com.batch.task.support.logger
import com.batch.task.support.reader.QuerydslPagingItemReader
import org.hibernate.SessionFactory
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.database.HibernateCursorItemReader
import org.springframework.batch.item.database.JpaCursorItemReader
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.HibernateCursorItemReaderBuilder
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.persistence.EntityManagerFactory

const val CHUNK_SIZE = 100
const val DATA_SET_UP_SIZE = 5_000

@Configuration
class ReaderPerformanceJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    val log by logger()

    @Bean
    fun readerPerformanceJob(
        jobDataSetUpListener: JobDataSetUpListener,
        readerPerformanceStep: Step
    ) =
        jobBuilderFactory["readerPerformanceJob"]
            .incrementer(RunIdIncrementer())
            .listener(JobReportListener())
            .listener(jobDataSetUpListener)
            .start(readerPerformanceStep)
            .build()

    @Bean
    @JobScope
    fun readerPerformanceStep(
        jpaCursorItemReader: JpaCursorItemReader<Payment>,
        jpaPagingItemReader: JpaPagingItemReader<Payment>,
        hibernateCursorItemReader: HibernateCursorItemReader<Payment>,
        queryDslPagingItemReader: QuerydslPagingItemReader<Payment>
    ) =
        stepBuilderFactory["readerPerformanceStep"]
            .chunk<Payment, Payment>(CHUNK_SIZE)
//            .reader(jpaCursorItemReader)
//            .reader(jpaPagingItemReader)
//            .reader(hibernateCursorItemReader)
            .reader(queryDslPagingItemReader)
            .writer { log.info("item size ${it.size}") }
            .build()

    @Bean
    @StepScope
    fun jpaCursorItemReader(
        entityManagerFactory: EntityManagerFactory
    ) = JpaCursorItemReaderBuilder<Payment>()
        .name("jpaCursorItemReader")
        .entityManagerFactory(entityManagerFactory)
        .queryString("SELECT p FROM Payment p")
        .build()

    @Bean
    @StepScope
    fun jpaPagingItemReader(
        entityManagerFactory: EntityManagerFactory
    ) = JpaPagingItemReaderBuilder<Payment>()
        .name("jpaPagingItemReader")
        .entityManagerFactory(entityManagerFactory)
        .queryString("SELECT p FROM Payment p")
        .build()

    @Bean
    @StepScope
    fun hibernateCursorItemReader(
        sessionFactory: SessionFactory
    ) = HibernateCursorItemReaderBuilder<Payment>()
        .name("hibernateCursorItemReader")
        .sessionFactory(sessionFactory)
        .queryString("SELECT p FROM Payment p")
        .build()

    @Bean
    @StepScope
    fun queryDslPagingItemReader(
        entityManagerFactory: EntityManagerFactory
    ) = QuerydslPagingItemReader(
        entityManagerFactory = entityManagerFactory,
        pageSize = CHUNK_SIZE
    ){
        it.selectFrom(QPayment.payment)
    }
}