package com.example.boot3mongo.order

import com.example.boot3mongo.MongoCustomRepositorySupport
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
}