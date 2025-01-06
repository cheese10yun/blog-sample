package com.example.mongostudy

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.BDDAssertions.then
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource

class DiffManagerTest {
    private fun readFile(path: String): String = String(ClassPathResource(path).inputStream.readBytes())
    private val diffMapper = jacksonObjectMapper()
        .apply {
            propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
            registerModules(SimpleModule().apply { addSerializer(ObjectId::class.java, ObjectIdSerializer()) })
        }

    @Test
    fun `주문 데이터의 필드 변경을 확인한다`() {
        // Given
        val originalOrder: Order = diffMapper.readValue(readFile("/diff-origin.json"))
        val newOrder: Order = diffMapper.readValue(readFile("/diff-new.json"))

        // When
        val result = DiffManager.calculateDifferences(
            originItems = listOf(originalOrder),
            newItems = listOf(newOrder),
            associateByKey = { it.orderId },
            groupByKey = { it.orderId }
        )

        // Then
        val differences = result["ORD123456"]
        then(differences).isNotNull
        then(differences!!.size).isEqualTo(3)
        then(differences).allSatisfy { key, value ->
            when (key) {
                "customer/contact/address/street" -> {
                    then(value.origin).isEqualTo("서울특별시 종로구")
                    then(value.new).isEqualTo("서울특별시 강남구")
                }
                "items/0/price" -> {
                    then(value.origin).isEqualTo("1500000")
                    then(value.new).isEqualTo("1400000")
                }
                "payment/transaction_id" -> {
                    then(value.origin).isEqualTo("TXN987654321")
                    then(value.new).isEqualTo("TXN987654322")
                }
                else -> throw IllegalStateException("검증 되지 않은 값이 들어왔습니다. 신규 데이터 or 로직 변경에 따른 테스트 코드를 보강해주세요")
            }
        }
    }
}


data class Order(
    val orderId: String,
    val customer: Customer,
    val items: List<Item>,
    val payment: Payment
)

data class Customer(
    val customerId: String,
    val name: String,
    val contact: Contact
)

data class Contact(
    val email: String,
    val phone: String,
    val address: Address
)

data class Address(
    val street: String,
    val city: String,
    val zipCode: String,
    val country: String
)

data class Item(
    val product: Product,
    val quantity: Int,
    val price: Int
)

data class Product(
    val productId: String,
    val productName: String,
    val category: Category
)

data class Category(
    val mainCategory: String,
    val subCategory: String
)

data class Payment(
    val method: String,
    val transactionId: String,
    val status: String
)