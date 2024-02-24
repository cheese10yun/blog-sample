package com.example.kotlincoroutine.lecture

import kotlin.reflect.KClass

fun main(exec: () -> Unit) {
//    val filter: StringFilter =  { s -> s.startsWith("A") } // 컴파일 오류
    val filter: StringFilter = StringFilter { s -> s.startsWith("A") } // StringFilter 명시하면 가능
//    val num1 = 1
//    val num2 = 2
//    val result = add(num1, num2)

    repeat(2, exec)

    consumeFilter({ s -> s.startsWith("A") })
}

fun consumeFilter(filter: StringFilter) {}

//inline fun add(num1: Int, num2: Int): Int {
//    return num1 + num2
//}

inline fun repeat(times: Int, noinline exec: () -> Unit) {

    for (i in 1..times) {
        exec()
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FILE, AnnotationTarget.CLASS)
@Repeatable
annotation class Shape(
    val text: String,
    val number: Int,
    val clazz: KClass<*>
)

@Shape("a", 1, Sample::class)
@Shape("b", 1, Sample::class)
class Sample{

}