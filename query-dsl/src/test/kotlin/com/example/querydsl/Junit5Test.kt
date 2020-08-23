package com.example.querydsl

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.util.StopWatch


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class Junit5Test {

    private val ids = (1..10000000).map { it }.toList()

    @Test
    internal fun `stream`() {
        val stopWatch = StopWatch("stream")

        stopWatch.start("stream")
        val count = ids.stream().map {
            it
        }.count()

        println("count: $count")



        stopWatch.stop()
        printStopWatch(stopWatch)
    }

    @Test
    internal fun `parallelStream`() {
        val stopWatch = StopWatch("parallelStream")

        stopWatch.start("parallelStream")
        val count = ids.parallelStream().map {
            it
        }.count()

        println("count: $count")

        stopWatch.stop()
        printStopWatch(stopWatch)


    }

    private fun printStopWatch(stopWatch: StopWatch) {
//        println(stopWatch.shortSummary())
//        println(stopWatch.totalTimeMillis)
        println(stopWatch.prettyPrint())
        println(stopWatch.totalTimeSeconds)
//        println(stopWatch.lastTaskTimeMillis)
    }
}
