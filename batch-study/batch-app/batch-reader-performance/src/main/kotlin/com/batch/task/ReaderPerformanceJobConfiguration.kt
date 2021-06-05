package com.batch.task

import com.batch.payment.domain.payment.QPayment.payment as qPayment
import com.batch.payment.domain.payment.Payment
import com.batch.task.support.listener.JobReportListener
import java.time.LocalDateTime
import javax.persistence.EntityManagerFactory
import org.hibernate.SessionFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
import org.springframework.batch.item.querydsl.reader.QuerydslNoOffsetPagingItemReader
import org.springframework.batch.item.querydsl.reader.expression.Expression
import org.springframework.batch.item.querydsl.reader.options.QuerydslNoOffsetNumberOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

const val CHUNK_SIZE = 100
const val DATA_SET_UP_SIZE = 10_000

fun <A : Any> A.logger(): Lazy<Logger> = lazy { LoggerFactory.getLogger(this.javaClass) }

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
        queryDslNoOffsetPagingReader: QuerydslNoOffsetPagingItemReader<Payment>
    ) =
        stepBuilderFactory["readerPerformanceStep"]
            .chunk<Payment, Payment>(CHUNK_SIZE)
//            .reader(jpaCursorItemReader)
//            .reader(jpaPagingItemReader)
            .reader(queryDslNoOffsetPagingReader)
//            .reader(hibernateCursorItemReader)
//            .reader(queryDslPagingItemReader)
            .writer { log.info("item size ${it.size}") }
            .build()

    @Bean
    @StepScope
    fun jpaPagingItemReader(
        entityManagerFactory: EntityManagerFactory
    ) = JpaPagingItemReaderBuilder<Payment>()
        .name("jpaPagingItemReader")
        .pageSize(CHUNK_SIZE)
        .entityManagerFactory(entityManagerFactory)
        .queryString("SELECT p FROM Payment p where p.createdAt >= :createdAt ORDER BY p.createdAt DESC")
        .parameterValues(mapOf("createdAt" to LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0)))
        .build()

    @Bean
    @StepScope
    fun jpaCursorItemReader(
        entityManagerFactory: EntityManagerFactory
    ) = JpaCursorItemReaderBuilder<Payment>()
        .name("jpaCursorItemReader")
        .entityManagerFactory(entityManagerFactory)
        .queryString("SELECT p FROM Payment p where p.createdAt >= :createdAt ORDER BY p.createdAt DESC")
        .parameterValues(mapOf("createdAt" to LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0)))
        .build()

    @Bean
    @StepScope
    fun queryDslNoOffsetPagingReader(
        entityManagerFactory: EntityManagerFactory
    ): QuerydslNoOffsetPagingItemReader<Payment> {
        // 1. No Offset Option
        val options = QuerydslNoOffsetNumberOptions<Payment, Long>(qPayment.id, Expression.ASC)
        // 2. Querydsl Reader
        return QuerydslNoOffsetPagingItemReader(entityManagerFactory, CHUNK_SIZE, options) {
            it.selectFrom(qPayment)
                .where(qPayment.createdAt.goe(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0)))
        }
    }

    @Bean
    @StepScope
    fun hibernateCursorItemReader(
        sessionFactory: SessionFactory
    ) = HibernateCursorItemReaderBuilder<Payment>()
        .name("hibernateCursorItemReader")
//        .fetchSize()
        .sessionFactory(sessionFactory)
        .queryString("SELECT p FROM Payment p where p.createdAt >= :createdAt ORDER BY p.createdAt DESC")
        .parameterValues(mapOf("createdAt" to LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0)))
        .build()
}