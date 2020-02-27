package com.example.querydsl

//import org.testcontainers.containers.DockerComposeContainer
//import org.testcontainers.containers.PostgreSQLContainer
//import org.testcontainers.containers.output.Slf4jLogConsumer
//import org.testcontainers.junit.jupiter.Container
//import org.testcontainers.junit.jupiter.Testcontainers
import com.querydsl.core.types.dsl.EntityPathBase
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager


@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
@ActiveProfiles("test")
//@Testcontainers
abstract class SpringBootTestSupport {

    @Autowired
    protected lateinit var entityManager: EntityManager

    @Autowired
    protected lateinit var query: JPAQueryFactory

    protected fun <T> save(entity: T): T {
        entityManager.persist(entity)

        entityManager.flush()
        entityManager.clear()

        return entity
    }

    protected fun <T> saveAll(entities: Iterable<T>): Iterable<T> {
        for (entity in entities) {
            entityManager.persist(entity)
        }

        entityManager.flush()
        entityManager.clear()

        return entities
    }

    protected fun deleteAll(qEntity: EntityPathBase<*>) {

        query.delete(qEntity).execute()

        entityManager.flush()
        entityManager.clear()
    }


//    companion object {
//        @JvmStatic
//        @Container
//        val postgrContainer = PostgreSQLContainer<Nothing>()
//            .apply {
//                withDatabaseName("querydsl")
//            }
//
//        @JvmStatic
//        @Container
//        val dockerComposeContainer = DockerComposeContainer<Nothing>(File("src/test/resource/docker-compose.yml"))
//
////        @JvmStatic
////        @Container
////        val genericContainer = GenericContainer<Nothing>("dockerImageName")
////            .apply {
////                withEnv("POSTGRES_DB", "DATABASE_NAME")
////                withExposedPorts(5432) // 포트를 선언하지만 실제값은 랜덤이다. 사용할 수있는 포트 중에서 랜덤으로
////            }
//
//        @BeforeAll
//        @JvmStatic
//        fun beforeAll() {
//            val log by logger()
//            val logConsumer = Slf4jLogConsumer(log)
//            postgrContainer.followOutput(logConsumer)
//        }
//
//    }

//    @Test
//    internal fun `container port`() {
//        val realPort = genericContainer.getMappedPort(5432) // 실제 컨테이너가 사용하는 포트
//    }
}