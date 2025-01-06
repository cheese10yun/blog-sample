package com.example.mongostudy

import org.bson.Document
import org.springframework.data.convert.PropertyValueConverter
import org.springframework.data.convert.ValueConversionContext

typealias DiffValueType = Map<String, DiffValue<String, String>>

class DiffConverter : PropertyValueConverter<DiffValueType, Document, ValueConversionContext<*>> {

    companion object {
        private val ORIGIN = "origin"
        private val NEW = "new"
    }

    override fun read(
        value: Document,
        context: ValueConversionContext<*>
    ): DiffValueType {
        return value
            .map {
                val diffValue = it.value as Document
                val key = it.key!!
                key to DiffValue(
                    origin = diffValue[ORIGIN].toString(),
                    new = diffValue[NEW].toString(),
                )
            }
            .toMap()
    }

    override fun write(
        value: DiffValueType,
        context: ValueConversionContext<*>
    ): Document {
        val mapValues = value.mapValues { (key, value) ->
            mapOf(
                ORIGIN to value.origin,
                NEW to value.new
            )
        }
        return Document(mapValues)
    }
}