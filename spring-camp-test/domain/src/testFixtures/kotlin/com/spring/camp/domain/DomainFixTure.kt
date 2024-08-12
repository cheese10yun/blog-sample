package com.spring.camp.domain

import java.time.LocalDate

object DomainFixTure {

    fun order(
        s: String = "customerName",
        localDate: LocalDate? = LocalDate.now()
    ) {
        Order(
            customerName = s,
            orderDate = localDate,
            totalAmount = 8.9,
            status = "READY",
            shippingAddress = "sapien",
            billingAddress = "ridens",
            paymentMethod = "libero",
            shippingCost = 10.11,
            taxAmount = 12.13,
            discount = 14.15,
            notes = null
        )
    }

}