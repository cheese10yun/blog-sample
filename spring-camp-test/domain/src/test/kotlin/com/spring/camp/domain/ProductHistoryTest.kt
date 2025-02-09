package com.spring.camp.domain

import java.math.BigDecimal
import java.time.LocalDate
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given

class ProductHistoryTest(
    private val productReservationService: ProductReservationService,
    private val emailSender: EmailSender
) {

    @Test
    fun `기존 히스토리종료 신규 히스토리생성 정상동작 테스트`() {
        // given
        val productHistory = ProductHistory(
            effectiveStartDate = LocalDate.of(2024, 1, 1),
            effectiveEndDate = LocalDate.of(2025, 1, 1),
            productId = 1L,
            productName = "Sample Product",
            category = "Electronics",
            brand = "Acme",
            price = 1_000.toBigDecimal(),
            discountRate = 0.toBigDecimal(),
            currency = "KRW",
            isActive = false,
            region = "North America",
            description = "Initial price record for Sample Product",
            cost = BigDecimal("250.00"),
            supplier = "Acme Supplier",
            taxRate = BigDecimal("0.08"),
            unit = "piece",
            additionalFee = 10.toBigDecimal(),
        )

        persist(productHistory) // DB에 저장

        // when
        // 기존 히스토리를 종료 시키고, 새로운 히스토리를 생성한다.
        productReservationService.renewReservationRecord(
            id = productHistory.id!!,
            effectiveStartDate = LocalDate.of(2024, 5, 1),
            effectiveEndDate = LocalDate.of(2025, 1, 1),
        )

        // then
        // DB에서 조회하여, 기존 히스토리 종료 검증
        // DB에서 조회하여, 새로운 히스토리 생성 검증
    }

    @Test
    fun `기존 히스토리종료 신규 히스토리생성 정상동작 테스트 `() {
        // given
        val productHistory = givenProductHistory(
            effectiveStartDate = LocalDate.of(2024, 1, 1),
            effectiveEndDate = LocalDate.of(2025, 1, 1),
        )
        persist(productHistory) // DB에 저장


        // when
        // 기존 히스토리를 종료 시키고, 새로운 히스토리를 생성한다.
        productReservationService.renewReservationRecord(
            id = productHistory.id!!,
            effectiveStartDate = LocalDate.of(2024, 5, 1),
            effectiveEndDate = LocalDate.of(2025, 1, 1),
        )

        // then
        // DB에서 조회하여, 기존 히스토리 종료 검증
        // DB에서 조회하여, 새로운 히스토리 생성 검증
    }

    @Test
    fun `기존 히스토리종료 신규 히스토리생성 정상동작 테스트  `() {
        // given
        val productHistory = givenProductHistory(
            effectiveStartDate = LocalDate.of(2024, 1, 1),
            effectiveEndDate = LocalDate.of(2025, 1, 1),
        )
        persist(productHistory) // DB에 저장

        given(
            emailSender.sendProductChangeNotificationEmail(
                ProductChangeNotificationRequest(
                    effectiveStartDate = LocalDate.of(2024, 5, 1),
                    effectiveEndDate = LocalDate.of(2025, 1, 1),
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
                    mailSubject = "Price Change Notification ...",
                    mailBody = "Price of the product has been changed ...",
                    recipientEmail = ""
                )
            )
        ).willReturn(true)

        // when
        // 기존 히스토리를 종료 시키고, 새로운 히스토리를 생성한다.
        productReservationService.renewReservationRecord(
            id = productHistory.id!!,
            effectiveStartDate = LocalDate.of(2024, 5, 1),
            effectiveEndDate = LocalDate.of(2025, 1, 1),
        )

        // then
        // DB에서 조회하여, 기존 히스토리 종료 검증
        // DB에서 조회하여, 새로운 히스토리 생성 검증
    }

    private fun givenProductHistory(
        effectiveStartDate: LocalDate,
        effectiveEndDate: LocalDate,
    ): ProductHistory {
        return ProductHistory(
            effectiveStartDate = effectiveStartDate,
            effectiveEndDate = effectiveEndDate,
            productId = 1L,
            productName = "Sample Product",
            category = "Electronics",
            brand = "Acme",
            price = 1_000.toBigDecimal(),
            discountRate = 0.toBigDecimal(),
            currency = "KRW",
            isActive = false,
            region = "North America",
            description = "Initial price record for Sample Product",
            cost = BigDecimal("250.00"),
            supplier = "Acme Supplier",
            taxRate = BigDecimal("0.08"),
            unit = "piece",
            additionalFee = 10.toBigDecimal(),
        )
    }

    fun persist(productHistory: ProductHistory) {

    }

}

class ProductReservationService {


    fun renewReservationRecord(id: Long, effectiveStartDate: LocalDate, effectiveEndDate: LocalDate) {


    }

}

object FixturesDomain {

    fun productHistory(
        effectiveStartDate: LocalDate,
        effectiveEndDate: LocalDate,
    ): ProductHistory {
        return ProductHistory(
            effectiveStartDate = effectiveStartDate,
            effectiveEndDate = effectiveEndDate,
            productId = 1L,
            productName = "Sample Product",
            category = "Electronics",
            brand = "Acme",
            price = 1_000.toBigDecimal(),
            discountRate = 0.toBigDecimal(),
            currency = "KRW",
            isActive = false,
            region = "North America",
            description = "Initial price record for Sample Product",
            cost = BigDecimal("250.00"),
            supplier = "Acme Supplier",
            taxRate = BigDecimal("0.08"),
            unit = "piece",
            additionalFee = 10.toBigDecimal(),
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


class EmailSender {

    fun sendProductChangeNotificationEmail(request: ProductChangeNotificationRequest): Boolean {
        // 이메일 전송

        return true
    }
}