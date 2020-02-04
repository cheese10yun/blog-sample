package com.example.querydsl

import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File


@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
@ActiveProfiles("test")
@Testcontainers
abstract class SpringBootTestSupport {


    companion object {


        @JvmStatic
        @Container
        val postgrContainer = PostgreSQLContainer<Nothing>()
            .apply {
                withDatabaseName("querydsl")
            }

        @JvmStatic
        @Container
        val dockerComposeContainer = DockerComposeContainer<Nothing>(File("src/test/resource/docker-compose.yml"))

//        @JvmStatic
//        @Container
//        val genericContainer = GenericContainer<Nothing>("dockerImageName")
//            .apply {
//                withEnv("POSTGRES_DB", "DATABASE_NAME")
//                withExposedPorts(5432) // 포트를 선언하지만 실제값은 랜덤이다. 사용할 수있는 포트 중에서 랜덤으로
//            }

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            val log by logger()
            val logConsumer = Slf4jLogConsumer(log)
            postgrContainer.followOutput(logConsumer)
        }

    }

//    @Test
//    internal fun `container port`() {
//        val realPort = genericContainer.getMappedPort(5432) // 실제 컨테이너가 사용하는 포트
//    }
}