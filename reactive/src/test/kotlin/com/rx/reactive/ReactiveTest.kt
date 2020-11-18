package com.rx.reactive

import io.reactivex.rxkotlin.toFlowable
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.util.StopWatch

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class ReactiveTest(
    private val orderRepository: OrderRepository
) {

    val sampleApi = SampleApi()

    @Test
    internal fun name() {
        val stopWatch = StopWatch()
        stopWatch.start()

        (1..1_000)
            .toFlowable()
            .parallel()
            .runOn(Schedulers.io())
            .map {
                val result = sampleApi.doSomething()
                result
            }
            .sequential()
            .blockingSubscribe {

                when {
                    it -> Order(OrderStatus.COMPLETED)
                    else -> Order(OrderStatus.FAILED)
                }
            }

        stopWatch.stop()

        println(stopWatch.prettyPrint())
        println(stopWatch.totalTimeSeconds)
        println(stopWatch.totalTimeMillis)
    }

    @Test
    internal fun `동기적인 작업`() {

        val stopWatch = StopWatch()

        stopWatch.start()

        (1..1_000)
            .forEach {
                val result = sampleApi.doSomething()

                when {
                    result -> Order(OrderStatus.COMPLETED)
                    else -> Order(OrderStatus.FAILED)
                }
            }

        stopWatch.stop()

        println(stopWatch.prettyPrint())
        println(stopWatch.totalTimeSeconds)
        println(stopWatch.totalTimeMillis)

    }

    @Test
    internal fun asdasd() {
        (1..10_000)
            .toObservable()
            .subscribeOn(Schedulers.io())
            .map {
                println("Mapping $it ${Thread.currentThread().name}")
                it
            }
            .observeOn(Schedulers.io())
            .subscribe {
                println("Received $it ${Thread.currentThread().name}")
            }

        runBlocking { delay(5000) }
    }
}