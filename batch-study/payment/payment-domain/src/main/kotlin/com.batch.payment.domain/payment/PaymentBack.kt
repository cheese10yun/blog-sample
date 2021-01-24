package com.batch.payment.domain.payment

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object PaymentBack : LongIdTable(name = "payment_back") {
    val amount = decimal("amount", 19, 4)
    val orderId = long("order_id")
}

class PaymentBackDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<PaymentBackDao>(PaymentBack)

    var amount by PaymentBack.amount
    var orderId by PaymentBack.orderId
}