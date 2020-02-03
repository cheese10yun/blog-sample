package com.example.batch.batch.core

import com.example.batch.common.PageResponse
import com.example.batch.domain.order.domain.Payment
import com.example.batch.domain.order.dto.PaymentDto
import com.example.batch.service.PaymentRestService
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.AfterRead
import org.springframework.batch.core.annotation.BeforeRead
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.item.ItemReader
import java.math.BigDecimal

open class PageApiItemReader(
    private val size: Int = 100,
    private var page: Int = 0,
    private val amount: BigDecimal = BigDecimal(100),
    private val paymentRestService: PaymentRestService
) : ItemReader<PaymentDto> {

    private lateinit var stepExecution: StepExecution
    private var readContent = mutableListOf<PaymentDto>()

    override fun read(): PaymentDto? {
        return when {
            this.readContent.isEmpty() -> null
            else -> readContent.removeAt(this.readContent.size - 1)
        }
    }

    @BeforeStep
    @Suppress("UNUSED")
    fun beforeStep(stepExecution: StepExecution) {
        this.stepExecution = stepExecution
    }

    @BeforeRead
    @Suppress("UNUSED") // 왜 필요하지 ??
    fun beforeReade() {
        when {
            this.page < 0 -> return
            this.readContent.isEmpty() -> readContent = readRows(page).content
        }
    }

    @AfterRead
    @Suppress("UNUSED")
    fun afterRead() {
        if (readContent.isEmpty()) {
            ++page
        }
    }

    private fun readRows(page: Int = 0): PageResponse<PaymentDto> {
        return paymentRestService.requestPage<Any>(
            BigDecimal(10),
            page,
            size
        )
    }
}