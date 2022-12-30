package com.example.effectivekotlin

import org.junit.jupiter.api.Test
import java.util.SortedSet
import java.util.TreeSet

class `item 1 가변성을 제한하라` {


//    fun calculate(): Int {
////        println("Calculating...")
//        return 42
//    }
//
//    val fizz = calculate()
//    val buzz
//        get() = calculate()
//
//    @Test
//    internal fun `test 1`() {
//
//        println(fizz) // 42
//        println(fizz) // 42
//        println(buzz) // Calculating... 42
//        println(buzz) // Calculating... 42
//    }
//
//    val name: String? = "Marton"
//    val surname: String = "Braun"
//
//    val fullName: String?
//        get() = name?.let { "$it $surname" }
//
//    val fullName2: String? = name?.let { "$it $surname" }
//
//    @Test
//    internal fun `smart cast`() {
//
//        if (fullName != null) {
////            println(fullName.length) // 오류
//        }
//
//        if (fullName2 != null) {
//            println(fullName2.length) // 12
//        }
//
//    }
//
//    @Test
//    internal fun `down casting 위반`() {
//        val list = listOf(1, 2, 3)
//        if (list is MutableList) {
//            list.add(1) // java.lang.UnsupportedOperationException 오류 발생
//        }
//    }

    data class FullName(
        var name: String,
        val subName: String
    )

//    @Test
//    fun `hash immutable`() {
//
//        val names: SortedSet<FullName> = TreeSet()
//        val person = FullName("AAA", "AAA")
//        names.add(person)
//        names.add(FullName("Jon", "Jon"))
//        names.add(FullName("David", "David"))
//
//        println(names) // [AAA AAA, Jon Jon, David]
//        println(person in names) // true
//
//        person.name = "ZZZ"
//        println(names) // [AAA ZZZ, Jon Jon, David]
//        println(person in names) // false
//    }

    @Test
    internal fun properties() {
        class User(
            val name: String,
            val surname: String,
        ) {
            fun withSurname(surname: String) = User(name, surname)
            override fun toString(): String {
                return "User(name='$name', surname='$surname')"
            }
        }

        var user = User("Maja", "Markiewicz")
        user = user.withSurname("Moskla")
        println(user) // User(name='Maja', surname='Moskla')
    }

    @Test
    internal fun `data copy`() {
        data class User(
            val name: String,
            val surname: String,
        ) {
            fun withSurname(surname: String) = User(name, surname)
            override fun toString(): String {
                return "User(name='$name', surname='$surname')"
            }
        }

        var user = User("Maja", "Markiewicz")
        user = user.copy(surname = "Moskla")
        println(user) // User(name='Maja', surname='Moskla')
    }

    @Test
    internal fun `변경 가능 지점 노출하지 말기`() {
        data class User(val name: String)

        class UserRepository {
            private val storedUsers: MutableMap<Int, String> = mutableMapOf()

            fun loadAll(): MutableMap<Int, String> {
                return storedUsers
            }
        }

        val userRepository = UserRepository()
        val storedUsers = userRepository.loadAll()
        storedUsers[4] = "Kirill"
    }

    @Test
    internal fun `방어 로직`() {
        data class User(val name: String)

        class UserRepository {
            private val storedUsers: MutableMap<Int, String> = mutableMapOf()
            fun loadAll(): Map<Int, String> {
                return storedUsers
            }
        }

        val userRepository = UserRepository()
        val storedUsers = userRepository.loadAll()
//        storedUsers[4] = "Kirill" // 컴파일 오류
    }
}

interface Element {
    val active: Boolean
}

class ActualElement : Element {
    override var active: Boolean = false
}