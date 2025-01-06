package com.example.mongostudy

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.BDDAssertions.then
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test

class DiffManagerTest {

    private val diffMapper = jacksonObjectMapper()
        .apply {
            propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
            registerModules(SimpleModule().apply { addSerializer(ObjectId::class.java, ObjectIdSerializer()) })
        }

    @Test
    fun `주문 데이터의 필드 변경을 확인한다`() {
        // Given
        val originalOrderJson = """
            {
              "order_id": "ORD123456",
              "customer": {
                "customer_id": "CUST7890",
                "name": "홍길동",
                "contact": {
                  "email": "hong@example.com",
                  "phone": "010-1234-5678",
                  "address": {
                    "street": "서울특별시 종로구",
                    "city": "서울",
                    "zip_code": "03000",
                    "country": "KR"
                  }
                }
              },
              "items": [
                {
                  "product": {
                    "product_id": "PROD001",
                    "product_name": "노트북",
                    "category": {
                      "main_category": "전자제품",
                      "sub_category": "컴퓨터"
                    }
                  },
                  "quantity": 1,
                  "price": 1500000
                }
              ],
              "payment": {
                "method": "신용카드",
                "transaction_id": "TXN987654321",
                "status": "완료"
              }
            }

        """.trimIndent()

        val newOrderJson = """
            {
              "order_id": "ORD123456",
              "customer": {
                "customer_id": "CUST7890",
                "name": "홍길동",
                "contact": {
                  "email": "hong@example.com",
                  "phone": "010-1234-5678",
                  "address": {
                    "street": "서울특별시 강남구",
                    "city": "서울",
                    "zip_code": "03000",
                    "country": "KR"
                  }
                }
              },
              "items": [
                {
                  "product": {
                    "product_id": "PROD001",
                    "product_name": "노트북",
                    "category": {
                      "main_category": "전자제품",
                      "sub_category": "컴퓨터"
                    }
                  },
                  "quantity": 1,
                  "price": 1400000
                }
              ],
              "payment": {
                "method": "신용카드",
                "transaction_id": "TXN987654322",
                "status": "완료"
              }
            }

        """.trimIndent()

        val originalOrder: Order = diffMapper.readValue(originalOrderJson)
        val newOrder: Order = diffMapper.readValue(newOrderJson)

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