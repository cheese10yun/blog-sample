package com.example.springkotlin.domain.transaction

import java.util.*

enum class PaymentMethodType {

    CARD,
    CASH,
    BANKPAY,
    TOSS
    ;

    fun asd(): PaymentMethodType {
        return when (Random().nextInt(4)) {
            1 -> CARD
            2 -> CASH
            3 -> BANKPAY
            else -> TOSS
        }
    }
}

