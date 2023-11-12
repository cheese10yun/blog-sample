package com.example.mongostudy

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlin.reflect.KClass
import org.springframework.core.io.ClassPathResource
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.TestContext
import org.springframework.test.context.TestExecutionListener
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener


/**
 * Mongo Data Setup에 필요한 어노테이션을 Combined 하여 제공하는 어노테이션
 * ~~~kotlin
 * @MongoTestSupport
 * class xxxTest() {
 *    ...
 * }
 * ~~~
 * 테스트 클래스 상단에 `@MongoTestSupport` 추가하여 사용
 *
 * @see com.example.mongostudy.MongoDataSetupExecutionListenerTest
 */
@TestExecutionListeners(
    listeners = [
        MongoDataSetupExecutionListener::class,
        DependencyInjectionTestExecutionListener::class
    ]
)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class MongoTestSupport

/**
 * MongoDB Test DataSetup을 Support
 * [MongoDataSetup.jsonPath]의 JSON 파일을 MongoDB Document에 저장을 진행한다. 자세한 사용법은 아래 참조
 *
 * @see com.example.mongostudy.MongoDataSetupExecutionListenerTest
 */
class MongoDataSetupExecutionListener : TestExecutionListener {

    /**
     * 테스트 이전에 [MongoDataSetup]기반으로 Document를 생성한다.
     */
    override fun beforeTestMethod(testContext: TestContext) {
        val currentTestMethod = testContext.testMethod
        val mongoDataSetup = currentTestMethod.getAnnotation(MongoDataSetup::class.java)
        val mongoDataSetups = currentTestMethod.getAnnotation(MongoDataSetups::class.java)
        when {
            mongoDataSetup != null -> insertDocuments(mongoDataSetup, testContext)
            mongoDataSetups != null -> mongoDataSetups.mongoDataSetup.forEach { document ->
                insertDocuments(document, testContext)
            }
        }
    }

    /**
     * 테스트가 끝난 이후 모든 모든 데이터를 삭제한다.
     */
    override fun afterTestMethod(testContext: TestContext) {
        val mongoTemplate = mongoTemplate(testContext)
        val currentTestMethod = testContext.testMethod
        val mongoDataSetup = currentTestMethod.getAnnotation(MongoDataSetup::class.java)
        val mongoDataSetups = currentTestMethod.getAnnotation(MongoDataSetups::class.java)

        when {
            mongoDataSetup != null -> {
                when {
                    mongoDataSetup.collectionName.isEmpty() -> mongoTemplate.dropCollection(mongoDataSetup.clazz.java)
                    else -> mongoTemplate.dropCollection(mongoDataSetup.collectionName)
                }
            }
            mongoDataSetups != null -> {
                mongoDataSetups.mongoDataSetup.forEach {
                    when {
                        it.collectionName.isEmpty() -> mongoTemplate.dropCollection(it.clazz.java)
                        else -> mongoTemplate.dropCollection(it.collectionName)
                    }
                }
            }
        }
    }

    /**
     *
     */
    private fun insertDocuments(mongoDataSetup: MongoDataSetup, testContext: TestContext) {
        val mongoTemplate = mongoTemplate(testContext)
        val documents = objectMapper.readValue<List<Any>>(
            readFile(mongoDataSetup.jsonPath),
            objectMapper.typeFactory.constructCollectionType(List::class.java, mongoDataSetup.clazz.java)
        )
        if (documents.isNotEmpty()) {
            when {
                mongoDataSetup.collectionName.isEmpty() -> mongoTemplate.insertAll(documents)
                else -> {
                    // 객체와, collectionName 일치하지 않는 경우는 collectionName을 지정하여 저장한다.
                    for (document in documents) {
                        mongoTemplate.insert(document, mongoDataSetup.collectionName)
                    }
                }
            }
        }
    }

    /**
     * 스프링의 직접적인 의존성을 피하기 위해 applicationContext에서 직접 bean을 가져옴
     * @throws [BeansException] Bean이 없는 경우 예외 발생
     */
    private fun mongoTemplate(testContext: TestContext): MongoTemplate {
        return testContext.applicationContext.getBean(MongoTemplate::class.java)
    }

    /**
     * [path]의 경로에 있는 파일을 읽어서 String으로 응답한다.
     * JSON 파일을 읽어 String으로 리턴
     */
    private fun readFile(path: String): String {
        return String(ClassPathResource(path).inputStream.readBytes())
    }

    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .registerModules(JavaTimeModule(), Jdk8Module())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .apply { this.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE }
}

/**
 * @param jsonPath json file의 경로를 작성한다.
 * @param clazz MongoDB에 저장할 Document 객체
 * @param collectionName [clazz]와 Document collection 이름이 다른 경우 명시, 명시하지 않는 경우 [clazz]의 Document의 collection 으로 저장
 *
 * @see com.example.mongostudy.MongoDataSetupExecutionListenerTest.mongoDataSetup
 * @see com.example.mongostudy.MongoDataSetupExecutionListenerTest.mongoDataSetupCollectionName
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MongoDataSetup(
    val jsonPath: String,
    val clazz: KClass<*>,
    val collectionName: String = ""
)

/**
 * 여러 데이터를 셋업이 필요한 경우
 * @param mongoDataSetup
 *
 * @see com.example.mongostudy.MongoDataSetupExecutionListenerTest.mongoDataSetups
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class MongoDataSetups(vararg val mongoDataSetup: MongoDataSetup)
