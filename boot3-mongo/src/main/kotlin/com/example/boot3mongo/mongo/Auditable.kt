package com.example.boot3mongo.mongo

import java.time.LocalDateTime
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType

abstract class Auditable {
    @Id
    var id: ObjectId? = null
        internal set

    @Field(name = "created_at", targetType = FieldType.DATE_TIME)
    @CreatedDate
    open lateinit var createdAt: LocalDateTime
        internal set

    @Field(name = "updated_at", targetType = FieldType.DATE_TIME)
    @LastModifiedDate
    open lateinit var updatedAt: LocalDateTime
        internal set
}