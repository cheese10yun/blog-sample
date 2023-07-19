package com.example.kotlincoroutine

import kotlin.system.measureTimeMillis
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class AAA {

    @Test
    @DelicateCoroutinesApi
    fun test() = runBlocking {

        println("${Thread.activeCount()} thread active at the start")

        val time = measureTimeMillis {
            createCoroutines(100_000)
        }

        println("${Thread.activeCount()} thread active at the end")
        println("Took $time ms")

    }

    @DelicateCoroutinesApi
    suspend fun createCoroutines(amount: Int) {
        val jobs = ArrayList<Job>()
        for (i in 1..amount) {
            jobs += GlobalScope.launch {
                delay(1000)
            }
        }
        jobs.forEach { it.join() }
    }
}