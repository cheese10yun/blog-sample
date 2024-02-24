package com.example.kotlincoroutine.lecture

import kotlin.reflect.KClass

fun main() {
    val kClass: KClass<Sample1> = Sample1::class

    val ref = Sample1()
    val kClass1: KClass<out Sample1> = ref::class

    val kotlin: KClass<out Any> = Class.forName("kotlin.reflect.KClass").kotlin
}

class Sample1 {}



