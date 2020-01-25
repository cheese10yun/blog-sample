package com.example.batch.batch.core

import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.AfterRead
import org.springframework.batch.core.annotation.BeforeRead
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemStreamSupport
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import kotlin.properties.Delegates.notNull

private const val DEFAULT_PAGE_SIZE = 100

open class PageableItemReader<T>(
        private val name: String,
        private val sort: Sort,
        private val pageSize: Int = DEFAULT_PAGE_SIZE,
        private val query: (Pageable) -> Page<T>
) : ItemStreamSupport(), ItemReader<T> {

    private var totalPage by notNull<Int>()
    private var page = 0
    private var readContent = mutableListOf<T>()
    private lateinit var stepExecution: StepExecution


    init {
        super.setName(this.name)
    }

    override fun read(): T? {
        return when {
            this.readContent.isEmpty() -> null
            else -> this.readContent.removeAt(this.readContent.size - 1)
        }
    }

    @BeforeStep
    @Suppress("UNUSED")
    fun beforeStep(stepExecution: StepExecution) {
        this.stepExecution = stepExecution
        this.totalPage = readRows().totalPages
        this.page = totalPage - 1
    }

    @BeforeRead
    @Suppress("UNUSED")
    fun beforeRead() {
        when {
            this.page < 0 -> return
            this.readContent.isEmpty() -> readContent = readRows(page).content.toMutableList()
        }
    }

    @AfterRead
    @Suppress("UNUSED")
    fun afterRead() {
        if (readContent.isEmpty()) --page
    }

    private fun readRows(page: Int = 0): Page<T> {
        return when {
            this.page < 0 -> Page.empty()
            else -> try {
                this.query.invoke(PageRequest.of(page, pageSize, sort))
            } catch (e: Exception) {
                this.stepExecution.status = BatchStatus.FAILED
                this.stepExecution.exitStatus = ExitStatus.FAILED
                throw e
            }
        }
    }
}