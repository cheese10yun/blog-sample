package com.kotlin.cookbook.step2

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class PersonTest {


    @Test
    internal fun name() {
        val person = Person(
            first = "first",
            middle = null,
            last = "last"
        )

        val middleLength = person.middle?.length ?: 0
    }

    @Test
    internal fun `코틀린에서 중복 함수 변경 호출하기`() {
        // default 메서드에 따라 3개 메서드 구현

        val person = Person(
            first = "first",
            middle = null,
            last = "last"
        )
        person.addProduct(name = "name", price = 10, "DESC")
        person.addProduct(name = "name", price = 10)
        person.addProduct(name = "name")
    }
}