package com.example.querydsl.domain

import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "payment")
data class Payment(

    @Column(name = "amount", nullable = false)
    var amount: BigDecimal

) : EntityAuditing() {


}