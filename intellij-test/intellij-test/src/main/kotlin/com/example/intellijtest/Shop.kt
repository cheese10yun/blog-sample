package com.example.intellijtest

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository


@Entity(name = "shop")
data class Shop(
    @Column(name = "business_registration_number", nullable = false)
    val brn: String,
    @Column(name = "name", nullable = false)
    val name: String,
    @Column(name = "address", nullable = false)
    val address: String,
    @Column(name = "address_detail", nullable = false)
    val addressDetail: String,
    @Column(name = "zip_code", nullable = false)
    val zipCode: String,
    @Column(name = "rank", nullable = false)
    val rank: String
) : EntityAuditing()


interface ShopRepository : JpaRepository<Shop, Long>

@EntityListeners(value = [AuditingEntityListener::class])
@MappedSuperclass
abstract class EntityAuditing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        internal set
}