package com.example.batch.domain.order.domain

import java.math.BigDecimal
import javax.persistence.*


@Entity
@Table(name = "payment")
@Access(AccessType.FIELD) // 용도는 ?
data class Payment(

        @Column(name = "amount", nullable = false)
        var amount: BigDecimal

) : EntityAuditing() {


}