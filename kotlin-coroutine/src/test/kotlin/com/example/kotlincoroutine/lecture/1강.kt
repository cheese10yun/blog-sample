package com.example.kotlincoroutine.lecture

import kotlinx.coroutines.yield
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class `1강` {
}

fun main() = runBlocking{
    println("START")
    launch {
        println("Hello")
    }
    println("END")
}

suspend fun newRoutine() {
    val num1 = 1
    val num2 = 2
    yield()
    println("${num1 + num2}")
}