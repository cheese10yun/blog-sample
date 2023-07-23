package com.example.kotlincoroutine

import kotlin.system.measureTimeMillis
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class Coroutines {

    @Test
    @DelicateCoroutinesApi
    fun `코루틴 생성 테스트`() = runBlocking {
        println("${Thread.activeCount()} thread active at the start")

        val time = measureTimeMillis {
            createCoroutines(3)
        }

        println("${Thread.activeCount()} thread active at the end")
        println("Took $time ms")

    }

    @DelicateCoroutinesApi
    suspend fun createCoroutines(amount: Int) {
        val jobs = ArrayList<Job>()
        for (i in 1..amount) {
            jobs += GlobalScope.launch {
                println("Started $i in ${Thread.currentThread().name}")
                delay(1_000)
                println("Finished $i in ${Thread.currentThread().name}")
            }
        }
        jobs.forEach { it.join() }
    }

}