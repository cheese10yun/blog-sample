package com.spring.test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ActiveProfiles("test")
@Transactional
abstract class SpringTestSupport {

    @Autowired
    protected lateinit var entityManager: EntityManager

    protected fun save(entity: Any) = entityManager.persist(entity)

    protected fun saveAll(entities: Iterable<Any>): Iterable<Any> {
        for (entity in entities) {
            entityManager.persist(entity)
        }

        return entities
    }
}