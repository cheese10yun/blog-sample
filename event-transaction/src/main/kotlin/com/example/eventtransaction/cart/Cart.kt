package com.example.eventtransaction.cart

import com.example.eventtransaction.EntityAuditing
import com.example.eventtransaction.order.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "cart")
class Cart(
    @Column(name = "product_id", nullable = false)
    var productId: Long,

    @Column(name = "member_id", nullable = false, updatable = false)
    var memberId: Long
) : EntityAuditing()

interface CartRepository : JpaRepository<Cart, Long> {
    fun deleteByProductId(productId: Long)
}

@Service
class CartService(
    private val cartRepository: CartRepository
) {
    @Transactional
    fun deleteCartWithOrder(order: Order) {
        cartRepository.deleteByProductId(order.productId)
    }
}