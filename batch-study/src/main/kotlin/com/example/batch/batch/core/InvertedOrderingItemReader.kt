package com.example.batch.batch.core

import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

open class InvertedOrderingItemReader<T>(
    name: String,
    sort: Sort?,
    val pageSize: Int = 100,
    private val query: (Pageable) -> Page<T>
) : AbstractItemCountingItemStreamItemReader<T>() {
    private var initialized = AtomicBoolean(false)

    private lateinit var results: CopyOnWriteArrayList<T>

    private var page = AtomicInteger(0)

    init {
        super.setName(name)
    }

    /**
     * 정렬 기준 프로퍼티
     *
     * 읽으려는 데이터를 역전시키기 위해 주어진 정렬 조건을 반전시킨다.
     */
    private val sort: Sort = sort?.let {
        Sort.by(
            it.toList().map { order ->
                Sort.Order(
                    if (order.direction == Sort.Direction.ASC) Sort.Direction.DESC else Sort.Direction.ASC,
                    order.property
                )
            }
        )
    } ?: Sort.unsorted()

    override fun doOpen() {
        assert(!initialized.get()) { "Cannot open an already opened ItemReader, call close first" }

        initialized.set(true)

        page.set(query.invoke(PageRequest.of(0, pageSize, sort)).totalPages - 1)
    }

    override fun doRead(): T? {
        if (!::results.isInitialized || results.isEmpty()) { // 청크가 비어있으면 다음 청크(실제로는 이전 페이지)를 읽는다.
            doReadPage()
        }

        return when {
            results.isEmpty() -> null
            else -> results.removeAt(results.size - 1) // 청크의 뒷부분부터 읽는다.
        }
    }

    override fun doClose() {
        initialized.set(false)
        page.set(0)
    }

    private fun doReadPage() {
        initResult()

        if (page.get() > -1) { // 읽어들일 페이지가 남아 있으면 청크 탐색을 계속한다.
            results.addAll(query.invoke(PageRequest.of(page.get(), pageSize, sort)).content)
        }

        page.decrementAndGet()
    }

    private fun initResult() {
        when {
            !::results.isInitialized || results.isEmpty() -> results = CopyOnWriteArrayList()
            else -> this.results.clear()
        }
    }
}