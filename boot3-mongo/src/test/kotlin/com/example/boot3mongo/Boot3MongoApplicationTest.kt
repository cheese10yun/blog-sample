package com.example.boot3mongo

import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor


@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
abstract class Boot3MongoApplicationTest {

    @Autowired
    protected lateinit var mongoTemplate: MongoTemplate
}
