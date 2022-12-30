package com.example.effectivekotlin


import org.junit.jupiter.api.Test

class Collection {


    data class Member(
        val name: String,
        val status: MemberStatus
    )

    enum class MemberStatus(description: String) {
        NORMAL("정산"),
        VAN("정지")
    }

    data class Order(
        val orderNumber: String,
        val status: OrderStatus,
        val price: Int
    )


    data class OrderPrice(
        val orderStatus: OrderStatus,
        val price: Int
    )

    @Test
    fun groupBy() {
        val members = (1..50).map {
            Member(
                name = "name${it}",
                status = when {
                    (0..1).random() == 0 -> MemberStatus.NORMAL
                    else -> MemberStatus.VAN
                }
            )
        }

        val groupBy = members.groupBy { it.status }
        val vanMembers = groupBy[MemberStatus.VAN]
        val normalMembers = groupBy[MemberStatus.NORMAL]
        val groupBy1 = members.groupBy(
            { it.status },
            { it.name }
        )

    }

    @Test
    fun groupingBy() {
        val orders = (1..10).map {
            Order(
                orderNumber = "name${it}",
                status = when {
                    (0..1).random() == 0 -> OrderStatus.COMPLETE_ORDER
                    else -> OrderStatus.COMPLETE_PAYMENT
                },
                price = (100..15000).random()
            )
        }

        // (1)
        val groupingBy = orders
            .groupingBy { it.status }

        // (2)
        val aggregate = groupingBy
            .aggregate { key, accumulator: OrderPrice?, element, first ->
                when {
                    first -> OrderPrice(orderStatus = key, price = element.price)
                    else -> OrderPrice(orderStatus = key, price = accumulator!!.price + element.price)
                }
            }

        val totalPrice1 = aggregate[OrderStatus.COMPLETE_ORDER]
        val totalPrice2 = aggregate[OrderStatus.COMPLETE_PAYMENT]

        // (3)
        val eachCount = groupingBy.eachCount()
        val count1 = eachCount[OrderStatus.COMPLETE_ORDER]
        val count2 = eachCount[OrderStatus.COMPLETE_PAYMENT]

        println("")
    }


    @Test
    fun chunked() {
        val orders = (1..100).map {
            Order(
                orderNumber = "name${it}",
                status = when {
                    (0..1).random() == 0 -> OrderStatus.COMPLETE_ORDER
                    else -> OrderStatus.COMPLETE_PAYMENT
                },
                price = (100..15000).random()
            )
        }

        val chunked = orders.chunked(10)
    }


    data class OrderNumbers(
        val orderStatus: OrderStatus,
        val orderNumbers: Set<String>
    )

    @Test
    fun flatMap() {
        val orderNumbers = listOf(
            OrderNumbers(
                orderStatus = OrderStatus.COMPLETE_ORDER,
                orderNumbers = setOf("order-number-1", "order-number-2", " order-number-3", "order-number-3"),
            ),
            OrderNumbers(
                orderStatus = OrderStatus.COMPLETE_ORDER,
                orderNumbers = setOf("order-number-4", "order-number-5", " order-number-6", "order-number-7"),
            ),
            OrderNumbers(
                orderStatus = OrderStatus.COMPLETE_PAYMENT,
                orderNumbers = setOf("order-number-8", "order-number-9", " order-number-10", "order-number-11"),
            ),
            OrderNumbers(
                orderStatus = OrderStatus.COMPLETE_PAYMENT,
                orderNumbers = setOf("order-number-11", "order-number-12", " order-number-13", "order-number-14"),
            )
        )

        val flatMap = orderNumbers.flatMap { it.orderNumbers }


//        val reduce = orderNumbers
//            .groupingBy { it.orderStatus }
//            .reduce { key, accumulator, element ->
//                OrderNumbers(
//                    orderStatus = key,
//                    orderNumbers = accumulator.orderNumbers + element.orderNumbers
//                )
//            }
//            .map { it.value }

        println("")
    }
}

enum class OrderStatus(description: String) {
    COMPLETE_ORDER("주문 완료"),
    COMPLETE_PAYMENT("결제 완료"),
}