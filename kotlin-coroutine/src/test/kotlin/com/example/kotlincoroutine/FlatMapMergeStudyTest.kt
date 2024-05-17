package com.example.kotlincoroutine

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.util.StopWatch
class FlatMapMergeStudyTest {


    private val intRange = 1..100

    @Test
    fun flatMapMergeWork(): Unit = runBlocking{
        val stopWatch = StopWatch()
        val flatMapMergeStudy = FlatMapMergeStudy()

        stopWatch.start()
        flatMapMergeStudy.flatMapMergeWork(intRange)

        stopWatch.stop()

        println(stopWatch.totalTimeMillis)
    }

    @Test
    fun flatMapMergeWork2() {
        val stopWatch = StopWatch()
        val flatMapMergeStudy = FlatMapMergeStudy()

        stopWatch.start()
        flatMapMergeStudy.flatMapMergeWork2(intRange)

        stopWatch.stop()
        println(stopWatch.totalTimeMillis)
    }
}