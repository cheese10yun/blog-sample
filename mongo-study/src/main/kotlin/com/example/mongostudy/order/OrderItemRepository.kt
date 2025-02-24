package com.example.mongostudy.order

import com.example.mongostudy.mongo.MongoCustomRepositorySupport
import com.example.mongostudy.mongo.UpdateWithArrayFilters
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
    fun updateItems2(forms: List<OrderItemQueryForm.UpdateItem>)
}

class OrderItemCustomRepositoryImpl(mongoTemplate: MongoTemplate) : OrderItemCustomRepository, MongoCustomRepositorySupport<OrderItem>(
    OrderItem::class.java,
    mongoTemplate
) {

    override fun updateItems(forms: List<OrderItemQueryForm.UpdateItem>) {
        // 업데이트 실행 (컬렉션명 "yourCollection"은 실제 컬렉션명으로 변경)
        bulkUpdate(
            forms.map { form ->
                Pair(
                    first = { Query(Criteria.where("_id").`is`(form.orderItem)) },
                    second = {
                        val update = Update()
                        form.items.forEachIndexed { index, item ->
                            update
                                .set("items.\$[elem${index}].price", item.price)
                                .filterArray("elem${index}.name", item.name)
                        }
                        update
                    }
                )
            }
        )
    }

    override fun updateItems2(forms: List<OrderItemQueryForm.UpdateItem>) {
        bulkUpdateDefinition(
            forms.map { form ->
                Pair(
                    first = { Query(Criteria.where("_id").`is`(form.orderItem)) },
                    second = {
                        val update = Update()
                        val arrayFilters = mutableListOf<Document>()

                        // 각 항목마다 자리표현자(elem0, elem1, …)를 생성하여 업데이트 및 조건 Document 구성
                        form.items.forEachIndexed { index, item ->
                            // 예: "items.$[elem0].price": item.price
                            update.set("items.\$[elem$index].price", item.price)
                            // 원하는 조건 Document: { "elem0.name": "item1", "elem0.category": "신발" }
                            arrayFilters.add(
                                Document("elem${index}.name", item.name)
                                    .append("elem${index}.category", item.category)
                            )
                        }

                        // 커스텀 UpdateDefinition 생성
                        val customUpdate = UpdateWithArrayFilters(update, arrayFilters.toList())
                        customUpdate
                    }
                )
            }
        )
    }
}