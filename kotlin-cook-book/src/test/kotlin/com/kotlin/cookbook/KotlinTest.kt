package com.kotlin.cookbook

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

internal class KotlinTest {

    data class Person(
            val first: String,
            val middle: String?,
            val last: String
    )

    @Test
    internal fun `안전 호출 연산자와 엘비스 연산`() {
        val person = Person("first", null, "last")
        val middleLength = person.middle?.length ?: 0
        println(middleLength)
    }

    @Test
    internal fun `create vmpa using to function`() {
        val mapOf = mapOf("a" to 1, "b" to 2, "c" to 2)

        then(mapOf).anySatisfy { key, value ->
            then(key).isIn("a", "b", "c")
            then(value).isIn(1, 2)
        }
    }
}

class Task(val name: String, _priority: Int = DEFAULT_PRIORITY) {

    companion object {
        const val MIN_PRIORITY = 1 // (1)
        const val MAX_PRIORITY = 5 // (1)
        const val DEFAULT_PRIORITY = 3 // (1)
    }

    var priority = validPriority(_priority) // (2)
        set(value) {
            field = validPriority(value)
        }

    private fun validPriority(p: Int) = p.coerceIn(MIN_PRIORITY, MAX_PRIORITY) // (3)
}

class Task2(val name: String) {
    var priority = 3
        set(value) {
            field = value
        }

    val isLowPriority
        get() = priority < 3
}

class Customer(val name: String) {
    //    val message: List<String> = loadMessage()
    val message: List<String> by lazy { loadMessage() }
    private fun loadMessage(): List<String> {
        return listOf("1", "2", "3")
    }
}

class CustomerTest {

    @Test
    internal fun `none lazy, 객체 생성 시점에 loadMessage를 호출한다`() {
        // val message: List<String> = loadMessage()
        val customer = Customer("yun")
        customer.message
        println(customer)
    }

    @Test
    internal fun `lazy, 객체 생성 시점에 loadMessage를 호출하지 않고, 조회 시점까지 lazy하게 간다`() {
        // val message: List<String> = loadMessage()
        val customer = Customer("yun")
        customer.message
        println(customer)
    }
}

object Singleton {
    val myPriority = 3

    fun function() = "hello"
}

class SingletonTest() {

    @Test
    internal fun name() {
        Singleton.myPriority
    }
}