package com.example.boot3mongo.order

import com.example.boot3mongo.MongoCustomRepositorySupport
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.UpdateDefinition
import org.springframework.data.mongodb.repository.MongoRepository

interface OrderItemRepository : MongoRepository<OrderItem, ObjectId>, OrderItemCustomRepository

interface OrderItemCustomRepository {
    fun updateItems(forms: List<OrderItemQueryForm.UpdateItem>)
    fun updateItems2(form: OrderItemQueryForm.UpdateItem)
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
                                .filterArray(
                                    "elem${index}.name",
                                    Document("elem${index}.name", item.name).append("elem${index}.category", item.category)
                                )
                        }
                        update
                    }
                )
            }
        )
    }

    fun updateItems(form: OrderItemQueryForm.UpdateItem) {
        // 업데이트 실행 (컬렉션명 "yourCollection"은 실제 컬렉션명으로 변경)
        val query = Query(Criteria.where("_id").`is`(form.orderItem))
        val update = Update()
        form.items.forEachIndexed { index, item ->
            update
                .set("items.\$[elem${index}].price", item.price)
                .filterArray("elem${index}.name", item.name)
        }
        mongoTemplate.updateFirst(query, update, documentClass)
    }

    override fun updateItems2(form: OrderItemQueryForm.UpdateItem) {
        // _id 기준으로 업데이트할 도큐먼트를 선택
        val query = Query(Criteria.where("_id").`is`(form.orderItem))

        // 기본 Update 객체와 arrayFilters 리스트 생성
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

        // 업데이트 실행 (예: 컬렉션명 또는 documentClass에 맞게)
        mongoTemplate.updateFirst(query, customUpdate, documentClass)


    }
}

class UpdateWithArrayFilters(
    private val update: Update,
    private val arrayFilters: List<Document>
) : UpdateDefinition {
    override fun isIsolated(): Boolean = update.isIsolated

    override fun getUpdateObject(): Document {
        val updateObject = update.updateObject
        updateObject["arrayFilters"] = arrayFilters
        return updateObject
    }

    override fun modifies(key: String): Boolean = update.modifies(key)

    override fun inc(key: String) {
        update.inc(key)
    }

    // UpdateDefinition.ArrayFilter는 단순 람다로 구현할 수 있음
    override fun getArrayFilters(): List<UpdateDefinition.ArrayFilter> {
        return arrayFilters
            .map { doc -> UpdateDefinition.ArrayFilter { doc } }
            .toList()
    }
}