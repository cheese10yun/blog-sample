package com.example.kotlincoroutine

import kotlin.system.measureTimeMillis
import kotlinx.coroutines.*

fun main() {
    runBlocking {
        // 3개의 비동기 job 실행
        val time = measureTimeMillis {
            val job1 = launch {
                println("Job 1이 스레드에서 실행됩니다: ${Thread.currentThread().name}")
                delay(3000) // 작업 시뮬레이션
                println("Job 1이 완료되었습니다")
            }

            val job2 = launch {
                println("Job 2가 스레드에서 실행됩니다: ${Thread.currentThread().name}")
                delay(3000) // 작업 시뮬레이션
                println("Job 2가 완료되었습니다")
            }

            val job3 = launch {
                println("Job 3이 스레드에서 실행됩니다: ${Thread.currentThread().name}")
                delay(3000) // 작업 시뮬레이션
                println("Job 3이 완료되었습니다")
            }

            // 모든 job이 완료될 때까지 기다립니다
        job1.join()
        job2.join()
        job3.join()
//
//            job1.start()
//            job2.start()
//            job3.start()

            println("모든 job이 완료되었습니다")
        }

        println("Execution took $time ms")
    }
}