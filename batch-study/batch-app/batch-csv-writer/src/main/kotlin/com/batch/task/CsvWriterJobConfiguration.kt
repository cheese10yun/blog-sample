package com.batch.task

import com.batch.payment.domain.payment.Payment
import com.batch.task.support.listener.JobReportListener
import com.batch.task.support.support.LineAggregator
import com.batch.task.support.support.LineMapper
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder
import org.springframework.batch.item.file.transform.FieldSet
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import javax.persistence.EntityManagerFactory

@Configuration
class CsvWriterJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    entityManagerFactory: EntityManagerFactory
) {
    private val CHUNK_SIZE = 10

    @Bean
    fun csvWriterJob(
        csvWriterStep: Step
    ): Job =
        jobBuilderFactory["csvWriterJob"]
            .incrementer(RunIdIncrementer())
            .listener(JobReportListener())
            .start(csvWriterStep)
            .build()

    @Bean
    @JobScope
    fun csvWriterStep(
        stepBuilderFactory: StepBuilderFactory
    ): Step =
        stepBuilderFactory["csvWriterStep"]
            .chunk<Payment, PaymentCsv>(CHUNK_SIZE)
            .reader(reader)
            .writer(writer)
            .build()

    private val reader: JpaPagingItemReader<Payment> =
        JpaPagingItemReaderBuilder<Payment>()
            .queryString("select p from Payment p")
            .entityManagerFactory(entityManagerFactory)
            .name("readerPayment")
            .build()

    private val writer: FlatFileItemWriter<PaymentCsv> =
        FlatFileItemWriterBuilder<PaymentCsv>()
            .name("writerPayment")
            .resource(FileSystemResource("src/main/resources/payment.csv"))
            .append(false)
            .lineAggregator(PaymentCsvMapper().delimitedLineAggregator())
            .headerCallback {
                it.write(PaymentCsvMapper().headerNames.joinToString(","))
            }
            .encoding(StandardCharsets.UTF_8.name())
            .build()
}

data class PaymentCsv(
    val amount: BigDecimal,
    val orderId: Long
)

class PaymentCsvMapper :
    LineMapper<PaymentCsv>,
    LineAggregator<PaymentCsv> {

    override val headerNames: Array<String> = arrayOf(
        "amount", "orderId"
    )

    override fun fieldSetMapper(fs: FieldSet) = PaymentCsv(
        amount = fs.readBigDecimal("amount"),
        orderId = fs.readLong("orderId")
    )
}