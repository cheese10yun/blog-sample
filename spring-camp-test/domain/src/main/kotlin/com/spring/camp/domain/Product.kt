package com.spring.camp.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime


@Entity
@Table(name = "product")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "product_name")
    var productName: String,

    @Column(name = "category")
    var category: String,

    @Column(name = "brand")
    var brand: String,

    @Column(name = "price")
    var price: BigDecimal,

    @Column(name = "discount_rate")
    var discountRate: BigDecimal?,

    @Column(name = "currency")
    var currency: String,

    @Column(name = "is_active")
    var isActive: Boolean,

    @Column(name = "created_date")
    var createdDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_date")
    var updatedDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "region")
    var region: String,

    @Column(name = "description")
    var description: String?,

    @Column(name = "cost")
    var cost: BigDecimal,

    @Column(name = "supplier")
    var supplier: String?,

    @Column(name = "tax_rate")
    var taxRate: BigDecimal,

    @Column(name = "unit")
    var unit: String,

    @Column(name = "additional_fee")
    var additionalFee: BigDecimal
) {

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


class EmailSender {

    fun sendProductChangeNotificationEmail(request: ProductChangeNotificationRequest): Boolean {
        // 이메일 전송

        return true
    }
}