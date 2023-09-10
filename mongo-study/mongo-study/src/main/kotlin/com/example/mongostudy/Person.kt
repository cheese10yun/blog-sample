package com.example.mongostudy

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Service

@Document(collection = "persons")
data class Person(
    @Id
    val id: String? = null,
    val firstName: String,
    val lastName: String
)

interface PersonRepository : MongoRepository<Person, String>

@Service
class PersonQueryService(
    private val personRepository: PersonRepository,
    private val mongoTemplate: MongoTemplate
) {

    fun findBy(firstName: String): List<Person> {
        val query = Query.query(Criteria.where("firstName").isEqualTo(firstName))
        return mongoTemplate.find(query, Person::class.java)
    }
}