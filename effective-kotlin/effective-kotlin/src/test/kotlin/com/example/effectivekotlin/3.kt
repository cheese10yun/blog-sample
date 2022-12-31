package com.example.effectivekotlin

import kotlin.properties.Delegates
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class `3` {

    class AAA {
        var token: String? = null
            get() {
                println("token returned value $field")
                return field
            }
            set(value) {
                println("token changed from $field to $value")
                field = value
            }
    }

    class BBB {
        var token: String? by LoggingProperty(null)
        var attempts: Int by LoggingProperty(0)
    }

    @Test
    fun `None delegate`() {
        val aaa = AAA()
        aaa.token = "123"
        aaa.token
    }

    @Test
    fun `delegate`() {
        val bbb = BBB()
        bbb.token = "123"
        bbb.token
    }

    private class LoggingProperty<T>(var value: T) {

        operator fun getValue(
            thisRef: Any?,
            prop: KProperty<*>,
        ): T {
            println("${prop.name} returned value $value")
            return value
        }

        operator fun setValue(
            thisRef: Any?,
            prop: KProperty<*>,
            newValue: T,
        ) {
            val name = prop.name
            println("token changed from $value to $newValue")
        }
    }

    class CCC(
        email: String,
    ) {
        var email: String by Delegates.vetoable(email) { _, oldValue, newValue ->
            val b = oldValue == newValue
            when {
                b -> println("동일한 값 데이터베이스 반영 없음")
                else -> println("동일한 값 데이터베이스 반영")
            }

            return@vetoable b
        }
    }

    @Test
    fun `vetoable`() {
        val ccc = CCC("123@asd.com")

        ccc.email = "123@asd.com"
        println(ccc.email)

        ccc.email = "new@asd.com"
        println(ccc.email)
    }


}


class MoreBiggerInt(initValue: Int) {
    var value: Int by Delegates.vetoable(initValue) { property, oldValue, newValue ->
        val result = newValue > oldValue
        if (result) {
            println("더 큰 값이므로 값을 변경합니다.")
        } else {
            println("작은 값이므로 변경을 취소합니다.")
        }
        result
    }
}

class Employee {
    private val id: Int = 2

    override fun toString(): String {
        return "Employee(id=$id)"
    }

    private fun privateFunction() {
        println("Private function called")
    }

    @Test
    fun `call private function`() {
        val employee = Employee()
        callPrivateFunction(employee)

        changeEmployeeId(employee, 123)
        println(employee)
    }
}

fun callPrivateFunction(employee: Employee) {
    employee::class.declaredMemberFunctions
        .first { it.name == "privateFunction" }
        .apply { isAccessible = true }
        .call(employee)
}

fun changeEmployeeId(employee: Employee, newId: Int) {
    employee::class.java.getDeclaredField("id")
        .apply { isAccessible = true }
        .set(employee, newId)
}


//class `Generic`{
//    open class Dog
//    class Puppy: Dog()
//    class Hound: Dog()
//
//    class Box<out T> {
//        private var value: T? = null
//
//        // 코틀린에서는 사용할 수 없는 코드
//        fun set(value: T) {
//            this.value = value
//        }
//
//        fun get(): T = value ?: error("Value not set")
//    }
//
//    @Test
//    fun name() {
//        val puppyBox = Box<Puppy>()
//        val dogBox = Box<Dog> = puppyBox
////        dogBox.set(Hound()) // 하지만 Puppy를 위한 공간 입니다.
//
////        val dogHouse = Box<Dog>()
////        val dogBox: Box<Dog> = puppyBox
////        dogBox.set(Hound()) // 하지만 Dog를 위한 공간입니다.
////        dogBox.set(42) // 하지만 Dog를 위한 공간입니다.
//    }
//}

interface MyList<T> {

    companion object {
        fun <T> of(vararg elements: T): MyList<T>? {
            //...
            return null
        }
    }
}

class MyLinkedList<T>(
    val head: T,
    val tail: MyLinkedList<T>?,
) : MyList<T> {
    // ...
}

fun `of test`() {
    MyList.of(123)
}

class `6` {

    data class Player(
        val id: Int,
        val name: String,
        val pints: Int,
    )

    @Test
    fun `copy fun`() {
        val player = Player(
            id = 1,
            name = "Jin",
            pints = 150,
        )

        val copyPlayer = player.copy(name = "Kean")

        // Player(id=1, name=Kean, pints=150)
        println(copyPlayer)
    }

    @Test
    fun `componentN fun`() {
        val player = Player(
            id = 1,
            name = "Jin",
            pints = 150,
        )

        val (id, name, pts) = player

        val payerId = id
        val payerName = name
        val payerPoints = pts

        println("payerId: $payerId")
        println("payerName: $payerName")
        println("payerPoints: $payerPoints")
    }

    @Test
    fun `componentN fun2`() {
        val visited = listOf("China", "Russia", "India")
        val (first, second, third) = visited
        // China, Russia, India
        println("$first, $second, $third")

        val trip = mapOf(
            "China" to "Tianjin",
            "Russia" to "Petersburg",
            "India" to "Rishikesh",
        )

        for ((country, city) in trip) {
            // We loved Tianjin in China
            // We loved Petersburg in Russia
            // We loved Rishikesh in India
            println("We loved $city in $country")
        }
    }
}

class `컬랙션 처리 단수를 제한 하라` {

    data class Student(val name: String?)

//    // 작동합니다.
//    fun List<Student>.getNames(): List<String> = this
//        .map { it.name }
//        .filter { it != null }
//        .map { it!! }
//
//    // 더 좋습니다.
//    fun List<Student>.getNames(): List<String> = this
//        .map { it.name }
//        .filterNotNull()
//
//    // 가장 좋습니다.
//    fun List<Student>.getNames(): List<String> = this
//        .mapNotNull { it.name }
}

class `성능 테스트` {
    lateinit var list: List<Int>
    lateinit var array: IntArray

    @BeforeEach
    fun setUp() {

        list = List(1_000_000) { it }
        array = IntArray(1_000_000) { it }
    }

    @Test
    fun `list`() {
        // 평균 적으로 1,260,593 ns
         list.average()
    }

    @Test
    fun `array`() {
        // 평균 적으로 868 509 ns
        array.average()
    }
}
