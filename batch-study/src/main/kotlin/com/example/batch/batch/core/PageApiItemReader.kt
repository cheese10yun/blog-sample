package com.example.batch.batch.core

import com.example.batch.common.PageResponse
import com.example.batch.service.PaymentRestService
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.AfterRead
import org.springframework.batch.core.annotation.BeforeRead
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.item.ItemReader
import java.math.BigDecimal
import kotlin.properties.Delegates.notNull

open class PageApiItemReader<domainClass: Class<*>>(
    private val size: Int = 100,
    private var page: Int = 0,
    private val amount: BigDecimal = BigDecimal(100),
    private val paymentRestService: PaymentRestService
) : ItemReader<T> {

    private lateinit var stepExecution: StepExecution

    private var readContent = mutableListOf<domainClass>()

    private var totalPage by notNull<Int>()


    // 페이지를 하나씩 읽는다.
    // 읽으면 제거 한다.
    override fun read(): domainClass? {
        return when {
            this.readContent.isEmpty() -> null
            else -> {

                println(readContent)
                readContent.removeAt(this.readContent.size - 1)
            }
        }
    }

    @BeforeStep
    @Suppress("UNUSED")
    fun beforeStep(stepExecution: StepExecution) {
        this.stepExecution = stepExecution
        this.totalPage = readRows().totalPages
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

    private fun readRows(page: Int = 0): PageResponse<domainClass> {
        return paymentRestService.requestPayment(
            BigDecimal(10),
            page,
            size
        )
    }
}