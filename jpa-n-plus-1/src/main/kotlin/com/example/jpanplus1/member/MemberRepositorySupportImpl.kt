package com.example.jpanplus1.member

import com.example.jpanplus1.copon.QCoupon
import com.example.jpanplus1.order.QOrder
import com.querydsl.core.types.Projections
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional(readOnly = true)
class MemberRepositorySupportImpl : QuerydslRepositorySupport(Member::class.java), MemberRepositorySupport {

    val qMember: QMember = QMember.member
    val qCoupon: QCoupon = QCoupon.coupon
    val qOrder: QOrder = QOrder.order

    override fun findMemberAll(): MutableList<MemberDto> {
        return from(qMember)
                .select(Projections.constructor(MemberDto::class.java,
                        qMember.id,
                        qMember.email,
                        qMember.name,
                        qMember.createdAt,
                        qMember.updatedAt
                ))
                .leftJoin(qMember.coupons, qCoupon)
                .leftJoin(qMember.orders, qOrder)
                .fetch()
    }

    override fun findMemberww() {
        val members = from(qMember)
                .select(qMember.id,
                        qMember.email,
                        qMember.name,
                        qOrder,
                        qMember.createdAt,
                        qMember.updatedAt
                ).leftJoin(qMember.orders, qOrder)
                .distinct()
                .fetch()

        for (member in members) {
            println("member name is : ${member.get(qMember.name)}")

            println(member.get(qMember.orders.isEmpty))

        }
    }
}