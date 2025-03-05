package com.example.mongostudy

import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Test
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking


class AAA {

    @Test
    fun `asdasdasd`() {
        runBlocking {
            println("Main 시작 - 실행 스레드: ${Thread.currentThread().name}")

            // async() 기본 컨텍스트 사용: runBlocking의 컨텍스트를 상속받으므로 같은 스레드(main)에서 실행됩니다.
            val deferredDefault = async() {
                contentQuery("Default")
            }

            // async(Dispatchers.IO): I/O 전용 스레드 풀에서 실행됩니다.
            val deferredIO = async() {
                contentQuery("IO")
            }

            // 결과 대기
            val resultDefault = deferredDefault.await()
            println("[Default] 결과: $resultDefault - 호출 스레드: ${Thread.currentThread().name}")

            val resultIO = deferredIO.await()
            println("[IO] 결과: $resultIO - 호출 스레드: ${Thread.currentThread().name}")

            println("Main 종료 - 실행 스레드: ${Thread.currentThread().name}")
        }
    }

    fun contentQuery(contentAggregation: String): String {
        println("[$contentAggregation] 시작 - 실행 스레드: ${Thread.currentThread().name}")
        // 블로킹 작업을 모방 (예: 2초 대기)
        Thread.sleep(2000)
        println("[$contentAggregation] 완료 - 실행 스레드: ${Thread.currentThread().name}")
        return "Result from $contentAggregation"
    }
}



