package com.example.mongostudy.item

import com.example.mongostudy.mongo.MongoCustomRepositorySupport
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.repository.MongoRepository

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
                        val arrayFilters = mutableListOf<Document>()
                        forms.map {
                            it.items.forEachIndexed { index, item ->
                                update
                                    .set("items.\$[elem${index}].price", item.price)
//                                    .filterArray("elem${index}.name", item.name)
//                                    .filterArray("elem${index}.category", item.category)
                                    .filterArray("elem$index", Document("name", item.name).append("category", item.category))
//                                    .filterArray(
//                                        "elem$index", Document("name", item.name).append("category", item.category)
//                                    )



                            }
                        }

                        update.arrayFilters(arrayFilters)
                        update.arrayFilters(arrayFilters)
                        update
                    }
                )
            }
        )
    }
}