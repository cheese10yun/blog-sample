package com.example.kotlincoroutine.lecture

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.util.StopWatch

class IoTest {

    @Test
    fun `동시성 테스트`() {
        runBlocking {
            println("Main 시작 - 실행 스레드: ${Thread.currentThread().name}")
            val stopWatch = StopWatch()
            stopWatch.start()

            val deferred1 = async(Dispatchers.IO) { doSomething("deferred1") }
            val deferred2 = async(Dispatchers.IO) { doSomething("deferred2") }

            // 결과 대기
            val resultDefault = deferred1.await()
            println("deferred1 결과: $resultDefault - 호출 스레드: ${Thread.currentThread().name}")

            val resultIO = deferred2.await()
            println("deferred2 결과: $resultDefault - 호출 스레드: ${Thread.currentThread().name}")

            stopWatch.stop()
            println("소요 시간 : ${stopWatch.totalTimeMillis} ms")
            println("Main 종료 - 실행 스레드: ${Thread.currentThread().name}")
        }
    }

    private fun doSomething(dispatchersName: String): String {
        println("[$dispatchersName] 시작 - 실행 스레드: ${Thread.currentThread().name}")
        // 2,000 ms 대기
//        runBlocking { delay(2000) }
        Thread.sleep(2000)
        println("[$dispatchersName] 완료 - 실행 스레드: ${Thread.currentThread().name}")
        return "Result from $dispatchersName"
    }
}