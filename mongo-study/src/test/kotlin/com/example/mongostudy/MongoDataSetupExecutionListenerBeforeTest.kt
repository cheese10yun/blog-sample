package com.example.mongostudy

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import java.util.function.Consumer

@MongoTestSupport
@SpringBootTest
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class MongoDataSetupExecutionListenerBeforeTest(
    private val mongoTemplate: MongoTemplate
) {


    @MongoDataSetup(
        jsonPath = "/mongo-document-foo.json",
        clazz = Foo::class,
    )
    @Test
    @DisplayName("MongoDataSetup 단일 테스트")
    fun mongoDataSetup() {
        // when
        val fooDocuments = mongoTemplate.findAll<Foo>()

        // then
        then(fooDocuments).hasSize(2)
        then(fooDocuments).allSatisfy(
            Consumer {
                then(it.id).isNotNull()
                then(it.addressDetail).isIn(
                    "1동, xxxx",
                    "2동, xxxx"
                )
                then(it.createdAt).isNotNull()
                then(it.updatedAt).isNotNull()
            }
        )
    }

    @MongoDataSetup(
        jsonPath = "/mongo-document-foo-projection.json",
        clazz = FooProjection::class,
        collectionName = "foo"
    )
    @Test
    @DisplayName("collectionName 기반으로 테스트")
    fun mongoDataSetupCollectionName() {
        // when
        val fooDocuments = mongoTemplate.findAll<Foo>()

        // then
        then(fooDocuments).hasSize(2)
        then(fooDocuments).allSatisfy(
            Consumer {
                then(it.id).isNotNull()
                then(it.addressDetail).isIn(
                    "1동, xxxx",
                    "2동, xxxx"
                )
                then(it.createdAt).isNotNull()
                then(it.updatedAt).isNotNull()
            }
        )
    }

    @MongoDataSetups(
        MongoDataSetup(
            jsonPath = "/mongo-document-bar.json",
            clazz = Bar::class,
        ),
        MongoDataSetup(
            jsonPath = "/mongo-document-foo.json",
            clazz = Foo::class,
        ),
    )
    @Test
    @DisplayName("MongoDataSetups 여러개 테스트")
    fun mongoDataSetups() {
        // when
        val fooDocuments = mongoTemplate.findAll<Foo>()
        val barDocuments = mongoTemplate.findAll<Bar>()

        // then
        then(fooDocuments).hasSize(2)
        then(fooDocuments).allSatisfy(
            Consumer {
                then(it.id).isNotNull()
                then(it.addressDetail).isIn(
                    "1동, xxxx",
                    "2동, xxxx"
                )
                then(it.createdAt).isNotNull()
                then(it.updatedAt).isNotNull()
            }
        )

        then(barDocuments).hasSize(2)
        then(barDocuments).allSatisfy(
            Consumer {
                then(it.id).isNotNull()
                then(it.email).isIn(
                    "aaa@asd.com",
                    "bbb@asd.com"
                )
                then(it.createdAt).isNotNull()
                then(it.updatedAt).isNotNull()
            }
        )
    }
}