package com.example.springmocktest.api

import com.example.springmocktest.SpringApiTestSupport
import com.example.springmocktest.infra.AccountHolderVerificationResponse
import com.example.springmocktest.infra.ShinChanBankApiImpl
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.anyString
import org.mockito.BDDMockito.given
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post

internal class PartnerApiTest : SpringApiTestSupport() {

//    @MockBean
//    private lateinit var shinChanBankApiImpl: ShinChanBankApiImpl

    @Test
    internal fun `파트너 등록`() {

//        given(shinChanBankApiImpl.checkAccountHolder(anyString(), anyString()))
//            .willReturn(AccountHolderVerificationResponse(true))

        mockMvc.post("/partners") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "name" : "123",
                  "accountHolder" : "123",
                  "accountNumber" :  "123"
                }
            """.trimIndent()
        }.andExpect {
            status { isOk }
        }
    }
}