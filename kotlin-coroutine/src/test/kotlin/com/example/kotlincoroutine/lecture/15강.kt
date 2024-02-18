package com.example.kotlincoroutine.lecture

fun main(exec: () -> Unit) {
//    val num1 = 1
//    val num2 = 2
//    val result = add(num1, num2)

    repeat(2, exec)
}

//inline fun add(num1: Int, num2: Int): Int {
//    return num1 + num2
//}

inline fun repeat(times: Int, noinline exec: () -> Unit) {
    for (i in 1..times) {
        exec()
    }
}