package com.example.mongostudy.mongo

import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaField
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.and
import java.lang.IllegalStateException
import java.time.LocalDate


object MongoSupport

fun <T> KProperty<T>.fieldName(): String {
    val javaField = this.javaField ?: throw IllegalStateException("The property does not have a backing field.")
    val annotation = javaField.getAnnotation(Field::class.java) ?: throw IllegalStateException("@Field의 name 속성은 반드시 필요합니다.")
    return annotation.name
}

fun Criteria.eqIfNotNull(property: KProperty<*>, value: Any?): Criteria = when {
    value != null -> Criteria.where(property.name).`is`(value)
    else -> this
}

fun Criteria.gtIfNotNull(property: KProperty<*>, value: Any?): Criteria = when {
    value != null -> Criteria.where(property.name).gt(value)
    else -> this
}

fun Criteria.ltIfNotNull(property: KProperty<*>, value: Any?): Criteria = when {
    value != null -> Criteria.where(property.name).lt(value)
    else -> this
}

fun Criteria.gteIfNotNull(property: KProperty<*>, value: Any?): Criteria = when {
    value != null -> Criteria.where(property.name).gte(value)
    else -> this
}

fun Criteria.lteIfNotNull(property: KProperty<*>, value: Any?): Criteria = when {
    value != null -> Criteria.where(property.name).lte(value)
    else -> this
}