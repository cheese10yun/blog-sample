package com.example.kotlinjunit5

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class AmountTest {

    @ParameterizedTest(name = "{index} Amount: {0} totalPrice: {1}")
    @MethodSource("providerAmount")
    internal fun `amount total price 테스트 `(amount: Amount, expectedTotalPrice: Int) {
        assertThat(amount.totalPrice).isEqualTo(expectedTotalPrice)
    }

    companion object {
        @JvmStatic
        fun providerAmount() = listOf(
                Arguments.of(Amount(1000, 2), 2000),
                Arguments.of(Amount(2000, 5), 10000),
                Arguments.of(Amount(4000, 5), 20000),
                Arguments.of(Amount(5000, 3), 15000)
        )
    }
}