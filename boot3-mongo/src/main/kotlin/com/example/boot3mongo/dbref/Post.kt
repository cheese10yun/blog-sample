package com.example.boot3mongo.dbref

import com.example.boot3mongo.MongoCustomRepositorySupport
import com.example.boot3mongo.mongo.Auditable
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import org.springframework.data.mongodb.repository.MongoRepository

@Document(collection = "post")
class Post(
    @Field(name = "title", targetType = FieldType.STRING)
    val title: String,
    @Field(name = "content", targetType = FieldType.STRING)
    val content: String,
//    @DBRef(lazy = true)
    @DBRef(lazy = false)
    val author: Author
) : Auditable()

interface PostRepository : MongoRepository<Post, ObjectId>, PostCustomRepository

interface PostCustomRepository

class PostCustomRepositoryImpl(mongoTemplate: MongoTemplate) : PostCustomRepository, MongoCustomRepositorySupport<Post>(
    Post::class.java,
    mongoTemplate
)