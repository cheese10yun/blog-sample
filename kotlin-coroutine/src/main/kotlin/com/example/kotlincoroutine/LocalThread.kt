package com.example.kotlincoroutine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking

private val log = kLogger()

fun main() {
    val getting = ThreadLocal<String>()

    getting.set("Hello")


    runBlocking {

        log.info("thread: ${Thread.currentThread().name}")
        log.info("getting: ${getting.get()}")



        launch(Dispatchers.Unconfined) {

            log.info("thread: ${Thread.currentThread().name}")
            log.info("getting: ${getting.get()}")

        }
    }
}