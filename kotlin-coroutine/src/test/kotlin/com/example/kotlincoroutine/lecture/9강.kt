package com.example.kotlincoroutine.lecture

import kotlin.properties.Delegates
import kotlin.reflect.KProperty

class `9강` {
}


class LazyInitProperty<T>(val init: () -> T) {
    private var _value: T? = null
    val value: T
        get() {
            if (_value == null) {
                this._value = init()
            }
            return _value!!
        }

    operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        return value
    }
}


class Person1 {
    private val delegateProperty = LazyInitProperty {
        Thread.sleep(2_000)
        "홍길동"
    }

//    val name: String
//        get() = delegateProperty.value

    val name: String by LazyInitProperty {
        Thread.sleep(2_000)
        "홍길동"
    }
}

class Person4 {
    var age: Int by Delegates.observable(20) { _, oldValue, newValue ->

        if (oldValue != newValue) {
            println("oldValue: $oldValue, newValue: $newValue")
        }
    }
}

//class Person5 {
//    var age: Int by Delegates.vetoable(20) { _, _, oldValue ->
//
//    }
//}

class Person6 {
    @Deprecated("이름을 사용하지 않는다", ReplaceWith("age"))
    var num: Int = 0

    val age: Int by this::num
}

class Person7(map: Map<String, Any>) {
    val name: String by map
    val age: Int by map
}


data class Fruit(val name: String, val price: Long)

fun compute(num1: Int, num2: Int, operation: (Int, Int) -> Int): Int {
    return operation(num1, num2)
}

fun main() {

    // 람다식
    compute(5, 2) { a, b -> a + b }
    compute(
        num1 = 5,
        num2 = 2,
        operation = { a, b -> a + b }
    )

    // 익명 함수
    compute(5, 2, fun(a: Int, b: Int): Int {
        return a + b
    })


    iterate(listOf(1, 2, 3, 4, 5)) { num ->
        if (num == 3) {
            return // 'return' is not allowed here
        }
        println(num)


        val add = fun Int.(other: Long): Int = this + other.toInt()


        add.invoke(5, 3L)
        add(5, 3L)

    }


//    val p = Person4()
//
//    p.age = 30
//    p.age = 30
//
//
//    val fruits = emptyList<Fruit>()
//
//    fruits
//        .asSequence()
//        .filter { it.name > "사과" }
//        .map { it.price }
//        .take(10_000)
//        .average()


}


inline fun iterate(numbers: List<Int>, exec: (Int) -> Unit) {
    for (number in numbers) {
        exec(number)
    }
}



