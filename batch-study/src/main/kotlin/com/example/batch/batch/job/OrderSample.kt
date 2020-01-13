package com.example.batch.batch.job

import com.example.batch.domain.order.dao.OrderRepository
import com.example.batch.domain.order.domain.Order
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.support.ListItemReader
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor

@Configuration
class OrderSample(
        private val orderRepository: OrderRepository,
        private val stepBuilderFactory: StepBuilderFactory,
        private val jobBuilderFactory: JobBuilderFactory
) {

    @Bean
    fun orderSampleJob(orderSampleStep: Step): Job {
        return jobBuilderFactory.get("orderSampleJob")
                .start(orderSampleStep)
                .build()
    }

    @Bean
    fun orderSampleStep(@Qualifier("taskExecutor") taskExecutor: TaskExecutor): Step {
        return stepBuilderFactory.get("orderSampleStep")
                .chunk<Order, Order>(10)
                .reader(sampleReader())
                .processor(sampleProcessor())
                .writer(sampleWriter())
                .taskExecutor(taskExecutor)
                .throttleLimit(2)
                .build()
    }

    @Bean
    @StepScope
    fun sampleReader(): ListItemReader<Order> {
        return ListItemReader(orderRepository.findAll())
    }

    private fun sampleProcessor(): ItemProcessor<Order, Order> {
        return ItemProcessor { item ->
            item.updatePrice()
            item
        }
    }

    private fun sampleWriter(): ItemWriter<Order> {
        return ItemWriter { items -> orderRepository.saveAll(items) }
    }
}