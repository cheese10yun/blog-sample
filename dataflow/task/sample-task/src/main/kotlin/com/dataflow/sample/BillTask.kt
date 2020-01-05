package com.dataflow.sample

import com.dataflow.sample.domain.Bill
import com.dataflow.sample.domain.BillRepository
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.step.tasklet.TaskletStep
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.support.ListItemReader
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class BillTask(
        private val jobBuilderFactory: JobBuilderFactory,
        private val stepBuilderFactory: StepBuilderFactory,
        private val billRepository: BillRepository
) {

    @Bean
    fun billJob(billStep: Step): Job {
        return jobBuilderFactory.get("billJob")
                .incrementer(RunIdIncrementer())
                .start(billStep)
                .build()
    }

    @Bean
    fun billStep(): TaskletStep {

        return stepBuilderFactory.get("billStep")
                .chunk<Bill, Bill>(3)
                .reader(ListItemReader(billRepository.findAll()))
                .processor(ItemProcessor { item ->
                    item.changeFirstName()
                    item
                })
                .writer { items -> billRepository.saveAll(items) }
                .build()
    }
}


