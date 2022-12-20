package com.example.stock.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Stock constructor(
    productId: Long,
    quantity: Long
) {
    fun decrease(quantity: Long) {

        if (this.quantity - quantity < 0) {
            throw IllegalArgumentException("")
        }

        this.quantity = this.quantity - quantity
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(name = "product_id", nullable = false)
    var productId: Long
        internal set

    @Column(name = "quantity", nullable = false)
    var quantity: Long
        internal set

    init {
        this.productId = productId
        this.quantity = quantity
    }
}

interface StockRepository : JpaRepository<Stock, Long>

@Service
class StockService(
    private val stockRepository: StockRepository
) {

    @Transactional
    fun decrease(stockId: Long, quantity: Long) {
        val service = stockRepository.getReferenceById(stockId)
        service.decrease(quantity)
    }
}