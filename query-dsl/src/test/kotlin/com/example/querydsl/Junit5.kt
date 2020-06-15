package com.example.querydsl

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class Junit5 {

    private var value = 0

    @Test
    internal fun `value add 1`() {
        value++

        println("value : $value")
        println("Junit5 : $this")
    }

    @Test
    internal fun `value add 2`() {
        value++

        println("value : $value")
        println("Junit5 : $this")
    }
}