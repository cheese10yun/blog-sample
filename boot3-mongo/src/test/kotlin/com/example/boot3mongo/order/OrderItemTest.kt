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
                        name = "나이키 에어 포스",
                        category = "신발",
                        price = 100.00.toBigDecimal(),
                    ),
                    Item(
                        name = "나이키 후드",
                        category = "상의",
                        price = 200.00.toBigDecimal(),
                    ),
                    Item(
                        name = "나이키 반바지",
                        category = "하의",
                        price = 300.00.toBigDecimal(),
                    )
                )
            )
        )

        val forms = listOf(
            OrderItemQueryForm.UpdateItem(
                orderItem = orderItem.id!!,
                items = listOf(
                    OrderItemQueryForm.UpdateItemForm(
                        name = "나이키 에어 포스",
                        category = "신발",
                        price = 4000.00.toBigDecimal(),
                    ),
                    OrderItemQueryForm.UpdateItemForm(
                        name = "나이키 후드",
                        category = "상의",
                        price = 5000.00.toBigDecimal(),
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
                "나이키 에어 포스" -> then(item.price).isEqualByComparingTo(4000.00.toBigDecimal())
                "나이키 후드" -> then(item.price).isEqualByComparingTo(5000.00.toBigDecimal())
                "나이키 반바지" -> then(item.price).isEqualByComparingTo(300.00.toBigDecimal())
                else -> throw IllegalStateException("검증 되지 않은 값이 들어왔습니다. 신규 데이터 or 로직 변경에 따른 테스트 코드를 보강해주세요")
            }
        }
    }


    @Test
    fun `updateItems3`() {
        // given
        val orderItem = mongoTemplate.insert(
            OrderItem(
                items = listOf(
                    Item(
                        name = "나이키 에어 포스",
                        category = "신발",
                        price = 100.00.toBigDecimal(),
                    ),
                    Item(
                        name = "나이키 후드",
                        category = "상의",
                        price = 200.00.toBigDecimal(),
                    ),
                    Item(
                        name = "나이키 반바지",
                        category = "하의",
                        price = 300.00.toBigDecimal(),
                    )
                )
            )
        )

        val form = OrderItemQueryForm.UpdateItem(
            orderItem = orderItem.id!!,
            items = listOf(
                OrderItemQueryForm.UpdateItemForm(
                    name = "나이키 에어 포스",
                    category = "신발",
                    price = 4000.00.toBigDecimal(),
                ),
                OrderItemQueryForm.UpdateItemForm(
                    name = "나이키 후드",
                    category = "상의",
                    price = 5000.00.toBigDecimal(),
                )
            )
        )

        // when
        orderItemRepository.updateItems3(form)


        // then
        val result = mongoTemplate.findOne<OrderItem>(Query(Criteria.where("_id").`is`(orderItem.id)))!!
        then(result.items).allSatisfy { item ->
            when (item.name) {
                "나이키 에어 포스" -> then(item.price).isEqualByComparingTo(4000.00.toBigDecimal())
                "나이키 후드" -> then(item.price).isEqualByComparingTo(5000.00.toBigDecimal())
                "나이키 반바지" -> then(item.price).isEqualByComparingTo(300.00.toBigDecimal())
                else -> throw IllegalStateException("검증 되지 않은 값이 들어왔습니다. 신규 데이터 or 로직 변경에 따른 테스트 코드를 보강해주세요")
            }
        }
    }

    @Test
    fun `updateItems4`() {
        // given
        val orderItem = mongoTemplate.insert(
            OrderItem(
                items = listOf(
                    Item(
                        name = "나이키 에어 포스",
                        category = "신발",
                        price = 100.00.toBigDecimal(),
                    ),
                    Item(
                        name = "나이키 후드",
                        category = "상의",
                        price = 200.00.toBigDecimal(),
                    ),
                    Item(
                        name = "나이키 반바지",
                        category = "하의",
                        price = 300.00.toBigDecimal(),
                    )
                )
            )
        )

        val form = OrderItemQueryForm.UpdateItem(
            orderItem = orderItem.id!!,
            items = listOf(
                OrderItemQueryForm.UpdateItemForm(
                    name = "나이키 에어 포스",
                    category = "신발",
                    price = 4000.00.toBigDecimal(),
                ),
                OrderItemQueryForm.UpdateItemForm(
                    name = "나이키 후드",
                    category = "상의",
                    price = 5000.00.toBigDecimal(),
                )
            )
        )

        // when
        orderItemRepository.updateItems4(form)


        // then
        val result = mongoTemplate.findOne<OrderItem>(Query(Criteria.where("_id").`is`(orderItem.id)))!!
        then(result.items).allSatisfy { item ->
            when (item.name) {
                "나이키 에어 포스" -> then(item.price).isEqualByComparingTo(4000.00.toBigDecimal())
                "나이키 후드" -> then(item.price).isEqualByComparingTo(5000.00.toBigDecimal())
                "나이키 반바지" -> then(item.price).isEqualByComparingTo(300.00.toBigDecimal())
                else -> throw IllegalStateException("검증 되지 않은 값이 들어왔습니다. 신규 데이터 or 로직 변경에 따른 테스트 코드를 보강해주세요")
            }
        }
    }
}