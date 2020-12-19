package com.batch.study.domain.payment

import org.jetbrains.exposed.dao.id.LongIdTable

object PaymentBack : LongIdTable(name = "payment_back") {
    val amount = decimal("amount", 19, 4)
    val orderId = long("order_id")
}