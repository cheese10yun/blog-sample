package com.example.querydsl

import com.querydsl.jpa.impl.JPAQueryFactory
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.EntityTransaction

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ActiveProfiles("test")
@Testcontainers
//@TestInstance(Lifecycle.PER_CLASS)
abstract class SpringBootTestSupport {

    @Autowired
    protected lateinit var entityManagerFactory: EntityManagerFactory

    @Autowired
    protected lateinit var query: JPAQueryFactory


    companion object {
        @Container
        @JvmStatic
        val mysqlTestContainer = MySQLContainer<Nothing>()
            .apply {
                withDatabaseName("sample")
            }

        @BeforeAll
        @JvmStatic
        private fun beforeAll() {
            val log by logger()
            val logConsumer = Slf4jLogConsumer(log)
            mysqlTestContainer.followOutput(logConsumer)
        }

    }


    protected val entityManager: EntityManager by lazy {
        entityManagerFactory.createEntityManager()
    }

    protected val transaction: EntityTransaction by lazy {
        entityManager.transaction
    }

    protected fun <T> save(entity: T): T {
        transaction.begin()

        try {
            entityManager.persist(entity)
            entityManager.flush() // transaction commit시 자동으로 flush 발생시키나 명시적으로 선언
            transaction.commit()
            entityManager.clear()
        } catch (e: Exception) {
            transaction.rollback()
        }
        return entity
    }


    protected fun <T> saveAll(entities: Iterable<T>): Iterable<T> {
        transaction.begin()

        for (entity in entities) {
            try {
                entityManager.persist(entity)
                entityManager.flush() // transaction commit시 자동으로 flush 발생시키나 명시적으로 선언
                transaction.commit()
                entityManager.clear()

            } catch (e: Exception) {
                transaction.rollback()
            }
        }
        return entities
    }
}