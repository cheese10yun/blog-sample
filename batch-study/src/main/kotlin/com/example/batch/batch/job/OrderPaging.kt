package com.example.batch.batch.job

import com.example.batch.batch.listener.InactiveJobListener
import com.example.batch.domain.order.dao.OrderRepository
import com.example.batch.domain.order.domain.Order
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.core.task.TaskExecutor
import java.math.BigDecimal
import java.util.*
import javax.persistence.EntityManagerFactory


@Suppress("SpringElInspection")
@Configuration
class OrderPaging(
        private val orderRepository: OrderRepository,
        private val stepBuilderFactory: StepBuilderFactory,
        private val jobBuilderFactory: JobBuilderFactory,
        private val entityManagerFactory: EntityManagerFactory
) {

    private val CHUNK_SZIE: Int = 100

    @Bean
    fun orderPagingJob(orderPagingStep: Step, inactiveJobListener: InactiveJobListener): Job {
        return jobBuilderFactory.get("orderPagingJob")
                .listener(inactiveJobListener)
                .start(orderPagingStep)
                .build()
    }

    @Bean
    @JobScope
    fun orderPagingStep(orderPagingReader: JpaPagingItemReader<Order>, taskExecutor: TaskExecutor): Step {
        return stepBuilderFactory.get("orderPagingStep")
                .chunk<Order, Order>(CHUNK_SZIE)
                .reader(orderPagingReader)
                .processor(pagingProcessor())
                .writer(pagingWriter())
                .taskExecutor(taskExecutor)
                .throttleLimit(4)
                .build()
    }

    @Bean(destroyMethod = "")
    @StepScope
    fun orderPagingReader(@Value("#{jobParameters[targetAmount]}") targetAmount: BigDecimal): JpaPagingItemReader<Order> {
        println("=============")
        println(targetAmount)
        println("=============")
        val itemReader = object : JpaPagingItemReader<Order>() {
            override fun getPage(): Int {
                return 0
            }
        }
        itemReader.setQueryString("select o from Order o where o.amount < :targetAmount")
        itemReader.pageSize = CHUNK_SZIE
        itemReader.setEntityManagerFactory(entityManagerFactory)
        val parameterValues = HashMap<String, Any>()
        parameterValues["targetAmount"] = targetAmount
        itemReader.setParameterValues(parameterValues)
        itemReader.setName("orderPagingReader")
        return itemReader
    }

    @Bean
    @StepScope
    fun pagingProcessor(): ItemProcessor<Order, Order> {
        return ItemProcessor { item ->
            item.updatePrice()
            item
        }
    }

    @Bean
    @StepScope
    fun pagingWriter(): ItemWriter<Order> {
        return ItemWriter { items ->
            orderRepository.saveAll(items)
        }
    }

    @Bean
    @StepScope
    fun taskExecutor(): TaskExecutor {
        return SimpleAsyncTaskExecutor("Batch_task")
    }
}