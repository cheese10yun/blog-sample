package com.example.jpanplus1.order

import com.example.jpanplus1.member.Member
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
internal class OrderTest(
        val orderRepository: OrderRepository
){

    @Test
    @DisplayName("awsdsd")
    internal fun name() {
        val member = Member("asd@asd.com", "name")
        val order = Order(member, "N000123")
        println(order)
    }

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
}