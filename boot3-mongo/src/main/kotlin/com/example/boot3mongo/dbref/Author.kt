package com.example.boot3mongo.dbref

import com.example.boot3mongo.mongo.Auditable
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType

@Document(collection = "authors")
class Author(
    @Field(name = "name", targetType = FieldType.STRING)
    val name: String
) : Auditable()

@Document(collection = "posts")
data class Post(
    @Field(name = "name", targetType = FieldType.STRING)
    val title: String,
    @Field(name = "name", targetType = FieldType.STRING)
    val content: String,
//    @DBRef(lazy = true)
    @DBRef(lazy = false)
    val author: Author
) : Auditable()