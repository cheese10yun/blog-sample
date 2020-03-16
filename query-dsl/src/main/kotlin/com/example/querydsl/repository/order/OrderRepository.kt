package com.example.querydsl.repository.order


import com.example.querydsl.domain.Order
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Long>