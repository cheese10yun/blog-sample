package com.example.kotlincoroutine

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import org.junit.jupiter.api.Test
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class JsonTest {


    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun ㅁㅇㅁㄴㅇㄴㅁ() {
        val person = Person("John", 30)
        val jsonString = Json.encodeToString(person)
        println("Serialized JSON: $jsonString")

// JSON 문자열을 객체로 역직렬화
        val deserializedPerson = Json.decodeFromString<Person>(jsonString)
        println("Deserialized Person: $deserializedPerson")
    }

}


@Serializable
data class Person(
    @SerialName("last_name")
    val lastName: String,
    val age: Int
)


data class Foo(
    val name: String
)

