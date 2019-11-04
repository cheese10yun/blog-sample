package com.example.dataflowshell

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.dataflow.shell.EnableDataFlowShell

@EnableDataFlowShell
@SpringBootApplication
class DataFlowShellApplication

fun main(args: Array<String>) {
    runApplication<DataFlowShellApplication>(*args)
}
