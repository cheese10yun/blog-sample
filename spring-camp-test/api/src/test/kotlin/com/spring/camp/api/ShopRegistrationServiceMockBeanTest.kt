package com.spring.camp.api

import com.spring.camp.io.PartnerClient
//import com.spring.camp.io.PartnerClientService
import com.spring.camp.io.PartnerResponse
import com.spring.camp.io.PartnerStatus
import com.spring.camp.io.PartnerStatusResponse
import org.assertj.core.api.BDDAssertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.time.LocalDate

class ShopRegistrationServiceMockBeanTest(
    private val shopRegistrationService: ShopRegistrationService,
    private val mockPartnerClient: PartnerClient,
//    private val mockPartnerClientService: PartnerClientService,
) : TestSupport() {

//    @MockBean
//    private lateinit var partnerClient: PartnerClient

    @BeforeEach
    fun resetMock() {
        Mockito.reset(mockPartnerClient)
//        Mockito.reset(mockPartnerClientService)
    }

    @Test
    fun sdasdasdasd() {


        val brn = setOf("234234")
        val willReturn = given(mockPartnerClient.getPartners(brn)
        ).willReturn(
            listOf(
                PartnerStatusResponse(
                    status = PartnerStatus.CLOSING,
                    closeBusinessDate = null
                )
            )
        )

        val asd = shopRegistrationService.asd(brn)

        println("")
    }

    //    @Test
//    fun `register mock bean test`() {
//        //given
//        val brn = "000-00-0000"
//        val name = "주식회사 XXX"
//        given(mockPartnerClient.getPartnerBy(brn))
//            .willReturn(PartnerResponse(brn, name))
//
//        //when
//        val shop = shopRegistrationService.register(brn)
//
//        //then
//        then(shop.name).isEqualTo(name)
//        then(shop.brn).isEqualTo(brn)
//    }

//    @Test
//    fun `register mock bean test`() {
//        //given
//        val brn = "000-00-0000"
//        val name = "주식회사 XXX"
//        given(mockPartnerClientService.getPartner(brn))
//            .willReturn(
//                ResponseEntity(
//                    PartnerResponse(brn, name),
//                    HttpStatus.BAD_REQUEST
//                )
//            )
//
//        //when
//        val shop = shopRegistrationService.register(brn)
//
//        //then
//        then(shop.name).isEqualTo(name)
//        then(shop.brn).isEqualTo(brn)
//    }

    @Test
    fun `Shop 등록 테스트 케이스`() {
        //given
        val brn = "000-00-0000"
        val name = "주식회사 XXX"

        //when
        val shop = shopRegistrationService.register(brn)

        //then
        then(shop.name).isEqualTo(name)
        then(shop.brn).isEqualTo(brn)
    }
}