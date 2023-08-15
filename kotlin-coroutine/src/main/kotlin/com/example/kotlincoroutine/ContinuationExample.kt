package com.example.kotlincoroutine

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.lang.IllegalStateException

private val log = kLogger()

fun main() {
    var visied = false
    val continuation = object : Continuation<Int> {
        override val context: CoroutineContext
            get() = EmptyCoroutineContext

        override fun resumeWith(result: Result<Int>) {
            if (visied) {
                log.info("Result: $result")
            } else {
                log.info("Visit Now")
                visied = true
            }
        }
    }

    continuation.resume(10)
    continuation.resume(10)
    continuation.resumeWithException(IllegalStateException())
}