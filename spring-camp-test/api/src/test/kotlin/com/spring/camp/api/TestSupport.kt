package com.spring.camp.api

import com.spring.camp.io.ClientTestConfiguration
import io.micrometer.core.instrument.util.IOUtils
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.core.io.ResourceLoader
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import java.io.IOException
import java.nio.charset.StandardCharsets

@SpringBootTest
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
@Import(
    ClientTestConfiguration::class,
)
abstract class TestSupport{

    @Autowired
    protected lateinit var resourceLoader: ResourceLoader

    protected fun readJson(path: String): String {
        return IOUtils.toString(resourceLoader.getResource("classpath:$path").inputStream, StandardCharsets.UTF_8)
    }
}