package com.example.kotlincoroutine.a1

import com.example.kotlincoroutine.logger
import kotlin.properties.Delegates
import org.junit.jupiter.api.Test

typealias UserPointAssociation = Pair<User, UserPoint>

class SampleTest {

    @Test
    fun `copy test `() {
        val user = User(
            id = 1,
            name = "name",
            email = "email@asd.com",
            nickName = "nickName"
        )

        val userCopy = user.copy(
            email = "email@asd.com 암호화"
        )


        println("user: $user")
        println("userCopy: $userCopy")


        println("user: ${System.identityHashCode(user)}")
        println("userCopy: ${System.identityHashCode(userCopy)}")
    }


    @Test
    fun `Pair`() {
        val user = Pair(
            first = "name",
            second = "email@asd.com"
        )

        println("user.name: ${user.first}")
        println("user.email: ${user.second}")


        val (userName, userEmail) = Pair(
            first = "name",
            second = "email@asd.com"
        )

        println("user.name: $userName")
        println("user.email: $userEmail")
    }


    fun calculate() {


    }
}


data class User(
    val id: Long,
    val name: String,
    val email: String,
    val nickName: String
)

data class UserPoint(
    val id: Long,
    val point: Int
)

class UserPointCalculator(
    private val userRepository: UserRepository,
    private val userPointRepository: UserPointRepository,
    private val userClient: UserClient,
) {

    private val useraClient: UserClient by Delegates.notNull()

    private val log by logger()

    fun calculate() {
        val users = userRepository.findUserByIds(listOf(1, 2, 3))
        val points = userPointRepository.findUserPoint(listOf(1, 2, 3))
            .associateBy { it.id }

        val userPoints = users.map {
            Triple(
                first = it.name,
                second = it.email,
                third = points[it.id]!!.point
            )
        }

//        val (userName, userEmail, userPoint) = userPoints.first()
//
//        val pair = Pair<String, String>()
//
//
//        for (userPoint in userPoints) {
//            println("user name: ${userPoint.first}, user email  ${userPoint.second}, user point ${userPoint.third}")
//        }
//
//        for ((userName, userEmail, userPoint) in userPoints) {
//            println("user name: ${userName}, user email  ${userEmail}, user point $userPoint")
//        }

        val userPointAssociations = users.map {
            UserPointAssociation(
                first = it,
                second = points[it.id]!!
            )
        }

        val (user, userPoint) = userPointAssociations.first()
    }

    fun getUser(userId: Long): User {
        return runCatching { userClient.getUser(userId) }
            .onFailure { throw IllegalArgumentException("Failed to fetch user data for user ID $userId") }
            .getOrThrow()
    }
}

interface UserRepository {

    fun findUserByIds(ids: List<Long>): List<User>
    fun findById(id: Long): User
}

interface UserClient {

    fun findUserByIds(ids: List<Long>): List<User>
    fun getUser(id: Long): User
}

interface UserPointRepository {

    fun findUserPoint(ids: List<Long>): List<UserPoint>
}
