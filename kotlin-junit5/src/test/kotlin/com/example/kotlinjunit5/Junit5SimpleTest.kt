package com.example.kotlinjunit5

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

internal class Junit5SimpleTest {

    @ParameterizedTest
    @ValueSource(strings = ["", " "])
    internal fun `isBlank `(value: String) {
        print("value: $value ") // value:  value:
        assertThat(value.isBlank()).isTrue()
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3, 4])
    internal fun `ints values`(value: Int) {
        print("value: $value ") // value: 1 value: 2 value: 3 value: 4
    }

    @ParameterizedTest
    @CsvSource(
            "010-1234-1234,01012341234",
            "010-2333-2333,01023332333",
            "02-223-1232,022231232"
    )
    internal fun `전화번호는 '-'를 제거한다`(value: String, expected: String) {
        val valueReplace = value.replace("-", "")
        assertThat(valueReplace).isEqualTo(expected)
    }
}