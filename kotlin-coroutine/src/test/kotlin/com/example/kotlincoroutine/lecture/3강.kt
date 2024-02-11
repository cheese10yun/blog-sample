package com.example.kotlincoroutine.lecture

import com.example.kotlincoroutine.coroutine.printWithThread
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class `3강` {
}

fun main(): Unit = runBlocking {
    val job = launch(start = CoroutineStart.LAZY) {
        printWithThread("Hello launch")
    }

    delay(1_000L)
    job.start()
}

fun example1() {
    runBlocking {
        printWithThread("START")
        val job = launch {
            delay(2_000L) // yield()
            printWithThread("LAUNCH END")
        }
    }

    printWithThread("END")
}