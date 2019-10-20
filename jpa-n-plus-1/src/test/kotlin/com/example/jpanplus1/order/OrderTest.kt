package com.example.jpanplus1.order

import com.example.jpanplus1.member.Member
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Order 생성테스트")
internal class OrderTest{

    @Test
    @DisplayName("awsdsd")
    internal fun name() {
        val member = Member("asd@asd.com", "name")
        val order = Order(member, "N000123")
        println(order)
    }
}