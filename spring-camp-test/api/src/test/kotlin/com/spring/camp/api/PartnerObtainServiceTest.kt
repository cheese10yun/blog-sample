package com.spring.camp.api

//import com.spring.camp.io.ClientTestConfiguration
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.mock.mockito.MockBean


class PartnerObtainServiceTest(
    private val partnerObtainService: PartnerObtainService,
): TestSupport() {

    @MockBean
    private lateinit var obtainService: ObtainService

    @Test
    fun `test case 1`() {
        //given
        val brn = "xxxx"
        val name = "주식회사 XXX"
        given(obtainService.syncPartner()).willReturn(true)

        //when
        partnerObtainService.syncPartner()

        //then
//        then(shop.name).isEqualTo("주식회사 XXX")
    }

//    @Test
//    fun `상품 준비중 to 배송 시작 status 변경 테스트`() {
//        //given
//        val orderNumber = "order-number-123"
//        val order = Order(orderNumber)
//
//        order.status = OrderStatus.COMPLETE_PAYMENT
//
//        //when
//
//        //then
//
//    }
}