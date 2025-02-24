package com.example.mongostudy.item

import com.example.mongostudy.MongoStudyApplicationTests
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.findAll

class OrderItemTest(
    private val orderItemRepository: OrderItemRepository
) : MongoStudyApplicationTests() {

    @Test
    fun `updateItems`() {
        // given
        val orderItems = listOf(
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
        mongoTemplate.insertAll(orderItems)

        val forms = orderItems.map {
            OrderItemQueryForm.UpdateItem(
                orderItem = it.id!!,
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
        }
        // when

        orderItemRepository.updateItems(forms)

        // then
        val findAll = mongoTemplate.findAll<OrderItem>()
        println(findAll)
    }
}