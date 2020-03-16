package com.example.querydsl.domain

import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "coupon")
data class Coupon(

    @Column(name = "amount", nullable = false)
    var amount: BigDecimal
) : EntityAuditing()