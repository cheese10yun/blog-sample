package com.example.querydsl.repository.order

import com.example.querydsl.SpringBootTestSupport
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import jakarta.persistence.EntityManager


@Transactional
internal class OrderCustomRepositoryImplTest(
    private val em: EntityManager,
    private val orderRepository: OrderRepository
) : SpringBootTestSupport() {

    @Test
    fun `count 1,000ms, content 500ms Thread sleep test`() = runBlocking {
        val time = measureTimeMillis {
            orderRepository.findPaging3By(
                pageable = PageRequest.of(0, 10),
                address = "address"
            )
        }
        println("${time}ms") // 1037ms
    }

    @Test
    fun `findSliceBy2 - 총 22건을 size 22로 조회하면 hasNext가 false이다`() {
        val address = "slice-boundary-exact"
        saveAll((1..22).map { Order(orderNumber = "order-$it", userId = 1L, address = address, couponId = null) })

        val result = orderRepository.findSliceBy2(PageRequest.of(0, 22), address)

        then(result.content).hasSize(22)
        then(result.hasNext()).isFalse()
    }

    @Test
    fun `findSliceBy2 - 총 22건을 size 21로 조회하면 hasNext가 true이다`() {
        val address = "slice-boundary-over"
        saveAll((1..22).map { Order(orderNumber = "order-$it", userId = 1L, address = address, couponId = null) })

        val result = orderRepository.findSliceBy2(PageRequest.of(0, 21), address)

        then(result.content).hasSize(21)
        then(result.hasNext()).isTrue()
    }
}