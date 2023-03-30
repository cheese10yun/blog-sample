package com.spring.camp.api

//import com.spring.camp.io.ClientTestConfiguration
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestConstructor

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@Import(ClientTestConfiguration::class)
abstract class TestSupport


//@SpringBootApplication
//@Import(ClientTestConfiguration::class)
//class ApiApplicationTestApplication