package com.example.batch.domain.order.dao

import com.example.batch.domain.order.domain.Order
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.math.BigDecimal

interface OrderRepository : JpaRepository<Order, Long> {

    @Query("select o from Order o where o.amount > 5000.00")
    fun findasdasd(): List<Order>

    fun findByAmountGreaterThan(amount: BigDecimal, pageable: Pageable) : Page<Order>


}