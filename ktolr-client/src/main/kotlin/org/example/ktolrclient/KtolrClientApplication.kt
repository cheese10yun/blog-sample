package org.example.ktolrclient

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KtolrClientApplication

fun main(args: Array<String>) {
    runApplication<KtolrClientApplication>(*args)
}
