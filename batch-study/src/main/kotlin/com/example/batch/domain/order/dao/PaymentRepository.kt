package com.example.batch.domain.order.dao

import com.example.batch.domain.order.domain.Payment
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository : JpaRepository<Payment, Long> {
}