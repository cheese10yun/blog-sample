package com.kotlin.cookbook.step6

import java.util.Locale
import org.junit.jupiter.api.Test

class AAA {

    @Test
    fun name() {
        (100 until 200).map { it * 2 }
            .first { it % 3 == 0 }
    }

    @Test
    fun `asSequence`() {
        (100 until 2_000_000).asSequence()
            .map { println("doubling $it"); it * 2 }
            .filter { println("filtering $it"); it % 3 == 0 }
            .first()
    }

    @Test
    fun processString(str: String?) {
        str?.let { it ->
            when {
                it.isEmpty() -> "Empty"
                it.isBlank() -> "Blank"
                else -> it.capitalize()
            }
        } ?: "Null"
    }
}