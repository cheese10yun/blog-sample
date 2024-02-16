package com.example.kotlincoroutine.a3

import com.example.kotlincoroutine.a1.Animal

class Cage2<T> {

    private val animals: MutableList<T> = mutableListOf()

    fun getFirst(): T {
        return animals.first()
    }

    fun put(animal: T) {
        this.animals.add(animal)
    }

    fun moveForm(otherCage: Cage2<out T>) {
        this.animals.addAll(otherCage.animals)
    }

    fun moveForm(otherCage: Cage2<in T>) {
        otherCage.animals.addAll(this.animals)
    }
}

class Cage5<T>(
    private val animals: MutableList<T> = mutableListOf()
) where  T : Animal, T : Comparable<T> {

    fun getFirst(): T {
        return animals.first()
    }

    fun put(animal: T) {
        this.animals.add(animal)
    }

//    fun moveForm(otherCage: Cage5<T>) {
//        this.animals.addAll(otherCage.animals)
//    }
//
//    fun moveForm(otherCage: Cage5<T>) {
//        otherCage.animals.addAll(this.animals)
//    }

}

fun main() {
    val num = 3
    num.toSuperString()
    // 정상 동작
    println("${num::class.java}: $num")

    val str = "ABC"
    str.toSuperString()
    // 정상 동작
    println("${str::class.java}: $str")
}

//private fun <T> T.toSuperString() {
//
//    // 오류 발생 Cannot use 'T' as reified type parameter. Use a class inst
////    println("${T::class.java}: $this")
//}

inline fun <reified T> T.toSuperString() {
    println("${T::class.java}: $this")
}
