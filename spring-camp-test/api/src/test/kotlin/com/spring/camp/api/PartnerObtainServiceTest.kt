//package com.spring.camp.api
//
//import org.junit.jupiter.api.Test
//import org.mockito.BDDMockito.given
//import org.springframework.boot.test.mock.mockito.MockBean
//
//
//class PartnerObtainServiceTest(
//    private val partnerObtainService: PartnerObtainService,
//): TestSupport() {
//
//    @MockBean
//    private lateinit var obtainService: ObtainService
//
//    @Test
//    fun `test case 1`() {
//        //given
//        val brn = "xxxx"
//        val name = "주식회사 XXX"
//        given(obtainService.syncPartner()).willReturn(true)
//
//        //when
//        partnerObtainService.syncPartner()
//
//        //then
////        then(shop.name).isEqualTo("주식회사 XXX")
//    }
//}