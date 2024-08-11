package com.spring.camp.domain

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import org.junit.jupiter.api.Test

class OrderTest {

    @Test
    fun ㅁㄴㅇㅁㄴㅇ() {

        println("")
    }
}


data class Person(
    val name: String,
    val age: Int
)

class MyServiceTest {

    private val fixtureMonkey = FixtureMonkey.builder().build()

    @Test
    fun `test with fixture monkey`() {
        val person: Person = fixtureMonkey.giveMeOne<Person>()

        println(person) // 생성된 Person 객체 출력
    }
}