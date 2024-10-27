package com.example.mongostudy

import com.example.mongostudy.mongo.Auditable
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime
import java.util.function.Consumer

class MongoDataSetupExecutionListenerTest(
//    private val mongoTemplate: MongoTemplate
) : MongoStudyApplicationTests() {

    @AfterAll
    fun `테스트가 끝난 시점에 모든 데이터는 초기화 되야 한다`() {
        val fooDocuments = mongoTemplate.findAll<Foo>()
        val barDocuments = mongoTemplate.findAll<Bar>()

        then(fooDocuments).hasSize(0)
        then(barDocuments).hasSize(0)
    }

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

@Document(collection = "foo")
data class Foo(
    @Field("address_detail")
    var addressDetail: String
) : Auditable()

data class FooProjection(
    @Field("address_detail")
    var addressDetail: String,
    @Field("created_at")
    val createdAt: LocalDateTime,
    @Field("updated_at")
    val updatedAt: LocalDateTime
)

@Document(collection = "bar")
data class Bar(
    @Field("email")
    var email: String
) : Auditable()