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

//    @Test
//    internal fun `주문 검증 `() {
//        //.. test code
//
//        // 쿠폰 사용여부 검증을 위한 조회
//        couponRepository.findXXXX()
//        // 주문 항목 검증을 위한 조회
//        orderItemRepository.findXXXX()
//        // 배송 정보, 배송 상태 검증을 위한 조회
//        shippingRepository.findXXXX()
//        // 수취인 검증을 위한 조회
//        receiverRepository.findXXXX()
//        // 주문자 검증을 위한 조회
//        ordererRepository.findXXXX()
//    }

//    @Test
//    internal fun `주문 검증 `() {
//        //.. test code
//
//        save(coupon)
//        save(orderItem)
//        save(coupon)
//        save(receiver)
//
//        // 쿠폰 사용여부 검증을 위한 조회
//        val coupons = query.selectFrom(QCoupon.coupon)
//            .where(QCoupon.coupon.amount.eq(123.toBigDecimal()))
//            .fetch()
//
//        // 주문 항목 검증을 위한 조회
//        val orderItems = query.selectFrom(QOrderItem.OrderItem)
//            .where(QOrderItem.OrderItem.amount.gt(BigDecimal.TEN))
//            .fetch()
//
//        // 배송 정보, 배송 상태 검증을 위한 조회
//        val shipping = query.select(QShipping.shipping.address)
//            .from(QShipping.shipping)
//            .where(QShipping.shipping.address.eq("xxxx"))
//            .fetch()
//
//        // 수취인 검증을 위한 조회
//        val receiver = query.select(QReceiver.receiver.nane)
//            .from(QReceiver.receiver)
//            .where(QReceiver.receiver.name.eq("xxxx"))
//            .fetch()
//    }
//
//    @Test
//    internal fun `주문 검증 `() {
//        //.. 외부 디펜던시 없이 영속화 간단하게 가능
//        save(coupon)
//        save(orderItem)
//        save(coupon)
//        save(receiver)
//    }
}