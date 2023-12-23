package com.example.querydsl.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "heelo")
data class Hello(
    @Column(name = "name", nullable = false)
    val name: String

) : EntityAuditing() {
}