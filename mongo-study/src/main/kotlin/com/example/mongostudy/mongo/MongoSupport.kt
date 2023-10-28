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

fun KProperty<*>.fieldName(): String {
    // 필드에 직접 적용된 어노테이션을 가져오기
    val fieldAnnotation = this.javaField?.getAnnotation(Field::class.java)

    // 어노테이션이 존재하면 name 속성 값을 반환, 그렇지 않으면 예외 발생
    return fieldAnnotation?.name
        ?: throw IllegalStateException("Property ${this.name} must be annotated with @Field and have a name attribute.")
}

fun Criteria.eqIfNotNull(property: KProperty<*>, value: Any?): Criteria = when {
    value != null -> Criteria.where(property.fieldName()).`is`(value)
    else -> this
}

fun Criteria.gtIfNotNull(property: KProperty<*>, value: Any?): Criteria = when {
    value != null -> Criteria.where(property.fieldName()).gt(value)
    else -> this
}

fun Criteria.ltIfNotNull(property: KProperty<*>, value: Any?): Criteria = when {
    value != null -> Criteria.where(property.fieldName()).lt(value)
    else -> this
}

fun Criteria.gteIfNotNull(property: KProperty<*>, value: Any?): Criteria = when {
    value != null -> Criteria.where(property.fieldName()).gte(value)
    else -> this
}

fun Criteria.lteIfNotNull(property: KProperty<*>, value: Any?): Criteria = when {
    value != null -> Criteria.where(property.fieldName()).lte(value)
    else -> this
}