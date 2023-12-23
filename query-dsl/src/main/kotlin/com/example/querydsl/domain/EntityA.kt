package com.example.querydsl.domain

import org.springframework.data.jpa.repository.JpaRepository
import jakarta.persistence.*

@Entity
@Table(name = "entity_a")
class EntityA(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "name")
    var name: String
) {
    override fun toString(): String {
        return "EntityA(id=$id, name='$name')"
    }
}


interface EntityARepository : JpaRepository<EntityA, Long>