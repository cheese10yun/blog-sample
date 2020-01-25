package com.example.querydsl.domain

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "heelo")
data class Hello(
        @Column(name = "name", nullable = false)
        val name: String

) : EntityAuditing() {
}