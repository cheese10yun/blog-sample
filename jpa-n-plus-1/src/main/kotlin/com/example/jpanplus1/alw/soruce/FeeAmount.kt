package com.example.jpanplus1.alw.soruce

import javax.persistence.AttributeOverride
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.Embedded

@Embeddable
data class FeeAmount(

        @Embedded
        @AttributeOverride(name = "value", column = Column(name = "target_amount", nullable = false))
        val targetAmount: Amount,

        @Embedded
        @AttributeOverride(name = "value", column = Column(name = "fee_factor", nullable = false))
        val feeFactor: Amount
) {

}