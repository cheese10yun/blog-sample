package com.example.intellijtest

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.io.File

@SpringBootTest
class IntellijTestApplicationTests {

    fun print(body: () -> String) {
        println(body())
    }

    @Test
    fun test() {
        print({
            "리턴되는 값을 정의"
        })
    }
}


class AA(
    name: String,
) {
    var name: String
        private set

    init {
        this.name = name
    }
}