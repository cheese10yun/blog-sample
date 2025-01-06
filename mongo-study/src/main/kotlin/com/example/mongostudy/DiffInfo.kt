package com.example.mongostudy

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.convert.ValueConverter
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.repository.MongoRepository

@Document(collection = "diff_info")
data class DiffInfo(

    @Field(name = "key")
    val key: String,

    @Field(name = "name")
    var name: String,

    @Field(name = "email")
    var email: String,

    @ValueConverter(DiffConverter::class)
    @Field(name = "diff")
    var diff: DiffValueType = emptyMap()

) {
    @Id
    var id: ObjectId? = null
        internal set
}

interface DiffInfoRepository : MongoRepository<DiffInfo, ObjectId>, DiffInfoCustomRepository

interface DiffInfoCustomRepository

data class DiffValue<out A, out B>(
    @Field(name = "origin")
    val origin: A,
    @Field(name = "new")
    val new: B
)

