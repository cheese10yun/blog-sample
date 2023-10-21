package com.example.mongostudy

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.group
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.previousOperation
import org.springframework.data.mongodb.core.aggregation.Aggregation.project
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.stereotype.Service

@Document(collection = "persons")
class Person(
    @Id
    val id: String? = null,
    val firstName: String,
    val lastName: String
)

interface PersonRepository : MongoRepository<Person, String>, QuerydslPredicateExecutor<Person>, PersonCustomRepository

interface PersonCustomRepository {
    fun findPage(
        lastName: String,
        pageable: Pageable
    ): Page<Person>
}

class PersonCustomRepositoryImpl(
    mongoTemplate: MongoTemplate
) : PersonCustomRepository,
    MongoCustomRepositorySupport<Person>(
        Person::class.java,
        mongoTemplate
    ) {

    override fun findPage(
        lastName: String,
        pageable: Pageable
    ): Page<Person> {
        val criteria = Criteria.where("lastName").`is`(lastName)

        return applyPagination(
            pageable = pageable,
            contentQuery = { query: Query ->
                mongoTemplate.find(query.addCriteria(criteria), documentClass)
            },
            countQuery = { query: Query ->
                mongoTemplate.count(query.addCriteria(criteria), documentClass)
            }
        )
    }

    fun updateById(personId: ObjectId, newAddress: String) {
        val criteria = Criteria.where("id").`is`(personId)
        val update = Update().set("address", newAddress)
        val partialUpdate = updateOne(criteria, update)
    }

    fun updatePersonsAddress(lastName: String, newAddress: String): Long {
        val criteria = Criteria.where("lastName").`is`(lastName)
        val update = Update().set("address", newAddress)

        return updateMany(criteria, update)
    }

    fun insertMultiplePersons(persons: List<Person>) {
        insertMany(persons)
    }

}


@Service
class PersonQueryService(
    private val personRepository: PersonRepository,
    private val mongoTemplate: MongoTemplate
) {

    fun findBy(firstName: String): List<Person> {
        val query = Query.query(Criteria.where("firstName").isEqualTo(firstName))
        return mongoTemplate.find(query, Person::class.java)
    }

    fun groupByLastName(): List<LastNameGroup> {

        val aggregation = newAggregation(
            group("lastName"),
            project(LastNameGroup::class.java)
                .and(previousOperation()).`as`("lastName"),
        )

        val results =
            mongoTemplate.aggregate(
                aggregation,
                "persons",
                LastNameGroup::class.java
            )

        return results.mappedResults
    }

    fun asd(name: String): List<Person> {

        val person = QPerson.person

        return personRepository.findAll(person.firstName.eq(name)).toList()

    }
}

data class LastNameGroup(
    val lastName: String
)