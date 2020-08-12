package com.example.querydsl.repository.payment

import com.example.querydsl.SpringBootTestSupport
import com.example.querydsl.domain.Payment
import com.example.querydsl.repository.member.MemberRepository
import com.example.querydsl.service.Coupon
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class JpaTest(
    private val paymentRepository: PaymentRepository,
    private val memberRepository: MemberRepository,
    private val couponRepository: CouponRepository
) : SpringBootTestSupport() {

    @Test
    internal fun `특정 테스트를 하기위해서는 외부 dependency가 필요하다`() {
        // 특정 테스트를 하기 위해서 많은 디펜던시가 필요하다.
        paymentRepository.save(Payment(BigDecimal.TEN))
//        memberRepository.save(Member("username", 10, Team("team-name")))
        couponRepository.save(Coupon(BigDecimal.TEN))

        // 특정 서비스가 여러 entity rows를 변경할때 아래와 같은 조회로 Then 이어가야 합니다.
        // paymentRepository.findBy... epository 메서드는 없는데??...
        // memberRepository.findBy...
        // couponRepository.findBy...
    }
}