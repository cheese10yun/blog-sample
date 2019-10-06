package com.example.dataflow

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.task.configuration.EnableTask
import org.springframework.cloud.task.listener.TaskExecutionListener
import org.springframework.cloud.task.repository.TaskExecution

@EnableTask
@SpringBootApplication
class DataFlowApplication : ApplicationRunner, TaskExecutionListener {

    override fun run(args: ApplicationArguments) {
        println("Spring Cloud Task!  : ${args.sourceArgs[0]}")
    }

    override fun onTaskStartup(taskExecution: TaskExecution) {

        println("TaskName : ${taskExecution.taskName}, Execution Id : ${taskExecution.executionId}, started....")

    }

    override fun onTaskEnd(taskExecution: TaskExecution) {
        println("TaskName : ${taskExecution.taskName}, Execution Id : ${taskExecution.executionId}, completed....")
    }

    override fun onTaskFailed(taskExecution: TaskExecution, throwable: Throwable?) {
        println("TaskName : ${taskExecution.taskName}, Execution Id : ${taskExecution.executionId}, failed...")
    }
}

fun main(args: Array<String>) {
    runApplication<DataFlowApplication>(*args)
}
