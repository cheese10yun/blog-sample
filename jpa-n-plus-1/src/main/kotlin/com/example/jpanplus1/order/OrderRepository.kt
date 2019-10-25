package com.example.jpanplus1.order

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface OrderRepository : JpaRepository<Order, Long> {

    @Query("select o from Order o join fetch o.member")
    fun findAllOrder(): List<Order>

    @Query("select o from Order o where o.id = :id")
    fun findByIdCustom(@Param("id") id: Long): Order

    fun findByNumber(number: String): Order
}