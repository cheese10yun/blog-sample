package com.example.dataflowserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.dataflow.server.EnableDataFlowServer

@SpringBootApplication
@EnableDataFlowServer
class DataFlowServerApplication

fun main(args: Array<String>) {
    runApplication<DataFlowServerApplication>(*args)
}
