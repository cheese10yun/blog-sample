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
) : Auditable() {

    override fun toString(): String {
        return "OrderItem(items=$items)"
    }
}


data class Item(
    @Field(name = "name", targetType = FieldType.STRING)
    val name: String,

//    @Field(name = "price")
    @Field(name = "price", targetType = FieldType.DECIMAL128)
    val price: BigDecimal,
)

interface OrderItemRepository : MongoRepository<OrderItem, ObjectId>, OrderItemCustomRepository

interface OrderItemCustomRepository {
    fun updateItems(ids: List<ObjectId>)
}

class OrderItemCustomRepositoryImpl(mongoTemplate: MongoTemplate) : OrderItemCustomRepository, MongoCustomRepositorySupport<OrderItem>(
    OrderItem::class.java,
    mongoTemplate
) {

    override fun updateItems(ids: List<ObjectId>) {


        // 업데이트 실행 (컬렉션명 "yourCollection"은 실제 컬렉션명으로 변경)

        bulkUpdate(
            ids.map {
                Pair(
                    { Query(Criteria.where("_id").`is`(it)) },
                    {
                        Update()
                            .set("items.\$[elem1].price", 123.toBigDecimal())
                            .set("items.\$[elem2].price", 456.toBigDecimal())
                            .filterArray("elem1.name", "item1")
                            .filterArray("elem2.name", "item2")
                    }
                )
            }
        )
    }
}