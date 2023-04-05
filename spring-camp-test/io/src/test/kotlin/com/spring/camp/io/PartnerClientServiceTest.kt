package com.spring.camp.io

import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenThrownBy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

@Import(ClientTestConfiguration::class)
class PartnerClientServiceTest(
    private val partnerClientService: PartnerClientService,
    private val partnerClient: PartnerClient
) : TestSupport() {

    @Test
    fun `getPartnerBy 200 응답 케이스`() {
        //given
        val brn = "000-00-0000"
        val name = "주식회사 XXX"
        val response = PartnerResponse(brn, name)

        given(partnerClient.getPartnerByResponse(brn))
            .willReturn(ResponseEntity(response, HttpStatus.OK))

        //when
        val result = partnerClientService.getPartnerBy(brn)

        //then
        then(result.brn).isEqualTo(brn)
        then(result.name).isEqualTo(name)
    }

    @Test
    fun `getPartnerBy 400 케이스`() {
        //given
        val brn = "000-00-0000"
        val name = "주식회사 XXX"
        val response = PartnerResponse(brn, name)

        given(partnerClient.getPartnerByResponse(brn))
            .willReturn(ResponseEntity(response, HttpStatus.BAD_REQUEST))

        //when
        thenThrownBy {
            partnerClientService.getPartnerBy(brn)
        }
            .isInstanceOf(IllegalArgumentException::class.java)
    }


    @Test
    fun `getPartner ResponseEntity 응답`() {
        //given
        val brn = "000-00-0000"
        val name = "주식회사 XXX"
        val partnerResponse = PartnerResponse(brn, name)

        given(partnerClient.getPartnerByResponse(brn))
            .willReturn(ResponseEntity(partnerResponse, HttpStatus.BAD_REQUEST))

        //when
        val response = partnerClientService.getPartner(brn)
        then(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        then(response.body!!.brn).isEqualTo(brn)
        then(response.body!!.name).isEqualTo(name)
    }
}