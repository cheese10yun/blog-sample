package com.example.mongostudy.mongo

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

// 오디터 클래스
abstract class Auditable {
    @Id
    var id: ObjectId? = null
        internal set

    @Field(name = "created_at")
    @CreatedDate
    lateinit var createdAt: LocalDateTime
        internal set

    @Field(name = "updated_at")
    @LastModifiedDate
    lateinit var updatedAt: LocalDateTime
        internal set
}

