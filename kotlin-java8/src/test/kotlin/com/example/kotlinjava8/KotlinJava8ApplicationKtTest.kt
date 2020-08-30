package com.example.kotlinjava8

import org.junit.jupiter.api.Test
import java.util.*
import java.util.stream.Collectors

internal class KotlinJava8ApplicationKtTest {

    @Test
    internal fun `flatMap`() {
        val listOf = listOf("Hello", "World")
        val collect = listOf.stream()
            .map { a: String -> a.split("").toTypedArray() }
            .flatMap { Arrays.stream(it) }
            .distinct()
            .collect(Collectors.toList())
    }

    @Test
    internal fun reduce() {
        val listOf = listOf(1, 2, 3, 4, 5, 6)
        val sum = listOf.stream()
            .reduce { a, b -> a + b }
            .get()
    }

    @Test
    internal fun `reduce max`() {
        val listOf = listOf(1, 2, 3, 4, 5, 6)
        listOf.stream()
            .reduce(Integer::max)
//            .reduce { a: Int, b: Int -> Integer.max(a, b) }
    }

    @Test
    internal fun `check processor`() {
        val availableProcessors = Runtime.getRuntime().availableProcessors()
        println(availableProcessors)
    }
}