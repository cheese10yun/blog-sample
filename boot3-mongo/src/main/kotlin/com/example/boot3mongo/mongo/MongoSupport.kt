package com.example.boot3mongo.mongo

import java.util.Locale
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField
import org.springframework.data.mapping.toDotPath
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.and

object MongoSupport

fun <V> dotPath(field: KProperty<V>): String {
    return field.toDotPath().snakeCase()
}

fun <V> dotPath(vararg fields: KProperty<V>): String {
    return fields.joinToString(separator = ".") { it.field() }
}

fun <T> Criteria.eqIfNotNull(property: KProperty<T>, value: T?): Criteria {
    value?.let { this.and(property).`is`(value) }
    return this
}

fun <T> Criteria.neIfNotNull(property: KProperty<T>, value: T?): Criteria {
    value?.let { this.and(property).ne(value) }
    return this
}

fun <T> Criteria.gtIfNotNull(property: KProperty<T>, value: T?): Criteria {
    value?.let { this.and(property).gt(value) }
    return this
}

fun <T> Criteria.gteIfNotNull(property: KProperty<T>, value: T?): Criteria {
    value?.let { this.and(property).gte(value) }
    return this
}

fun <T> Criteria.ltIfNotNull(property: KProperty<T>, value: T?): Criteria {
    value?.let { this.and(property).lt(value) }
    return this
}

fun <T> Criteria.lteIfNotNull(property: KProperty<T>, value: T?): Criteria {
    value?.let { this.and(property).lte(value) }
    return this
}

fun <T> Criteria.inIfNotNull(property: KProperty<T>, values: Collection<T>?): Criteria {
    values?.let { this.and(property).`in`(values) }
    return this
}

fun <T> Criteria.ninIfNotNull(property: KProperty<T>, values: Collection<T>?): Criteria {
    values?.let { this.and(property).nin(values) }
    return this
}

private fun <V> KProperty<V>.field(): String {
    return field(
        fieldAnnotation = this.javaField?.getAnnotation(Field::class.java),
        memberField = this.name,
    )
}

private fun field(
    fieldAnnotation: Field?,
    memberField: String,
): String {
    return when {
        fieldAnnotation == null -> memberField.snakeCase()
        fieldAnnotation.value.isNotEmpty() -> fieldAnnotation.value
        fieldAnnotation.name.isNotEmpty() -> fieldAnnotation.name
        else -> memberField.snakeCase()
    }
}

private fun String.snakeCase(): String {
    return this.replace(Regex("([a-z])([A-Z]+)")) {
        it.groupValues[1] + "_" + it.groupValues[2].lowercase(Locale.getDefault())
    }.lowercase(Locale.getDefault())
}