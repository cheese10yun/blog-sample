package com.spring.camp.api

import com.spring.camp.io.PartnerClient
import com.spring.camp.io.PartnerResponse
import com.spring.camp.io.PartnerStatus
import com.spring.camp.io.PartnerStatusResponse
import org.assertj.core.api.BDDAssertions
import org.assertj.core.api.BDDAssertions.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.mockito.BDDMockito.*
import org.mockito.Mockito
import java.time.LocalDate


class ShopLatestUpdateServiceTest(
    private val shopLatestUpdateService: ShopLatestUpdateService,
    private val mockPartnerClient: PartnerClient,
) : TestSupport() {

    @BeforeEach
    fun resetMock() {
        Mockito.reset(mockPartnerClient)
    }

    @Test
    fun `폐업 사업자의 경우 xxx`() {
        //given
        val brn = "000-00-0000"
        given(mockPartnerClient.getPartnerStatus(brn))
            .willReturn(PartnerStatusResponse(PartnerStatus.OUT_OF_BUSINESS, LocalDate.of(2023, 12, 12)))

        //when
        val shop = shopLatestUpdateService.sync(brn)

        //then
        // 검증 진행..
    }

    @Test
    fun `휴업 사업자의 경우 xxx`() {
        //given
        val brn = "000-00-0000"
        given(mockPartnerClient.getPartnerStatus(brn))
            .willReturn(PartnerStatusResponse(PartnerStatus.CLOSING, LocalDate.of(2023, 12, 12)))

        //when
        val shop = shopLatestUpdateService.sync(brn)

        //then
        // 검증 진행..
    }
}