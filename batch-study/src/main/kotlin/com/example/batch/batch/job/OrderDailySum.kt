package com.example.batch.batch.job

import com.example.batch.domain.order.dao.OrderRepository
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal

@Configuration
class OrderDailySum(
        private val orderRepository: OrderRepository,
        private val stepBuilderFactory: StepBuilderFactory,
        private val jobBuilderFactory: JobBuilderFactory

) {

    @Bean
    fun orderDailySumJob(): Job {
        return jobBuilderFactory.get("orderDailySumJob")
                .start(orderDailySumStep(""))
                .build()
    }

    @Bean
    @JobScope
    fun orderDailySumStep(@Value("#{jobParameters[requestDate]}") requestDate : String): Step {
        println(requestDate)
        return stepBuilderFactory.get("orderDailrequestDateySumStep")
                .tasklet(tasklet())
                .build()
    }

    private fun tasklet(): (contribution: StepContribution, chunkContext: ChunkContext) -> RepeatStatus? {
        return { stepContribution, chunkContext ->
            run {
                val orders = orderRepository.findAll()
                var sumAmount: BigDecimal = BigDecimal.ZERO
                for (order in orders) {
                    sumAmount = sumAmount.plus(order.amount)
                }
                println(sumAmount)
            }
            RepeatStatus.FINISHED
        }
    }


}

