package com.example.kotlincoroutine

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class CoroutineStud4 {

    private val log = kLogger()


    @Test
    fun `flow test`(): Unit = runBlocking {
        log.info("Start runBlocking")
        rang(5000)
            .take(3)
            .map { it * 2 }
            .toList()
//            .collect {
//                log.info("Item: {$it}")
//            }
        log.info("Finish runBlocking")
    }

    private fun rang(n: Int): Flow<Int> {
        return flow {
            for (i in 0 until n) {
//                delay(100)
                emit(i)
            }
        }
    }
}