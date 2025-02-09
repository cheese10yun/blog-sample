package com.spring.camp.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime


@Entity
@Table(name = "product")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long?,

    @Column(name = "product_id")
    var productId: Long,

    @Column(name = "product_name")
    var productName: String,

    @Column(name = "category")
    var category: String,

    @Column(name = "brand")
    var brand: String,

    @Column(name = "price")
    var price: BigDecimal,

    @Column(name = "discount_rate")
    var discountRate: BigDecimal?,

    @Column(name = "currency")
    var currency: String,

    @Column(name = "is_active")
    var isActive: Boolean,

    @Column(name = "created_date")
    var createdDate: LocalDateTime,

    @Column(name = "updated_date")
    var updatedDate: LocalDateTime,

    @Column(name = "region")
    var region: String,

    @Column(name = "description")
    var description: String?,

    @Column(name = "cost")
    var cost: BigDecimal?,

    @Column(name = "supplier")
    var supplier: String?,

    @Column(name = "tax_rate")
    var taxRate: BigDecimal?,

    @Column(name = "unit")
    var unit: String,

    @Column(name = "additional_fee")
    var additionalFee: BigDecimal?
) {

}