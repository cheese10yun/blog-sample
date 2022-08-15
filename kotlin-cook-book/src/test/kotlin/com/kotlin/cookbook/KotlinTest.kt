package com.kotlin.cookbook

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.Locale

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
    internal fun `create map using to function`() {
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

internal class Fold {

    @Test
    internal fun `fold sum`() {
        val numbers = intArrayOf(1, 2, 3, 4)
        val sum = sum(*numbers)
        println(sum) // 10
    }

    fun sum(vararg nums: Int) =
        nums.fold(0) { acc, n -> acc + n }

    @Test
    internal fun `reduce sum`() {
        val numbers = intArrayOf(1, 2, 3, 4)
        val sum = sumReduce(*numbers)
    }


    fun sumReduce(vararg nums: Int) =
        nums.reduce { acc, i ->
            println("acc: $acc, i: $i")
            acc + i
        }


    @Test
    internal fun associateWith() {
        val keys = 'a'..'f'
        val associate = keys.associate {
            it to it.toString().repeat(5).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }

        // {a=Aaaaa, b=Bbbbb, c=Ccccc, d=Ddddd, e=Eeeee, f=Fffff}
        println(associate)
    }

    data class Product(
        val name: String,
        var price: Double,
        var onSale: Boolean = false
    )

    @Test
    internal fun ifEmpty() {
        val products = listOf(Product("goods", 1000.0, false))
        val joinToString = products.filter { it.onSale }
            .map { it.name }
            .ifEmpty { listOf("none") }
            .joinToString(separator = ", ")

        println(joinToString)
    }
}

interface Dialable {
    fun dial(number: String): String
}

class Phone : Dialable {
    override fun dial(number: String): String = "Dialing $number"
}

interface Snappable {
    fun takePictrue(): String
}

class Camera : Snappable {
    override fun takePictrue() = "Taking Picture"
}

class SmartPhone(
    private val phone: Dialable = Phone(),
    private val camera: Snappable = Camera()
) : Dialable by phone, Snappable by camera

class SmartPhoneTest {

    @Test
    internal fun `dialing delegates to internal phone`() {
        val smartPhone = SmartPhone()
        val dial = smartPhone.dial("111")
        println(dial) // Dialing 111
    }

    @Test
    internal fun `Taking picture delegates to internal camera`() {
        val smartPhone = SmartPhone()
        val message = smartPhone.takePictrue()
        println(message) // Taking Picture
    }
}

data class Project(val map: MutableMap<String, Any>) {
    val name: String by map
    val priority: Int by map
    val completed: Boolean by map
}

class ProjectTest {

    @Test
    fun `use map delegate for project`() {
        val project = Project(
            mutableMapOf(
                "name" to "Lean Kotlin",
                "priority" to 5,
                "completed" to true
            )
        )

        println(project)
    }
}

