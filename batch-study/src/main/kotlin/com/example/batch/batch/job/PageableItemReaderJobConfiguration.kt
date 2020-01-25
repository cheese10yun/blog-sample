package com.example.batch.batch.job

import com.example.batch.batch.core.PageableItemReader
import com.example.batch.domain.order.dao.OrderRepository
import com.example.batch.domain.order.domain.Order
import logger
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort
import java.math.BigDecimal

@Configuration
class PageableItemReaderJobConfiguration(
        private val jobStepBuilder: JobBuilderFactory,
        private val stepBuilder: StepBuilderFactory,
        private val orderRepository: OrderRepository
) {
    private val log by logger()

    private val chunkSize = 200

    @Bean
    fun pageableItemReaderJob(): Job {
        return jobStepBuilder.get("pageableItemReaderJob")
                .incrementer(RunIdIncrementer())
                .start(step())
                .build()
    }

    private fun step(): Step {
        return stepBuilder.get("PageableItemReaderStep")
                .chunk<Order, Order>(chunkSize)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build()
    }


//    private fun reader(): PageableItemReader<Order> {
//        return PageableItemReader(
//                name = "PageableItemReaderReader",
//                sort = Sort.by(Sort.Direction.ASC, "id"),
//                pageSize = chunkSize,
//                query = orderRepository::findAll
//        )
//    }

    private fun reader(): PageableItemReader<Order> {

        return PageableItemReader(
                name = "PageableItemReaderReader",
                sort = Sort.by(Sort.Direction.ASC, "id"),
                pageSize = chunkSize,
                query = { orderRepository.findByAmountGreaterThan(BigDecimal(8000), it) }
        )
    }

    private fun processor(): ItemProcessor<Order, Order> {
        return ItemProcessor {
            log.info("ItemProcessor ->>>>>>> order id : ${it.id}")
            it
        }
    }


    private fun writer(): ItemWriter<Order> {
        return ItemWriter {
            for (order in it) {
                log.info("ItemWriter ->>>>>>>>> order id : ${order.id}")
            }
        }
    }
}