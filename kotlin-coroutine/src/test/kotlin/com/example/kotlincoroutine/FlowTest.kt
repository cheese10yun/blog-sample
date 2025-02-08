package com.example.kotlincoroutine

import java.util.concurrent.CountDownLatch
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

class FlowTest {

    @OptIn(FlowPreview::class)
    @Test
    fun `asFlow test`() {

        runBlocking {
            (1..100)
                .asFlow()
                .flatMapMerge(concurrency = 100) { value ->
                    flow {
                        println("Processing $value on thread ${Thread.currentThread().name}")
                        // 작업 시뮬레이션: 랜덤한 시간(delay)을 줌 (순서 보장이 필요 없으므로 delay 시간이 다를 수 있음)
                        delay((10..100L).random())
                        emit("Result $value")
                    }
                }
                .collect { result ->
                    println("Collected $result on thread ${Thread.currentThread().name}")
                }
            println("END")
        }
    }

    @Test
    fun testAsyncTasksWithoutBean() {
        // TaskExecutor를 직접 생성 및 초기화합니다.
        val taskExecutor = ThreadPoolTaskExecutor().apply {
            corePoolSize = 10
            maxPoolSize = 10
            setQueueCapacity(100)
            initialize()
        }

        // 10개의 작업 완료를 기다리기 위한 CountDownLatch 생성
        val latch = CountDownLatch(10)

        // 1부터 10까지 각각의 작업을 비동기적으로 실행
        for (i in 1..10) {
            taskExecutor.execute {
                println("[${Thread.currentThread().name}] Task $i 시작")
                // 작업을 시뮬레이션: 100밀리초 대기
                Thread.sleep(100)
                println("[${Thread.currentThread().name}] Task $i 종료")
                latch.countDown() // 작업 완료 후 CountDownLatch 카운트 감소
            }
        }

        // 모든 작업이 완료될 때까지 대기
        latch.await()
        println("모든 작업이 완료되었습니다.")
    }
}