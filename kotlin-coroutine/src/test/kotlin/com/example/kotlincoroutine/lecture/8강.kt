package com.example.kotlincoroutine.lecture

class `8강` {
}

class Person {

    lateinit var name: String

    val isKim: Boolean
        get() = name.startsWith("김")

    val maskingName: String
        get() = name.substring(0, 1) + "*".repeat(name.length - 1)

}