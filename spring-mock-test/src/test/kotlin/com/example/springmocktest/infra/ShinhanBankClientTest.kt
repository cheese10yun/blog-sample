package com.example.springmocktest.infra

import com.example.springmocktest.SpringContextTestSupport
import org.assertj.core.api.BDDAssertions.thenThrownBy
import org.junit.jupiter.api.Test

internal class ShinhanBankClientTest(
    private val shinhanBankClient: ShinhanBankClient
) : SpringContextTestSupport() {

    @Test
    internal fun verifyAccountHolder() {
        //given
        val accountNumber = "110-3333-3333"
        val accountHolder = "jin"

        //when & then

        thenThrownBy {
            shinhanBankClient.verifyAccountHolder(accountNumber, accountHolder)
        }.isInstanceOf(IllegalArgumentException::class.java)


    }
}