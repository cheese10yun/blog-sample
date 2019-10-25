package com.example.jpanplus1.member

import com.example.jpanplus1.order.Order
import com.example.jpanplus1.order.OrderRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
internal class MemberJpaTest(
        val memberRepository: MemberRepository,
        val orderRepository: OrderRepository
) {

    @Test
    @DisplayName("findById 테스트")
    internal fun findById() {
        val order = orderRepository.findById(1L).get()
        println(order)
    }

    @Test
    @DisplayName("findAll 테스트")
    internal fun findAll() {
        val orders = orderRepository.findAll()
        println(orders)
    }

    @Test
    @DisplayName("findAll 테스트")
    internal fun findAll2() {
        val orders = orderRepository.findAll()
        for(order in orders){
            println(orders)
        }
    }

//
//    @Test
//    @Sql("/member-data.sql")
//    @DisplayName("SQL TEST")
//    internal fun sqlTest() {
//        val members = memberRepository.findAll()
//        assertThat(members).hasSizeGreaterThan(7)
//    }
}