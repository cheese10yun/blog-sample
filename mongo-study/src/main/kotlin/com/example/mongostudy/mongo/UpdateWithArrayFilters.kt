package com.example.mongostudy.mongo

import org.bson.Document
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.UpdateDefinition

class UpdateWithArrayFilters(
    private val update: Update,
    private val arrayFilters: List<Document>
) : UpdateDefinition {
    override fun isIsolated(): Boolean = update.isIsolated

    override fun getUpdateObject(): Document {
        val updateObject = update.updateObject
        updateObject["arrayFilters"] = arrayFilters
        return updateObject
    }

    override fun modifies(key: String): Boolean = update.modifies(key)

    override fun inc(key: String) {
        update.inc(key)
    }

    // UpdateDefinition.ArrayFilter는 단순 람다로 구현할 수 있음
    override fun getArrayFilters(): List<UpdateDefinition.ArrayFilter> {
        return arrayFilters
            .map { doc -> UpdateDefinition.ArrayFilter { doc } }
            .toList()
    }
}