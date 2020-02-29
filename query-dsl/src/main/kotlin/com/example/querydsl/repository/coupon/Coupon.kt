package com.example.querydsl.repository.coupon

import com.example.querydsl.domain.EntityAuditing
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