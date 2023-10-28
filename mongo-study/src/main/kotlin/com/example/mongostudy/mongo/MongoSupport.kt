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

fun <T> Criteria.eqIfNotNull(property: KProperty<T>, param: T?): Criteria {
    param?.let {
        this.and(property).`is`(param)
    }
    return this
}

/**
 * regex 검색, 대부분 like 검색을 진행한다.
 *
 * @param ignoreCase 대소문자 구분 여부, true 경우 대소문자 구분을 무시하고 조회한다.
 */
fun Criteria.regexIfNotNull(property: KProperty<*>, regex: String, ignoreCase: Boolean = true): Criteria {
    return when {
        ignoreCase -> this.and(property.name).regex(regex, "i")
        else -> this.and(property.name).regex(regex, null)
    }
}


fun <T> Criteria.inIfNotNull(property: KProperty<T>, param: List<T>?): Criteria {
    param?.let {
        this.and(property).`in`(param)
    }
    return this
}

fun <T> Criteria.inIfNotEmpty(property: KProperty<T>, param: List<T>?): Criteria {
    if (!param.isNullOrEmpty()) {
        this.and(property).`in`(param)
    }
    return this
}

fun <T> Criteria.between(property: KProperty<T>, param: List<LocalDate>): Criteria {
    return this.and(property).gte(param[0]).lt(param[1].plusDays(1))
}

/**
 * D[0] <= x < D[1] + 1(day)
 */
fun <T> Criteria.betweenIfNotNull(property: KProperty<T>, param: List<LocalDate>?): Criteria {
    param?.let {
        this.and(property).gte(it[0]).lt(it[1].plusDays(1))
    }
    return this
}
