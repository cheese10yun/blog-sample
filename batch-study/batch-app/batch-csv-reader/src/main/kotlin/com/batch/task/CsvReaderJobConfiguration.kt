package com.batch.task


import com.batch.payment.domain.payment.Payment
import com.batch.task.support.listener.JobReportListener
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.database.JpaItemWriter
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.math.BigDecimal
import javax.persistence.EntityManagerFactory

@Configuration
class CsvReaderJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    entityManagerFactory: EntityManagerFactory
) {
    private val CHUNK_SZIE = 10

    @Bean
    fun csvReaderJob(
        csvReaderStep: Step
    ): Job = jobBuilderFactory["csvReaderJob"]
        .incrementer(RunIdIncrementer())
        .listener(JobReportListener())
        .start(csvReaderStep)
        .build()

    @Bean
    @JobScope
    fun csvReaderStep(): Step =
        stepBuilderFactory["csvReaderStep"]
            .chunk<PaymentCsv, Payment>(CHUNK_SZIE)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build()

    private val processor: ItemProcessor<in PaymentCsv, out Payment> =
        ItemProcessor { it.toEntity() }

    private val reader: FlatFileItemReader<PaymentCsv> =
        FlatFileItemReaderBuilder<PaymentCsv>()
            .name("paymentCsv")
            .resource(ClassPathResource("/payment.csv"))
            .linesToSkip(1)
            .delimited()
            .delimiter(DelimitedLineTokenizer.DELIMITER_COMMA)
            .names("amount", "orderId")
            .fieldSetMapper {
                PaymentCsv(
                    amount = it.readBigDecimal("amount"),
                    orderId = it.readLong("orderId")
                )
            }
            .build()

    private val writer: JpaItemWriter<Payment> =
        JpaItemWriterBuilder<Payment>()
            .entityManagerFactory(entityManagerFactory)
            .build()
}

data class PaymentCsv(
    val amount: BigDecimal,
    val orderId: Long
) {
    fun toEntity() = Payment(amount, orderId)
}
