package com.example.mongostudy.dbref

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class PostTest {

    val client = HttpClient(CIO)

    @Test
    fun `performance lookup test`() {
        val responseTimes = mutableListOf<Long>()
        val limit = 5000

        for (i in 1..11) {
            val time = runBlocking {
                measureTimeMillis {
                    // GET 요청 실행 (응답 본문은 사용하지 않음)
                    client.get("http://localhost:8080/posts/lookup") {
                        parameter("limit", limit)
                    }
                }
            }
            println("Call $i response time: ${time}ms")
            if (i != 1) {
                responseTimes.add(time)
            }
        }

        // 평균 응답 시간 계산 (ms 단위)
        val averageTime = if (responseTimes.isNotEmpty()) responseTimes.average() else 0.0
        println("Average response time: ${averageTime}ms")

        client.close()
    }

    @Test
    fun `performance post-with-author test`() {
        val responseTimes = mutableListOf<Long>()
        val limit = 5000

        for (i in 1..11) {
            val time = runBlocking {
                measureTimeMillis {
                    // GET 요청 실행 (응답 본문은 사용하지 않음)
                    client.get("http://localhost:8080/posts/post-with-author") {
                        parameter("limit", limit)
                    }
                }
            }
            println("Call $i response time: ${time}ms")
            if (i != 1) {
                responseTimes.add(time)
            }
        }

        // 평균 응답 시간 계산 (ms 단위)
        val averageTime = if (responseTimes.isNotEmpty()) responseTimes.average() else 0.0
        println("Average response time: ${averageTime}ms")

        client.close()
    }

    @Test
    fun `performance post-only test`() {
        val responseTimes = mutableListOf<Long>()
        val limit = 5000

        for (i in 1..11) {
            val time = runBlocking {
                measureTimeMillis {
                    // GET 요청 실행 (응답 본문은 사용하지 않음)
                    client.get("http://localhost:8080/posts/post-only") {
                        parameter("limit", limit)
                    }
                }
            }
            println("Call $i response time: ${time}ms")
            if (i != 1) {
                responseTimes.add(time)
            }
        }

        // 평균 응답 시간 계산 (ms 단위)
        val averageTime = if (responseTimes.isNotEmpty()) responseTimes.average() else 0.0
        println("Average response time: ${averageTime}ms")

        client.close()
    }
}