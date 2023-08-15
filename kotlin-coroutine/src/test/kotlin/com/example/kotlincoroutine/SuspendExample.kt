package com.example.kotlincoroutine

import kotlinx.coroutines.delay

class SuspendExample {
    suspend fun greet(){
        delay(100)
        println("Hello, World!")
    }
}