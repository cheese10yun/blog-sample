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
                .leftJoin(qMember.copons, qCoupon)
                .leftJoin(qMember.orders, qOrder)
                .select(Projections.constructor(MemberDto::class.java,
                        qMember.id,
                        qMember.email,
                        qMember.name,
                        qMember.orders,
                        qMember.copons,
                        qMember.createdAt,
                        qMember.updatedAt
                ))
                .fetch()
    }
}