package com.example.batch.domain.order.dao

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.math.BigDecimal


@RunWith(SpringRunner::class)
@SpringBootTest
internal class OrderRepositoryTest {

    @Autowired
    private lateinit var orderRepository: OrderRepository


    @Test
    fun test() {

    }
}