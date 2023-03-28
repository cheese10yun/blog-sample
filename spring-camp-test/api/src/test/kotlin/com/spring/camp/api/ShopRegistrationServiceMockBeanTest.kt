package com.spring.camp.api

//import org.assertj.core.api.BDDAssertions.then
//import org.junit.jupiter.api.Test
//import org.mockito.BDDMockito.given
//import org.springframework.boot.test.mock.mockito.MockBean
//
//class ShopRegistrationServiceMockBeanTest(
//    private val shopRegistrationService: ShopRegistrationService,
//): TestSupport() {
//
//    @MockBean
//    private lateinit var partnerClient: PartnerClient
//
//    @Test
//    fun `register mock bean test`() {
//        //given
//        val brn = "000-00-0000"
//        val name = "주식회사 XXX"
//        given(partnerClient.getPartnerBy(brn))
//            .willReturn(PartnerResponse(brn, name))
//
//        //when
//        val shop = shopRegistrationService.register(brn)
//
//        //then
//        then(shop.name).isEqualTo(name)
//        then(shop.brn).isEqualTo(brn)
//    }
//}

import com.spring.camp.io.PartnerClient
import com.spring.camp.io.PartnerResponse
import org.assertj.core.api.BDDAssertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito

class ShopRegistrationServiceMockBeanTest(
    private val shopRegistrationService: ShopRegistrationService,
    private val mockPartnerClient: PartnerClient
): TestSupport() {

    @BeforeEach
    fun resetMock(){
        Mockito.reset(mockPartnerClient)
    }

    @Test
    fun `register mock bean test`() {
        //given
        val brn = "000-00-0000"
        val name = "주식회사 XXX"
        given(mockPartnerClient.getPartnerBy(brn))
            .willReturn(PartnerResponse(brn, name))

        //when
        val shop = shopRegistrationService.register(brn)

        //then
        then(shop.name).isEqualTo(name)
        then(shop.brn).isEqualTo(brn)
    }

}



