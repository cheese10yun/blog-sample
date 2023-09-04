package com.example.querydsl.repository.order

import com.example.querydsl.SpringBootTestSupport
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager


@Transactional
internal class OrderCustomRepositoryImplTest(
    private val em: EntityManager,
    private val orderRepository: OrderRepository
) : SpringBootTestSupport() {

    @Test
    fun `count 1,000ms, content 500ms delay test`() = runBlocking {
        val time = measureTimeMillis {
            orderRepository.findPaging3By(
                pageable = PageRequest.of(0, 10),
                address = "address"
            )
        }
        println("${time}ms") // 1037ms
    }
}