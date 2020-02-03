package com.example.batch.service

import com.example.batch.SpringBootTestSupport
import com.example.batch.common.PageResponse
import com.example.batch.domain.order.domain.Payment
import com.example.batch.domain.order.dto.PaymentDto
import com.fasterxml.jackson.core.type.TypeReference
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class JsonTest : SpringBootTestSupport() {

    @Test
    internal fun `content test`() {
        //given
        val path = "/json/payment.json"

        //when
        val payments = read(path, object : TypeReference<List<PaymentDto>>() {})

        //then
        then(payments).hasSize(10)
    }

    @Test
    internal fun `page test`() {
        //given
        val path = "/json/payment-page.json"

        //when
        val page = readPage(path, object : TypeReference<PageResponse<PaymentDto>>() {})

        //then
        then(page.content).hasSize(10)
        then(page.totalPages).isEqualTo(224)
        then(page.totalElements).isEqualTo(2232)
        then(page.last).isFalse()
        then(page.first).isTrue()
        then(page.size).isEqualTo(10)
        then(page.number).isEqualTo(0)
        then(page.empty).isFalse()
    }
}