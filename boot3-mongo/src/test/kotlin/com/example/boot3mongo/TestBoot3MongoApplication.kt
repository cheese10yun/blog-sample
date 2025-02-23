package com.example.boot3mongo

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<Boot3MongoApplication>().with(TestcontainersConfiguration::class).run(*args)
}
