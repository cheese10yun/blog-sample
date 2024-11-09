package com.example.redis

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository

@Entity(name = "address")
@Table(name = "address")
class Address(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long? = null,

    @Column(name = "street", length = 100, nullable = false)
    val street: String,

    @Column(name = "city", length = 50, nullable = false)
    val city: String,

    @Column(name = "state", length = 50, nullable = false)
    val state: String,

    @Column(name = "zipcode", length = 10, nullable = false)
    val zipcode: String
)

interface AddressRepository : JpaRepository<Address, Long>