package com.example.kotlincoroutine

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.DEFAULT_CONCURRENCY
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.util.StopWatch
import kotlin.system.measureTimeMillis

class FlatMapMergeStudyTest {

    //10
//50
//100
//500
//1,000
    private val intRange = 1..500

    private val log by logger()

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun createCoroutines(amount: Int) {
        val jobs = ArrayList<Job>()
        for (i in 1..amount) {
            jobs += GlobalScope.launch {
//                println("Started $i in ${Thread.currentThread().name}")
                delay(1000)
//                println("Finished $i in ${Thread.currentThread().name}")
            }
        }
        jobs.forEach { it.join() }
    }

    @Test
    @DelicateCoroutinesApi
    fun `코루틴 생성 테스트`() = runBlocking {
        println("${Thread.activeCount()} thread active at the start")

        val time = measureTimeMillis {
            createCoroutines(2)
        }

        println("${Thread.activeCount()} thread active at the end")
        println("Took $time ms")

        // 1, 4, 7
        // 100, 4, 15
        // 500, 4, 15
        // 1,000, 4, 15
        // 5,000, 4, 15
        // 10,000, 4, 15
        // 100,000, 4, 15

    }

    @Test
    fun getOrderFlow(): Unit = runBlocking {

        val intRange = 1..10_000
        val stopWatch = StopWatch()
        val flatMapMergeStudy = FlatMapMergeStudy()
        val orderRequests = (intRange).map { OrderRequest("$it") }

        log.info("${Thread.activeCount()} thread active at the start")
        stopWatch.start()
        val response = flatMapMergeStudy.getOrderFlow(orderRequests)
        stopWatch.stop()

        // 2,228ms
        println("===============")
        println(stopWatch.totalTimeMillis)
        println("===============")

        log.info("${Thread.activeCount()} thread active at the end")

//    println(response)
    }

    @Test
    fun getOrderFlow3(): Unit = runBlocking {
        val intRange = 1..1_000
        val stopWatch = StopWatch()
        val flatMapMergeStudy = FlatMapMergeStudy()
        val orderRequests = (intRange).map { OrderRequest("$it") }

        log.info("${Thread.activeCount()} thread active at the start")
        stopWatch.start()
        val response = flatMapMergeStudy.getOrderFlow3(orderRequests, 300)
        stopWatch.stop()

        // 2,228ms
        println("===============")
        println(stopWatch.totalTimeMillis)
        println("===============")

        log.info("${Thread.activeCount()} thread active at the end")

//    println(response)
    }

    @Test
    fun orderRequests(): Unit = runBlocking {
        val intRange = 1..10_000
        val stopWatch = StopWatch()
        val flatMapMergeStudy = FlatMapMergeStudy()
        val orderRequests = (intRange).map { OrderRequest("$it") }

        val cpuCount = Runtime.getRuntime().availableProcessors()
//        val concurrency = cpuCount * 2 // CPU 코어 수의 2배로 동시 실행할 코루틴 수를 설정
        val concurrency = 500

        log.info("${Thread.activeCount()} thread active at the start")
        stopWatch.start()
        val response = flatMapMergeStudy.getOrderFlow2(orderRequests, concurrency)
        stopWatch.stop()
        log.info("${Thread.activeCount()} thread active at the end")
        println("===============")
        println("size: ${response.size}, ${stopWatch.totalTimeMillis} ms")
        println("===============")

        /**
         * 500, default(16), 9,851ms
         * 500, 10, 15377ms,
         * 500, 20, 7741ms,
         * 500, 50, 3171,
         * 500, 100, 1641,
         * 500, 300, 700,
         * 500, 500, 390,
         */
    }

    @Test
    fun orderRequests4(): Unit = runBlocking {
        val intRange = 1..100
        val stopWatch = StopWatch()
        val flatMapMergeStudy = FlatMapMergeStudy()
        val orderRequests = (intRange).map { OrderRequest("$it") }

        val concurrency = 100

        log.info("${Thread.activeCount()} thread active at the start")
        stopWatch.start()
        val response = flatMapMergeStudy.getOrderFlow4(orderRequests, concurrency)
        stopWatch.stop()
        log.info("${Thread.activeCount()} thread active at the end")
        println("===============")
        println("size: ${response.size}, ${stopWatch.totalTimeMillis} ms")
        println("===============")

        /**
         * 1,000 rows test
         *
         * 16, 19278
         * 50, 6174
         * 100, 3146
         * 200, 1612
         * 300, 1312
         * 400, 1013
         * 500, 720
         * 700, 714
         * 800, 712
         */
    }

    @Test
    fun getOrderSync() {
        val stopWatch = StopWatch()
        val flatMapMergeStudy = FlatMapMergeStudy()
        val orderRequests = intRange.map { OrderRequest("$it") }

        stopWatch.start()
        val response = flatMapMergeStudy.getOrderSync(orderRequests)
        stopWatch.stop()
        println("===============")
        println(stopWatch.totalTimeMillis)
        println("===============")
//        println(response)
    }
}