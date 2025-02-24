package com.example.boot3mongo.order

import com.example.boot3mongo.Boot3MongoApplicationTest
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

class OrderItemTest(
    private val orderItemRepository: OrderItemRepository
) : Boot3MongoApplicationTest() {

    @Test
    fun `updateItems`() {
        // given
        val orderItem = mongoTemplate.insert(
            OrderItem(
                items = listOf(
                    Item(
                        name = "item1",
                        category = "신발",
                        price = 100.01.toBigDecimal(),
                    ),
                    Item(
                        name = "item2",
                        category = "상의",
                        price = 100.02.toBigDecimal(),
                    )
                )
            )
        )

        val forms = listOf(
            OrderItemQueryForm.UpdateItem(
                orderItem = orderItem.id!!,
                items = listOf(
                    OrderItemQueryForm.UpdateItemForm(
                        name = "item1",
                        category = "신발",
                        price = 222.01.toBigDecimal(),
                    ),
                    OrderItemQueryForm.UpdateItemForm(
                        name = "item2",
                        category = "상의",
                        price = 333.02.toBigDecimal(),
                    )
                )
            )
        )
        // when
        orderItemRepository.updateItems2(forms)
//        orderItemRepository.updateItems(forms)

        // then
        val result = mongoTemplate.findOne<OrderItem>(Query(Criteria.where("_id").`is`(orderItem.id)))!!
        then(result.items).allSatisfy { item ->
            when (item.name) {
                "item1" -> then(item.price).isEqualByComparingTo(222.01.toBigDecimal())
                "item2" -> then(item.price).isEqualByComparingTo(333.02.toBigDecimal())
                else -> throw IllegalStateException("검증 되지 않은 값이 들어왔습니다. 신규 데이터 or 로직 변경에 따른 테스트 코드를 보강해주세요")
            }
        }
    }
}