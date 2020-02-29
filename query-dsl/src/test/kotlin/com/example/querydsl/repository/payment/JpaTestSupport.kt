package com.example.querydsl.repository.payment

import com.example.querydsl.SpringBootTestSupport
import com.example.querydsl.domain.Member
import com.example.querydsl.domain.Payment
import com.example.querydsl.domain.Team
import com.example.querydsl.repository.coupon.Coupon
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class JpaTestSupport(
    private val paymentRepository: PaymentRepository
) : SpringBootTestSupport() {

    @Test
    internal fun `entityManager를 이용해서 dependency가 최소화 `() {
        // 특정 테스트를 하기 위해서 많은 디펜던시가 필요하다.
        save(Payment(BigDecimal.TEN))
        val team = save(Team("team-ename"))
        save(Member("username", 10, team))
        save(Coupon(BigDecimal.TEN))

        // 특정 서비스가 여러 entity rows를 변경할때 아래와 같은 조회로 Then 이어가야 합니다.
        // paymentRepository.findBy... epository 메서드는 없는데??...
        // memberRepository.findBy...
        // couponRepository.findBy...
    }

    @Test
    internal fun `findUseForm`() {
        //given
        val targetAmount = 200.toBigDecimal()

        //when
        val payments = paymentRepository.findUseFrom(targetAmount)

        //then
        then(payments).anySatisfy {
            then(it.amount).isGreaterThan(targetAmount)
        }
    }

    @Test
    internal fun `findUseSelectForm`() {
        //given
        val targetAmount = 200.toBigDecimal()

        //when
        val payments = paymentRepository.findUseSelectFrom(targetAmount)

        //then
        then(payments).anySatisfy {
            then(it.amount).isGreaterThan(targetAmount)
        }
    }

    @Test
    internal fun `findUseSelect`() {
        //given
        val targetAmount = 200.toBigDecimal()

        //when
        val ids = paymentRepository.findUseSelect(targetAmount)

        //then
        then(ids).hasSizeGreaterThan(1)
    }
}