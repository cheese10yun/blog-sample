package com.example.mongostudy.mongo

import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaField
import org.springframework.data.mapping.toDotPath
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

fun <V> dotPath(kProperty: KProperty<V>): String {
    val field = kProperty.javaField?.getAnnotation(Field::class.java)
    return when {
        field == null -> kProperty.name
        field.value.isNotEmpty() -> field.value
        field.name.isNotEmpty() -> field.name
        else -> kProperty.name
    }
}

fun <T> Criteria.eqIfNotNull(property: KProperty<T>, value: T?): Criteria {
    return value?.let { this.and(property).`is`(value) } ?: this
}

fun <T> Criteria.neIfNotNull(property: KProperty<T>, value: T?): Criteria {
    return value?.let { this.and(property).ne(value) } ?: this
}

fun <T> Criteria.gtIfNotNull(property: KProperty<T>, value: T?): Criteria {
    return value?.let { this.and(property).gt(value) } ?: this
}

fun <T> Criteria.gteIfNotNull(property: KProperty<T>, value: T?): Criteria {
    return value?.let { this.and(property).gte(value) } ?: this
}

fun <T> Criteria.ltIfNotNull(property: KProperty<T>, value: T?): Criteria {
    return value?.let { this.and(property).lt(value) } ?: this
}

fun <T> Criteria.lteIfNotNull(property: KProperty<T>, value: T?): Criteria {
    return value?.let { this.and(property).lte(value) } ?: this
}

fun <T> Criteria.inIfNotNull(property: KProperty<T>, values: Collection<T>?): Criteria {
    return values?.let { this.and(property).`in`(values) } ?: this
}

fun <T> Criteria.ninIfNotNull(property: KProperty<T>, values: Collection<T>?): Criteria {
    return values?.let { this.and(property).nin(values) } ?: this
}