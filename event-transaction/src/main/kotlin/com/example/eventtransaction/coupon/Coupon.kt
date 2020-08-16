package com.example.eventtransaction.coupon

import com.example.eventtransaction.EntityAuditing
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "coupon")
class Coupon(
    @Column(name = "amount", nullable = false)
    var amount: BigDecimal,

    @Column(name = "member_id", nullable = false)
    var memberId: Long
) : EntityAuditing() {

}

interface CouponRepository : JpaRepository<Coupon, Long>

@Service
class CouponIssueService(
    private val couponRepository: CouponRepository
) {

    @Transactional
    fun issueSignUpCoupon(memberId: Long) {
        couponRepository.save(Coupon(100.toBigDecimal(), memberId))
    }
}