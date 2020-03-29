package com.example.springmocktest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc

@AutoConfigureMockMvc
abstract class SpringApiTestSupport : SpringContextTestSupport() {

    @Autowired
    protected lateinit var mockMvc: MockMvc

}
