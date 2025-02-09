package com.spring.camp.domain

import com.spring.camp.io.EmailClient
import com.spring.camp.io.PartnerClient
import java.math.BigDecimal
import java.time.LocalDate
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor

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
                    recipientEmail = "sample@sample.test"
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

    @Test
    fun `기존 히스토리종료 신규 히스토리생성 정상동작 테스트   `() {
        // given
        val persist = persist(DomainFixture.product())

        val productHistory = DomainFixture.productHistory(
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
    fun `product, productHistory 연관 테스트`() {
        // given
        val product = persist(DomainFixture.product()) // product 영속화
        val productHistory = DomainFixture.productHistory(
            productId = product.id!!, // product 연결
            effectiveStartDate = LocalDate.of(2024, 1, 1),
            effectiveEndDate = LocalDate.of(2025, 1, 1),
        )
        persist(productHistory) // DB에 저장

        // 기타 테스트 코드...
    }

    @Test
    fun `기존 히스토리종료 신규 히스토리생성 정상동작 테스트    `() {
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
                    recipientEmail = "sample@sample.test"
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

    @Test
    fun `기존 히스토리종료 신규 히스토리생성 정상동작 테스트      `() {
        // given
        val productHistory = DomainFixture.productHistory(
            effectiveStartDate = LocalDate.of(2024, 1, 1),
            effectiveEndDate = LocalDate.of(2025, 1, 1),
        )
        persist(productHistory) // DB에 저장
        val request = DomainIoFixture.productChangeNotificationRequest(
            productHistory = productHistory,
            mailSubject = "Price Change Notification ...",
            mailBody = "Price of the product has been changed ...",
            recipientEmail = "sample@sample.test"
        )
        given(emailSender.sendProductChangeNotificationEmail(request)).willReturn(true)

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

    fun <T> persist(entity: T): T {

        return entity
    }

}

class ProductReservationService {


    fun renewReservationRecord(id: Long, effectiveStartDate: LocalDate, effectiveEndDate: LocalDate) {


    }

}

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ProductReservationServiceTest(
    private val productReservationService: ProductReservationService,
    private val mockEmailClient: EmailClient
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

    private fun persist(entity: ProductHistory): ProductHistory {
        return entity
    }

}


//object FixturesDomain {
//
//    fun productHistory(
//        effectiveStartDate: LocalDate,
//        effectiveEndDate: LocalDate,
//    ): ProductHistory {
//        return ProductHistory(
//            effectiveStartDate = effectiveStartDate,
//            effectiveEndDate = effectiveEndDate,
//            productId = 1L,
//            productName = "Sample Product",
//            category = "Electronics",
//            brand = "Acme",
//            price = 1_000.toBigDecimal(),
//            discountRate = 0.toBigDecimal(),
//            currency = "KRW",
//            isActive = false,
//            region = "North America",
//            description = "Initial price record for Sample Product",
//            cost = BigDecimal("250.00"),
//            supplier = "Acme Supplier",
//            taxRate = BigDecimal("0.08"),
//            unit = "piece",
//            additionalFee = 10.toBigDecimal(),
//        )
//    }
//}

