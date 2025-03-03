package com.example.mongostudy.dbref

import com.example.mongostudy.mongo.Auditable
import com.example.mongostudy.mongo.MongoCustomRepositorySupport
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import org.springframework.data.mongodb.repository.MongoRepository

@Document(collection = "author")
class Author(
    @Field(name = "name", targetType = FieldType.STRING)
    val name: String
) : Auditable()

interface AuthorRepository : MongoRepository<Author, ObjectId>, AuthorCustomRepository

interface AuthorCustomRepository

class AuthorCustomRepositoryImpl(mongoTemplate: MongoTemplate) : AuthorCustomRepository, MongoCustomRepositorySupport<Author>(
    Author::class.java,
    mongoTemplate
)

