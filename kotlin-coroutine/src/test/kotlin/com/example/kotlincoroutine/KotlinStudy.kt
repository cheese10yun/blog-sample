package com.example.kotlincoroutine

import org.junit.jupiter.api.Test

class KotlinStudy {

    @Test
    fun `User test`() {
        val user = User(
            name = " asd ",
            email = " asd@asd.com"
        )

        println(user)
    }
}

class User private constructor(
    val name: String,
    val email: String,
) {

    companion object {
        operator fun invoke(
            name: String,
            email: String,
        ): User {
            return User(name.trim(), email.trim())
        }
    }

    override fun toString(): String {
        return "User(name='$name', email='$email')"
    }
}