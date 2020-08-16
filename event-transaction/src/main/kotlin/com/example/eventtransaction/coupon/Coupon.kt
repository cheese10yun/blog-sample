package com.example.eventtransaction.coupon

import com.example.eventtransaction.EntityAuditing
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "coupon")
class Coupon(
    @Column(name = "amount", nullable = false)
    var amount: BigDecimal
) : EntityAuditing() {

}