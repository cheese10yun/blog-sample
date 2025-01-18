package com.example.mongostudy.member

import org.bson.types.ObjectId

object MemberQueryForm {
    data class UpdateName(
        val id: ObjectId,
        val name: String
    )
}