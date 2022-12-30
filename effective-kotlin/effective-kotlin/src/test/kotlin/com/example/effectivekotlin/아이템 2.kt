package com.example.effectivekotlin

import org.junit.jupiter.api.Test
import java.io.File

class `아이템 2` {

    data class User(
        val name: String
    )

//    @Test
//    internal fun `변수의 스코프를 최소화하라`(users: List<User>) {
//        // 나쁜예
//        var user: User
//        for (index in users.indices) {
//            user = users[index]
//            println("User at $index is $user")
//
//        }
//
//        // 조금 더 좋은 예
//        for (index in users.indices) {
//            val user = users[index]
//            println("User at $index is $user")
//        }
//
//        // 제일 좋은 예
//        for ((index, user) in users.withIndex()) {
//            println("User at $index is $user")
//        }
//    }

//    @Test
//    internal fun `구조 분해 선언`() {
//        // 나쁜 예
//        val user: User
//        if (true) {
//            user = getValue()
//        } else {
//            user = User()
//        }
//
//        // 좋은 예
//        val user: User = if (true) {
//            getValue()
//        } else {
//            User()
//        }
//    }


    @Test
    internal fun `아이템 4 inferred 타입으로 리턴하지 말라`() {
        open class Animal
        class Zebra : Animal()

//        var animal = Zebra()
//        animal = Animal() // 오류: type mismatch

        var animal: Animal = Zebra()
        animal = Animal() // 오류 없음
    }

    @Test
    internal fun `Failure 처리 방법`() {


    }

//    inline fun <reified T> String.readObjectOrNull(): T? {
//        if (...) {
//            return null
//        }
//
//        return result
//    }

//    inline fun <reified T> String.readObject(): Result<T> {
//        if (...){
//            return Failure(JsonParsingException())
//        }
//
//        return Success(result)
//    }

    sealed class Result<out T>
    class Success<out T>(val result: T) : Result<T>()
    class Failure(val throwable: Throwable) : Result<Nothing>()

    class JsonParsingException : Exception()


    @Test
    internal fun use() {

//        fun countCharactersInFile(path: String): Int {
//            BufferedReader(FileReader(path)).use { reader ->
//                return reader.lineSequence().sumOf { it.length }
//            }
//        }

        fun countCharactersInFile(path: String): Int {
            File(path).useLines { lines ->
                return lines.sumOf { it.length }
            }
        }

    }
}


//data class UserControllerTest {
//    private lateinit var dao: UserDao
//    private lateinit var controller: UserController
//
//
//    @BeforeEach
//    fun init() {
//        dao = mockk()
//        controller = UserController(dao)
//    }
//
//    @Test
//    internal fun test() {
//        controller.doSometing()
//    }
//}




