package com.example.boot3mongo.order

import com.example.boot3mongo.MongoCustomRepositorySupport
import com.example.boot3mongo.mongo.Auditable
import java.math.BigDecimal
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.repository.MongoRepository

@Document(collection = "order_item")
class OrderItem(
    @Field(name = "items", targetType = FieldType.ARRAY)
    val items: List<Item> = emptyList()
) : Auditable()


data class Item(
    @Field(name = "name", targetType = FieldType.STRING)
    val name: String,

    @Field(name = "price")
//    @Field(name = "price", targetType = FieldType.DECIMAL128)
    val price: BigDecimal,
)

interface OrderItemRepository : MongoRepository<OrderItem, ObjectId>, OrderItemCustomRepository

interface OrderItemCustomRepository {
    fun updateItems(id: ObjectId)
}

class OrderItemCustomRepositoryImpl(mongoTemplate: MongoTemplate) : OrderItemCustomRepository, MongoCustomRepositorySupport<OrderItem>(
    OrderItem::class.java,
    mongoTemplate
) {

    override fun updateItems(id: ObjectId) {
        // _id로 업데이트할 문서 선택
        val query = Query(Criteria.where("_id").`is`(id))

        // Update 객체 생성 후 배열 요소의 price 값을 업데이트하고,
        // filterArray 메서드를 사용해 arrayFilters 조건을 추가
        val update = Update()
            .set("items.\$[elem1].price", 300)
            .set("items.\$[elem2].price", 400)
            .filterArray("elem1.name", "item1")
            .filterArray("elem2.name", "item2")

        // 업데이트 실행 (컬렉션명 "yourCollection"은 실제 컬렉션명으로 변경)
        mongoTemplate.updateFirst(query, update, documentClass)
    }
}