package com.example.batch.service

import com.example.batch.common.PageResponse
import java.math.BigDecimal

interface RestService {

    fun <T> requestPage(amount: BigDecimal, page: Int, size: Int): PageResponse<T>

}