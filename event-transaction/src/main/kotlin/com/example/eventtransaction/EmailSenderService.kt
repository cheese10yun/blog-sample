package com.example.eventtransaction

import com.example.eventtransaction.member.Member
import com.example.eventtransaction.order.Order
import org.springframework.stereotype.Service

@Service
class EmailSenderService {

    /**
     * 외부 인프라 서비스를 호출한다고 가정한다
     */
    fun sendOrderEmail(order: Order) {
        println(
            """
            주문자 이메일 : ${order.orderer.email}
            주문 가격 : ${order.productAmount}
            """.trimIndent()
        )
    }

    /**
     * 외부 인프라 서비스를 호출한다고 가정한다
     */
    fun sendSignUpEmail(member: Member) {
        println(
            """
                ${member.name} + " 님 회원가입을 축하드립니다."
            """.trimIndent()
        )
    }
}