package com.example.kotlinjunit5

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

internal class QuarterTest {

    @ParameterizedTest
    @EnumSource(Quarter::class)
    internal fun `분기의  value 값은 1 ~ 4 값이다`(quarter: Quarter) {
        assertThat(quarter.value in 1..4).isTrue()
    }

    @ParameterizedTest
    @EnumSource(value = Quarter::class, names = ["Q1", "Q2"])
    internal fun `names을 통해서 특정 enum 값만 가져올 수 있다`(quarter: Quarter) {
        assertThat(quarter.value in 1..2).isTrue()
    }
}