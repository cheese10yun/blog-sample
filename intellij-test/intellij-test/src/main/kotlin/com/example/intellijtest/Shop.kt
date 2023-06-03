package com.example.intellijtest

import jakarta.persistence.Column
import jakarta.persistence.Entity
import org.springframework.data.jpa.repository.JpaRepository

@Entity(name = "shop")
class Shop(
    @Column(name = "business_registration_number", nullable = false)
    val brn: String,
    @Column(name = "name", nullable = false)
    val name: String,
    @Column(name = "band", nullable = false)
    val band: String,
    @Column(name = "category", nullable = false)
    val category: String,
    @Column(name = "email", nullable = false)
    val email: String,
    @Column(name = "website", nullable = false)
    val website: String,
    @Column(name = "opening_hours", nullable = false)
    val openingHours: String,
    @Column(name = "seating_capacity", nullable = false)
    val seatingCapacity: Int,
    @Column(name = "rating", nullable = false)
    val rating: Int,
    @Column(name = "address", nullable = false)
    val address: String,
    @Column(name = "address_detail", nullable = false)
    val addressDetail: String,
    @Column(name = "zip_code", nullable = false)
    val zipCode: String,
) : EntityAuditing()


interface ShopRepository : JpaRepository<Shop, Long>