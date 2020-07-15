package com.example.querydsl.j

import org.junit.jupiter.api.Test
import java.util.function.Function

interface RunSomething {
    fun doit(): Unit
}

class Plus10 : Function<Int, Int> {
    override fun apply(t: Int): Int {
        return t.plus(10)
    }

}

internal class Plus10Test {
    @Test
    internal fun name() {

        val plus10 = Function<Int, Int> { it + 10 }
        val multiply2 = Function<Int, Int> { it * 2 }

        val compose = plus10.compose(multiply2)
        val andThen = plus10.andThen(multiply2)

        println("compose:  ${compose.apply(2)}")
        println("andThen:  ${andThen.apply(2)}")

    }
}