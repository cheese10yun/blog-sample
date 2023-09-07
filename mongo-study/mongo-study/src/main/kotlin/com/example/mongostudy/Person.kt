package com.example.mongostudy

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository

@Document(collection = "persons")
data class Person(
    @Id
    val id: String? = null,
    val firstName: String,
    val lastName: String
)

interface PersonRepository : MongoRepository<Person, String>