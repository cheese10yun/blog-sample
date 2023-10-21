package com.example.mongostudy.mongo

import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import org.springframework.data.mongodb.core.mapping.Field


object MongoSupport

fun <T : Any> KProperty1<T, *>.fieldName(): String {
    return this.findAnnotation<Field>()?.name?: throw IllegalStateException("@Field의 name 속성은 반드시 필요합니다.")
}