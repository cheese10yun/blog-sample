package com.spring.camp.api

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.mock.mockito.MockBean

class ShopRegistrationServiceMockBeanTest(
    private val shopRegistrationService: ShopRegistrationService,
): TestSupport() {

    @MockBean
    private lateinit var partnerClient: PartnerClient

    @Test
    fun `register mock bean test`() {
        //given
        val brn = "000-00-0000"
        val name = "주식회사 XXX"
        given(partnerClient.getPartnerBy(brn))
            .willReturn(PartnerResponse(brn, name))

        //when
        val shop = shopRegistrationService.register(brn)

        //then
        then(shop.name).isEqualTo(name)
        then(shop.brn).isEqualTo(brn)
    }
}



