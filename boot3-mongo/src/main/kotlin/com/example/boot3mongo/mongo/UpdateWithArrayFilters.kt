package com.example.boot3mongo.mongo

import org.bson.Document
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.UpdateDefinition

class UpdateWithArrayFilters(
    private val update: Update,
    private val arrayFilters: List<Document>
) : UpdateDefinition {
    override fun isIsolated(): Boolean = update.isIsolated

    override fun getUpdateObject(): Document = update.updateObject

    override fun modifies(key: String): Boolean = update.modifies(key)

    override fun inc(key: String) {
        update.inc(key)
    }

    // getArrayFilters()에서 arrayFilters를 UpdateDefinition.ArrayFilter 타입으로 변환하여 반환
    override fun getArrayFilters(): List<UpdateDefinition.ArrayFilter> {
        return arrayFilters
            .map { doc -> UpdateDefinition.ArrayFilter { doc } }
            .toList()
    }
}