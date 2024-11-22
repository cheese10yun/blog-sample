package com.example.kotlincoroutine

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
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

@Entity(name = "user")
@Table(name = "user")
data class User private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var name: String,
    val email: String,
    @Enumerated(EnumType.STRING)
    var stats: UserStats
) {

    companion object {
        operator fun invoke(
            name: String,
            email: String,
        ): User {
            return User(
                name = name.trim(),
                email = email.trim(),
                id = null,
                stats = UserStats.NORMAL
            )
        }

        fun of(form: UserRegistrationForm): User {
            return User(
                name = form.name,
                email = form.email
            )
        }
    }
}

data class UserRegistrationForm(
    val name: String,
    val email: String,
)

enum class UserStats {
    NORMAL,
    BLOCKED,
}


class UserTest {

    @Test
    fun `test`() {
        val user = User(
            name = " asd ",
            email = " asdas ",
        )


        println(user)
    }

    @Test
    fun `of test`() {
        val form = UserRegistrationForm(
            name = " asd ",
            email = " asdas ",
        )

        val of = User.of(form)


        println(of)
    }
}
