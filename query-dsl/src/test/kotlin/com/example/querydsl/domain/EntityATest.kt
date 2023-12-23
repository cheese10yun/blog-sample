package com.example.querydsl.domain

import com.example.querydsl.SpringBootTestSupport
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Transactional
import jakarta.persistence.EntityManager

@Transactional
internal class EntityATest(
    private val entityARepository: EntityARepository,
    private val entityManager: EntityManager
) : SpringBootTestSupport() {

    @Test
    internal fun name() {
        val entity = entityARepository.save(EntityA(123, "222"))

        entity.name = "123123"

        entityManager.flush()
        entityManager.clear()

        val findAll = entityARepository.findAll()
        println(findAll)
    }
}