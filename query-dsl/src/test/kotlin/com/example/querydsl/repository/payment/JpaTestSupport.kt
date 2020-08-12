package com.example.querydsl.repository.payment

import com.example.querydsl.SpringBootTestSupport
import com.example.querydsl.domain.*
import com.example.querydsl.service.Coupon

import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Transactional
internal class JpaTestSupport : SpringBootTestSupport() {

    @Test
    internal fun `entityManager를 이용해서 dependency가 최소화 `() {
        // 특정 테스트를 하기 위해서 많은 디펜던시가 필요하다.
        save(Payment(BigDecimal.TEN))
        save(Member("username", 10, save(Team("team-ename"))))
        save(Coupon(BigDecimal.TEN))

        // 특정 서비스가 여러 entity rows를 변경할때 아래와 같은 조회로 Then 이어가야 합니다.
        // paymentRepository.findBy... epository 메서드는 없는데??...
        // memberRepository.findBy...
        // couponRepository.findBy...

        val payments = query.selectFrom(QPayment.payment)
            .where(QPayment.payment.amount.gt(BigDecimal.TEN))
            .fetch()

        val members = query.select(QMember.member.age)
            .from(QMember.member)
            .where(QMember.member.age.gt(20))
            .fetch()

        val coupons = query.selectFrom(QCoupon.coupon)
            .where(QCoupon.coupon.amount.eq(123.toBigDecimal()))
            .fetch()
    }
}