package com.example.mongostudy

import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField
import org.springframework.data.mapping.div
import org.springframework.data.mongodb.core.mapping.Field

// KProperty로부터 @Field 어노테이션의 name 값을 추출하는 확장 함수
fun KProperty<*>.getFieldName(): String {
    return this.javaField?.getAnnotation(Field::class.java)?.name ?: this.name
}

// 프로퍼티 체인을 MongoDB 필드 경로로 변환하는 확장 함수
fun toFieldPath(vararg props: KProperty<*>): String =
    props.joinToString(separator = ".") { it.getFieldName() }

// 클래스 정의
data class Order(@Field(name = "address_test") val address: Address)
data class Address(@Field(name = "address_detail_test") val addressDetail: AddressDetail)
data class AddressDetail(val zipCode: String) // @Field 어노테이션 없음

// 사용 예
fun main() {
    val fieldPath = toFieldPath(Order::address / Address::addressDetail / AddressDetail::zipCode)
    println(fieldPath) // 출력: address_test.address_detail_test.zipCode
}


// address.addressDetail.zipCode


