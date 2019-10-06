package com.example.kotlinjunit5.member

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Month
import org.junit.jupiter.params.provider.EnumSource



internal class MemberTest {

    @ParameterizedTest(name = "For example, year {0} is not supported.")
    @ValueSource(ints = [-1, -4])
    internal fun asd(year: Int) {

    }

    @ParameterizedTest
    @EnumSource(Month::class) // passing all 12 months
    fun getValueForAMonth_IsAlwaysBetweenOneAndTwelve(month: Month) {
        val monthNumber = month.value
        assertTrue(monthNumber in 1..12)
    }
}

