package com.spring.camp.io

import java.math.BigDecimal
import java.time.LocalDate

object IoFixture {

    fun productChangeNotificationRequest(

    ): ProductChangeNotificationRequest {
        return ProductChangeNotificationRequest(
            effectiveStartDate = LocalDate.of(2021, 1, 1),
            effectiveEndDate = LocalDate.of(2021, 12, 31),
            productId = 1,
            productName = "커피",
            category = "음료",
            brand = "스타벅스",
            price = BigDecimal("5000"),
            discountRate = BigDecimal("0.1"),
            currency = "KRW",
            region = "KR",
            cost = BigDecimal("3000"),
            supplier = "커피농장",
            mailSubject = "제품 변경 알림",
            mailBody = "제품 정보가 변경되었습니다.",
            recipientEmail = ""
        )
    }


}

data class ProductChangeNotificationRequest(
    // ProductHistory와 겹치는 필드 12개
    val effectiveStartDate: LocalDate,  // 변경 이력의 시작일
    val effectiveEndDate: LocalDate,    // 변경 이력의 종료일
    val productId: Long,                // 제품 식별자
    val productName: String,            // 제품 이름
    val category: String,               // 제품 카테고리
    val brand: String,                  // 브랜드 정보
    val price: BigDecimal,              // 가격
    val discountRate: BigDecimal?,      // 할인율
    val currency: String,               // 통화
    val region: String,                 // 적용 지역
    val cost: BigDecimal?,              // 원가
    val supplier: String?,              // 공급업체 정보

    // 추가 필드 3개: 알림 메일 전송에 필요한 정보
    val mailSubject: String,            // 메일 제목
    val mailBody: String,               // 메일 본문
    val recipientEmail: String          // 수신자 이메일 주소
)
