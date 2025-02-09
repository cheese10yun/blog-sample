package com.spring.camp.domain

object DomainIoFixture {

    fun productChangeNotificationRequest(
        mailSubject: String = "제품 변경 알림",
        mailBody: String = "제품 정보가 변경되었습니다.",
        recipientEmail: String = "test@test.com",
        productHistory: ProductHistory
    ): ProductChangeNotificationRequest {
        return ProductChangeNotificationRequest(
            effectiveStartDate = productHistory.effectiveStartDate,
            effectiveEndDate = productHistory.effectiveEndDate,
            productId = productHistory.productId,
            productName = productHistory.productName,
            category = productHistory.category,
            brand = productHistory.brand,
            price = productHistory.price,
            discountRate = productHistory.discountRate,
            currency = productHistory.currency,
            region = productHistory.region,
            cost = productHistory.cost,
            supplier = productHistory.supplier,
            mailSubject = mailSubject,
            mailBody = mailBody,
            recipientEmail = recipientEmail,
        )
    }
}