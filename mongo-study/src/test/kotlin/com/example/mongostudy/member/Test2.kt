package com.example.mongostudy.member

import org.springframework.data.mapping.PropertyPath
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField
import org.springframework.data.mapping.div
import org.springframework.data.mongodb.core.mapping.Field

fun main() {
    val propertyPath = (Order::address / Address::addressDetail / AddressDetail::zipCode).toString()
    val fieldPath = toFieldPath(propertyPath)
    println(fieldPath)
}

fun toFieldPath(propertyPath: String): String {
    // PropertyPath의 문자열 표현을 기반으로 필드 경로 생성
    val props = propertyPath.split('.').map { it.trim() }
    return props.joinToString(".") { getFieldName(it) }
}

fun getFieldName(propertyName: String): String {
    // 실제 필드 이름을 찾아 @Field 어노테이션의 name 값을 반환
    return when (propertyName) {
        "address" -> Order::address.getFieldName()
        "addressDetail" -> Address::addressDetail.getFieldName()
        "zipCode" -> AddressDetail::zipCode.getFieldName()
        else -> propertyName
    }
}

fun KProperty1<*, *>.getFieldName(): String =
    this.javaField?.getAnnotation(Field::class.java)?.name ?: this.name

// 클래스 정의
data class Order(@Field(name = "address_test") val address: Address)
data class Address(@Field(name = "address_detail_test") val addressDetail: AddressDetail)
data class AddressDetail(@Field(name = "zip_code_test") val zipCode: String)
