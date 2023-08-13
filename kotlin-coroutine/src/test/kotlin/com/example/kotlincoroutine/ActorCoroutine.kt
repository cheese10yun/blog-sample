package com.example.kotlincoroutine

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.delay
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger


class ActorCoroutine {

    private var counter = 0
    private val context = newSingleThreadContext("counterActor")

    fun getCounter() = counter


    @Test
    fun `코루틴 단일 스레드로 한정`(): Unit = runBlocking {
        val context = newSingleThreadContext("counter")
        val workerA = asyncIncrement(2_000)
        val workerB = asyncIncrement(100)

        workerA.await()
        workerB.await()

        print("Counter [${getCounter()}]")
    }

    val actorCounter = GlobalScope.actor<Void?> {
        for (msg in channel) {
            counter++
        }
    }


    fun asyncIncrement(by: Int) = GlobalScope.async(context) {
        for (i in 0 until by) {
            actorCounter.send(null)
        }
    }
}

class ActorCoroutine2 {

    private var counter = 0
    private val context = newSingleThreadContext("counterActor")

    fun getCounter() = counter


    @Test
    fun `코루틴 단일 스레드로 한정`(): Unit = runBlocking {
        val context = newSingleThreadContext("counter")
        val workerA = asyncIncrement(2000)
        val workerB = asyncIncrement(200)
        val workerC = asyncDecrement(2000)

        workerA.await()
        workerB.await()
        workerC.await()

        print("Counter [${getCounter()}]")
    }

    val actorCounter = GlobalScope.actor<Action> {
        for (msg in channel) {
            println("actorCounter Current Thread ${Thread.currentThread().name}")
            when (msg) {
                Action.INCREASE -> counter++
                Action.DECREASE -> counter--
            }

        }
    }


    fun asyncIncrement(by: Int) = GlobalScope.async(context) {
        for (i in 0 until by) {
            println("asyncIncrement Current Thread ${Thread.currentThread().name}")
            actorCounter.send(Action.INCREASE)
        }
    }

    fun asyncDecrement(by: Int) = GlobalScope.async(context) {
        for (i in 0 until by) {
            println("asyncDecrement Current Thread ${Thread.currentThread().name}")
            actorCounter.send(Action.DECREASE)
        }
    }
}

class ActorCoroutine3 {

    @Test
    fun `actor buffer`(): Unit = runBlocking {
        val bufferedPrinter = actor<String>(capacity = 10) {
            for (msg in channel)
                println(msg)
        }

        bufferedPrinter.send("hello")
        bufferedPrinter.send("world")

        bufferedPrinter.close()

    }
}

class ActorCoroutine4 {

    val dispatcher = newFixedThreadPoolContext(3, "pool")
    val actor = GlobalScope.actor<String>(dispatcher) {
        for (msg in channel) {
            println("Running in ${Thread.currentThread().name}")
        }
    }

    @Test
    fun `actor buffer`(): Unit = runBlocking {
        for (i in 1..10) {
            actor.send("a")
        }
    }
}

class ActorCoroutine5 {

    val dispatcher = newFixedThreadPoolContext(3, "pool")
    val actor = GlobalScope.actor<String>(start = CoroutineStart.LAZY) {
        for (msg in channel) {
            println("Running in ${Thread.currentThread().name}")
        }
    }

    @Test
    fun `actor buffer`(): Unit = runBlocking {
        for (i in 1..10) {
            actor.send("a")
        }
    }
}

class ActorCoroutine6 {

    val mutex = Mutex()
    var counter = 0

    @Test
    fun `코루틴 단일 스레드로 한정`(): Unit = runBlocking {
        val context = newSingleThreadContext("counter")
        val workerA = asyncIncrement(2000)
        val workerB = asyncIncrement(200)

        workerA.await()
        workerB.await()

        print("Counter: [${counter}]")
    }

    fun asyncIncrement(by: Int) = GlobalScope.async() {
        for (i in 0 until by) {
            mutex.withLock {
                counter++
            }
        }
    }
}

class ActorCoroutine7 {

    @Test
    fun `코루틴 단일 스레드로 한정`(): Unit = runBlocking {
        val mutex = Mutex()
        mutex.lock() // 잠금이 이미 설정된 경우 일시 중단된다.
        print("I am now an atomic block")
        mutex.unlock() // 이것은 중단되지 않는다.
    }

    suspend fun asd() {
        val mutex = Mutex()
        mutex.lock()
        mutex.isLocked // true
        mutex.unlock()
    }

    suspend fun asd2() {
        val mutex = Mutex()
        mutex.tryLock() // true
        mutex.unlock()
    }
}

class ActorCoroutine8 {
    @Volatile
    var shutdownRequested = false
}

class Something {
    @Volatile
    private var type = 0
    private var title = ""

    fun setTitle(newTitle: String) {
        when (type) {
            0 -> title = newTitle
            else -> throw Exception("Invalid State")
        }
    }
}

class DataProcessor {
    @Volatile
    private var shutdownRequested = false

    fun shutdown() {
        shutdownRequested = true
    }

    fun process(){
        while(shutdownRequested.not()){
            // process away
        }
    }
}

class ActorCoroutine_9 {

    var counter = AtomicInteger()

    fun asyncIncrement(by: Int) = GlobalScope.async() {
        for (i in 0 until by) {
            counter.incrementAndGet()
        }
    }
}

class ActorCoroutine_10 {




    @Test
    fun suspendTest2() :Unit = runBlocking {

        suspend2()
        suspend1()
    }

    suspend fun suspend2(){
        println("suspend2 start")
        delay(1000)
        println("suspend2 end")
    }

    suspend fun suspend1(){
        println("suspend1 start")
        println("suspend1 end")
    }

}

class ActorCoroutine_ {

}


enum class Action {
    INCREASE,
    DECREASE
}