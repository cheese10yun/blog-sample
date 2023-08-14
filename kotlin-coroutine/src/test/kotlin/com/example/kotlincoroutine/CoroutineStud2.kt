package com.example.kotlincoroutine


import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test


//private val log = kLogger()





class CoroutineStud2 {

    private val log = kLogger()

    @Test
    fun `testasd`(): Unit = runBlocking {
        log.info("Start runBlocking")
        nonStructured()
        log.info("Finish runBlocking")
    }

    private suspend fun nonStructured() = coroutineScope {
        log.info("Step 1")
        launch {
            delay(1000)
            log.info("Finish launch1")
        }
        log.info("Step 2")
        launch {
            delay(100)
            log.info("Finish launch2")
        }
        log.info("Step 3")
    }
}