package com.example.kotlinjava8

import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit
import java.util.stream.Stream

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 2, jvmArgs = ["-Xms4G", "-Xmx4G"])
@Measurement(iterations = 2)
@Warmup(iterations = 3)


class ParallelStreamBenchmark {

    val n = 10_000_000L // 1백만

    @Benchmark
    fun asd() {


    }

}