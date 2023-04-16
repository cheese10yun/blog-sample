package com.spring.camp.api

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql

class OrderTest(
    private val orderRepository: OrderRepository,
) : TestSupport() {

    @Test
    fun `save test`() {
        //given
        val orderNumber = "order-number-123"
        val order = Order(orderNumber)

        //when
        val persistOrder = orderRepository.save(order)

        //then
        then(persistOrder.orderNumber).isEqualTo(orderNumber)
        then(persistOrder.status).isEqualTo(OrderStatus.READY)
    }

//    @Test
//    fun `상품 준비중 to 배송 시작 status 변경 테스트`() {
//        //given
//        val orderNumber = "order-number-123"
//        val order = Order(orderNumber) // 주문 상태 생성
//        order.updateStatusCompletePayment() // 결게 완료 상태 변경
//        order.updateStatusProductPreparation() // 상품 준비 상태 변경
//
//        // 데이터 셋업 완료
//        order.updateStatusDeliveryStarted()
//    }

@Test
@Sql("/order-setup.sql")
fun `상품 준비중 to 배송 시작 status 변경 테스트`() {
    //given
    val order = orderRepository.findAll().first()!!

    //when
    order.updateStatusDeliveryStarted()

    //then
    then(order.status).isEqualTo(OrderStatus.DELIVERY_STARTED)
}
}