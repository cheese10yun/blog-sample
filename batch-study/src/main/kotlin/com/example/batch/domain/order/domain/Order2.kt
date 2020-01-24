package com.example.batch.domain.order.domain

import java.math.BigDecimal
import javax.persistence.*


@Entity
@Table(name = "orders2")
@Access(AccessType.FIELD) // 용도는 ?
data class Order2(

        @Column(name = "amount", nullable = false)
        var amount: BigDecimal

) : EntityAuditing() {


}