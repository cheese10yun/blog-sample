package com.example.springcamptest

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.Column
import javax.persistence.Entity

@Entity(name = "shop")
data class Shop(
    @Column(name = "business_registration_number", nullable = false)
    val brn: String,
    @Column(name = "name", nullable = false)
    val name: String,
) : EntityAuditing()

interface ShopRepository : JpaRepository<Shop, Long>