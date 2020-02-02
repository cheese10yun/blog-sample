package com.example.batch.domain.order.domain

import java.math.BigDecimal
import javax.persistence.*


@Entity
@Table(name = "payment")
data class Payment(

        @Column(name = "amount", nullable = false)
        var amount: BigDecimal

) : EntityAuditing() {


}