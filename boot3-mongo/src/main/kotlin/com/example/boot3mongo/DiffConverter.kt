package com.example.boot3mongo

import org.bson.Document
import org.springframework.data.convert.PropertyValueConverter
import org.springframework.data.convert.ValueConversionContext

class DiffConverter : PropertyValueConverter<DiffValueTracker, Document, ValueConversionContext<*>> {

    companion object {
        private val ORIGIN = "origin"
        private val NEW = "new"
    }

    override fun read(
        value: Document,
        context: ValueConversionContext<*>
    ): DiffValueTracker {
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
        value: DiffValueTracker,
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