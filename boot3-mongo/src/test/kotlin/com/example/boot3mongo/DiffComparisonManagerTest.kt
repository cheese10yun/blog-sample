package com.example.boot3mongo

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.BDDAssertions.then
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource

class DiffComparisonManagerTest {
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
        val result = DiffComparisonManager.calculateDifferences(
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

    @Test
    fun `diff test`() {
        // Given
        val originProducts: List<Product> = diffMapper.readValue(readFile("/diff-array-origin.json"))
        val newProducts: List<Product> = diffMapper.readValue(readFile("/diff-array-new.json"))

        // When
        val results = DiffComparisonManager.calculateDifferences(
            originItems = originProducts,
            newItems = newProducts,
            associateByKey = Product::productId,
            groupByKey = Product::productId
        )

        then(results).hasSize(2)
        then(results).allSatisfy { associateByKey, diff ->
            when (associateByKey) {
                "PROD001" -> {
                    then(diff).allSatisfy { groupByKey, value ->
                        when (groupByKey) {
                            "product_name" -> {
                                then(value.origin).isEqualTo("노트북")
                                then(value.new).isEqualTo("울트라 노트북")
                            }
                            else -> throw IllegalStateException("검증 되지 않은 값이 들어왔습니다. 신규 데이터 or 로직 변경에 따른 테스트 코드를 보강해주세요")
                        }
                    }
                }
                "PROD002" -> {
                    then(diff).allSatisfy { groupByKey, value ->
                        when (groupByKey) {
                            "product_name" -> {
                                then(value.origin).isEqualTo("노트북")
                                then(value.new).isEqualTo("울트라 노트북")
                            }
                            else -> throw IllegalStateException("검증 되지 않은 값이 들어왔습니다. 신규 데이터 or 로직 변경에 따른 테스트 코드를 보강해주세요")
                        }
                    }
                }
                else -> throw IllegalStateException("검증 되지 않은 값이 들어왔습니다. 신규 데이터 or 로직 변경에 따른 테스트 코드를 보강해주세요")
            }
        }
    }

    @Test
    fun `calculateDifference - 단일 객체의 변경 사항을 감지한다`() {
        // Given
        val originalProduct = Product(
            productId = "PROD001",
            productName = "노트북",
            category = Category("전자제품", "컴퓨터")
        )
        val newProduct = Product(
            productId = "PROD001",
            productName = "울트라 노트북",
            category = Category("전자제품", "컴퓨터")
        )

        // When
        val result = DiffComparisonManager.calculateDifference(originalProduct, newProduct)

        // Then
        then(result).hasSize(1)
        then(result["product_name"]).isNotNull
        then(result["product_name"]?.origin).isEqualTo("노트북")
        then(result["product_name"]?.new).isEqualTo("울트라 노트북")
    }

    @Test
    fun `calculateDifference - 동일한 객체는 변경 사항이 없다`() {
        // Given
        val product = Product(
            productId = "PROD001",
            productName = "노트북",
            category = Category("전자제품", "컴퓨터")
        )

        // When
        val result = DiffComparisonManager.calculateDifference(product, product)

        // Then
        then(result).isEmpty()
    }

    @Test
    fun `calculateDifference - 중첩된 객체의 변경 사항을 감지한다`() {
        // Given
        val originalProduct = Product(
            productId = "PROD001",
            productName = "노트북",
            category = Category("전자제품", "컴퓨터")
        )
        val newProduct = Product(
            productId = "PROD001",
            productName = "노트북",
            category = Category("전자제품", "노트북")
        )

        // When
        val result = DiffComparisonManager.calculateDifference(originalProduct, newProduct)

        // Then
        then(result).hasSize(1)
        then(result["category/sub_category"]).isNotNull
        then(result["category/sub_category"]?.origin).isEqualTo("컴퓨터")
        then(result["category/sub_category"]?.new).isEqualTo("노트북")
    }

    @Test
    fun `calculateDifference - 여러 필드의 변경 사항을 감지한다`() {
        // Given
        val originalItem = Item(
            product = Product("PROD001", "노트북", Category("전자제품", "컴퓨터")),
            quantity = 2,
            price = 1500000
        )
        val newItem = Item(
            product = Product("PROD001", "울트라 노트북", Category("전자제품", "컴퓨터")),
            quantity = 3,
            price = 1400000
        )

        // When
        val result = DiffComparisonManager.calculateDifference(originalItem, newItem)

        // Then
        then(result).hasSize(3)
        then(result["product/product_name"]).isNotNull
        then(result["product/product_name"]?.origin).isEqualTo("노트북")
        then(result["product/product_name"]?.new).isEqualTo("울트라 노트북")
        then(result["quantity"]).isNotNull
        then(result["quantity"]?.origin).isEqualTo("2")
        then(result["quantity"]?.new).isEqualTo("3")
        then(result["price"]).isNotNull
        then(result["price"]?.origin).isEqualTo("1500000")
        then(result["price"]?.new).isEqualTo("1400000")
    }

    @Test
    fun `calculateDifference - 깊게 중첩된 객체의 변경 사항을 감지한다`() {
        // Given
        val originalOrder = Order(
            orderId = "ORD123",
            customer = Customer(
                customerId = "CUST001",
                name = "홍길동",
                contact = Contact(
                    email = "hong@example.com",
                    phone = "010-1234-5678",
                    address = Address("서울특별시 종로구", "서울", "03001", "대한민국")
                )
            ),
            items = emptyList(),
            payment = Payment("신용카드", "TXN001", "완료")
        )
        val newOrder = Order(
            orderId = "ORD123",
            customer = Customer(
                customerId = "CUST001",
                name = "홍길동",
                contact = Contact(
                    email = "hong@example.com",
                    phone = "010-1234-5678",
                    address = Address("서울특별시 강남구", "서울", "06001", "대한민국")
                )
            ),
            items = emptyList(),
            payment = Payment("신용카드", "TXN001", "완료")
        )

        // When
        val result = DiffComparisonManager.calculateDifference(originalOrder, newOrder)

        // Then
        then(result).hasSize(2)
        then(result["customer/contact/address/street"]).isNotNull
        then(result["customer/contact/address/street"]?.origin).isEqualTo("서울특별시 종로구")
        then(result["customer/contact/address/street"]?.new).isEqualTo("서울특별시 강남구")
        then(result["customer/contact/address/zip_code"]).isNotNull
        then(result["customer/contact/address/zip_code"]?.origin).isEqualTo("03001")
        then(result["customer/contact/address/zip_code"]?.new).isEqualTo("06001")
    }

    @Test
    fun `calculateDifference - null에서 값으로 변경을 감지한다`() {
        // Given
        data class TestData(val name: String, val description: String?)
        val original = TestData("테스트", null)
        val new = TestData("테스트", "설명 추가")

        // When
        val result = DiffComparisonManager.calculateDifference(original, new)

        // Then
        then(result).hasSize(1)
        then(result["description"]).isNotNull
        then(result["description"]?.origin).isEmpty()
        then(result["description"]?.new).isEqualTo("설명 추가")
    }

    @Test
    fun `calculateDifference - 값에서 null로 변경을 감지한다`() {
        // Given
        data class TestData(val name: String, val description: String?)
        val original = TestData("테스트", "기존 설명")
        val new = TestData("테스트", null)

        // When
        val result = DiffComparisonManager.calculateDifference(original, new)

        // Then
        then(result).hasSize(1)
        then(result["description"]).isNotNull
        then(result["description"]?.origin).isEqualTo("기존 설명")
        then(result["description"]?.new).isEmpty()
    }

    @Test
    fun `calculateDifference - 모든 필드가 변경된 경우를 감지한다`() {
        // Given
        val originalCategory = Category("전자제품", "컴퓨터")
        val newCategory = Category("가전제품", "노트북")

        // When
        val result = DiffComparisonManager.calculateDifference(originalCategory, newCategory)

        // Then
        then(result).hasSize(2)
        then(result["main_category"]).isNotNull
        then(result["main_category"]?.origin).isEqualTo("전자제품")
        then(result["main_category"]?.new).isEqualTo("가전제품")
        then(result["sub_category"]).isNotNull
        then(result["sub_category"]?.origin).isEqualTo("컴퓨터")
        then(result["sub_category"]?.new).isEqualTo("노트북")
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