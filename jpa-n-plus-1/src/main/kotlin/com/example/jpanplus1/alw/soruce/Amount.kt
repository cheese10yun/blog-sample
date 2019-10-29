package com.example.jpanplus1.alw.soruce

import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Embeddable


@Embeddable
data class Amount(
        @Column(name = "amount")
        private val amount: BigDecimal
) {

    constructor(value: Number) : this(BigDecimal.valueOf(value.toLong()))

    constructor(value: String) : this(value.toBigDecimal())

    val value: BigDecimal
        get() = amount.setScale(4)
}

