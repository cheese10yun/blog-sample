package com.example.batch

import com.example.batch.common.PageResponse
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ResourceLoader
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
@ActiveProfiles("test")
abstract class SpringBootTestSupport {
    @Autowired
    protected lateinit var resourceLoader: ResourceLoader

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    protected val CLASS_PATH = "classpath:"

    protected fun <T> readValue(path: String, clazz: Class<T>): T {
        val json = resourceLoader.getResource(path).inputStream
        return objectMapper.readValue(json, clazz)
    }

    protected fun <T> read(path: String, typeReference: TypeReference<List<T>>): List<T> {
        val json = resourceLoader.getResource("$CLASS_PATH$path").inputStream
        return objectMapper.readValue(json, typeReference)
    }

    protected fun <T> readPage(path: String, typeReference: TypeReference<PageResponse<T>>): PageResponse<T> {
        val json = resourceLoader.getResource("$CLASS_PATH$path").inputStream
        return objectMapper.readValue(json, typeReference)
    }
}