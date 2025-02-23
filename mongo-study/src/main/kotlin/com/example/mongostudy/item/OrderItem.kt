package com.example.mongostudy.item

import com.example.mongostudy.mongo.Auditable
import com.example.mongostudy.mongo.MongoCustomRepositorySupport
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
    fun updateItems(forms: List<OrderItemQueryForm.UpdateItem>)
}

class OrderItemCustomRepositoryImpl(mongoTemplate: MongoTemplate) : OrderItemCustomRepository, MongoCustomRepositorySupport<OrderItem>(
    OrderItem::class.java,
    mongoTemplate
) {

    override fun updateItems(forms: List<OrderItemQueryForm.UpdateItem>) {
        // 업데이트 실행 (컬렉션명 "yourCollection"은 실제 컬렉션명으로 변경)
        bulkUpdate(
            forms.map {
                Pair(
                    { Query(Criteria.where("_id").`is`(it.orderItem)) },
                    {
                        val update = Update()
                        forms.map {
                            it.items.forEachIndexed { index, item ->
                                update
                                    .set("items.\$[elem${index}].price", item.price)
                                    .filterArray("elem${index}.name", item.name)
                            }
                        }
                        update
                    }
                )
            }
        )
    }
}

object OrderItemQueryForm {
    data class UpdateItem(
        val orderItem: ObjectId,
        val items: List<UpdateItemForm>

    )

    data class UpdateItemForm(
        val name: String,
        val price: BigDecimal
    )
}