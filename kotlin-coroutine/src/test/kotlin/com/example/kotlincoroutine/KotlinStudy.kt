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
class User private  constructor(
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
                stats =  UserStats.NORMAL
            )
        }
    }

    override fun toString(): String {
        return "User(name='$name', email='$email')"
    }
}

enum class UserStats {
    NORMAL,
    BLOCKED,
}


class UserTest {

    @Test
    fun asdasdasdsad() {
        User(
            name = " asd ",
            email = " asdas ",
        )
    }
}
