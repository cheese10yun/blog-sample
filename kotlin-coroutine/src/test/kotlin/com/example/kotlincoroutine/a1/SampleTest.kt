package com.example.kotlincoroutine.a1

import org.junit.jupiter.api.Test

class SampleTest {

    @Test
    fun `test `() {
        val user = User(
            name = "name",
            email = "email@asd.com"
        )

        val userCopy = user.copy(
            name = "new name"
        )

        println("user: ${System.identityHashCode(user)}")
        println("userCopy: ${System.identityHashCode(userCopy)}")
    }
}


data class User(
    val name: String,
    val email: String
)