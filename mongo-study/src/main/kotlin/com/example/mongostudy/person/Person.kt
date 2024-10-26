package com.example.mongostudy.person

import com.example.mongostudy.mongo.MongoCustomRepositorySupport
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.repository.MongoRepository

@Document(collection = "persons")
class Person(
    @Id
    val id: String? = null,
    @Field(name = "", targetType = FieldType.DATE_TIME)
    val firstName: String,
    val lastName: String
)

interface PersonRepository : MongoRepository<Person, String>, PersonCustomRepository

interface PersonCustomRepository {
    fun findPage(
        lastName: String,
        pageable: Pageable
    ): Page<Person>

    fun bulkInsert(persons: List<Person>)
    fun updateById(personId: ObjectId, newAddress: String)
    fun updatePersonsAddress(lastName: String, newAddress: String): Long
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

    override fun updateById(personId: ObjectId, newAddress: String) {
        val criteria = Criteria.where("id").`is`(personId)
        val update = Update().set("address", newAddress)
        val partialUpdate = updateOne(criteria, update)
    }

    override fun updatePersonsAddress(lastName: String, newAddress: String): Long {
        val criteria = Criteria.where("lastName").`is`(lastName)
        val update = Update().set("address", newAddress)

        return updateMany(criteria, update)
    }

    override fun bulkInsert(persons: List<Person>) {
        insertMany(persons)
    }
}


//@Service
//class PersonQueryService(
//    private val personRepository: PersonRepository,
//    private val mongoTemplate: MongoTemplate
//) {
//
//    fun findBy(firstName: String): List<Person> {
//        val query = Query.query(Criteria.where("firstName").isEqualTo(firstName))
//        return mongoTemplate.find(query, Person::class.java)
//    }
//
//    fun groupByLastName(): List<LastNameGroup> {
//
//        val aggregation = newAggregation(
//            group("lastName"),
//            project(LastNameGroup::class.java)
//                .and(previousOperation()).`as`("lastName"),
//        )
//
//        val results =
//            mongoTemplate.aggregate(
//                aggregation,
//                "persons",
//                LastNameGroup::class.java
//            )
//
//        return results.mappedResults
//    }
//
//    fun findByName(name: String): List<Person> {
//        val person = QPerson.person
//        val eq = person.firstName.eq(name)
//
//
//        return personRepository.findAll(eq).toList()
//    }
//}

data class LastNameGroup(
    val lastName: String
)