package com.example.querydsl.j


import org.junit.jupiter.api.Test
import java.util.*
import java.util.function.*

internal class Plus10Test {

    @Test
    internal fun Function() {
        val plus10 = Function<Int, Int> { it + 10 }
        val multiply2 = Function<Int, Int> { it * 2 }

        println("compose:  ${plus10.compose(multiply2).apply(2)}") // (2 * 2) + 10
        println("andThen:  ${plus10.andThen(multiply2).apply(2)}") // (10 + 2) * 2
    }

    @Test
    internal fun consumer() {
        val consumer = Consumer<Int> {
            println(it)
        }
        consumer.accept(1)
    }

    @Test
    internal fun supplier() {
        val supplier = Supplier { 10 }
        println(supplier.get())
    }

    @Test
    internal fun `predicate test`() {
        val startWithXX = Predicate { s: String -> s.startsWith("asd") }
        println(startWithXX.test("asd"))
    }

    @Test
    internal fun `function test`() {
        val fuc1 = Function { value: Int -> value + 10 }
        val fuc2 = Function { value: Int -> value * 2 }
        val apply = fuc1.andThen(fuc2).apply(10)
        println(apply)
    }

    @Test
    internal fun `asdsad`() {
        val function = Function<Int, String> {
            it.toString()
        }

        val apply = function.apply(10)
        println(apply)
    }




    @Test
    internal fun name() {

        val names = arrayOf("k", "c", "d")

        Arrays.sort(names, String::compareTo)

        names.sortBy {
            it
        }

        for (name in names) {
            println(name)
        }
    }
}
