package com.example.kotlincoroutine
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.concurrent.CancellationException


private val log = kLogger()

class CoroutineStud3 {
    private suspend fun nonStructured() = coroutineScope {
        log.info("Step 1")
        launch {
            try {
                delay(1000)
                log.info("Finish launch1")
            } catch (e: CancellationException) {
                log.info("Job1 is cancelled")
            }
        }
        log.info("Step 2")
        launch {

            try {
                delay(100)
                log.info("Finish launch2")
            } catch (e: CancellationException) {
                log.info("Job2 is cancelled")
            }
        }
        delay(100)
        this.cancel()
    }

    @Test
    fun `cancel job`(): Unit = runBlocking {
        log.info("Start runBlocking")
        try {
            nonStructured()
        } catch (e: CancellationException) {
            log.info("Job is cancelled")
        }
        log.info("Finish runBlocking")
    }
}

