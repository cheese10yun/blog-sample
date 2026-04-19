package com.example.querydsl.repository.payment

import com.example.querydsl.SpringBootTestSupport
import com.example.querydsl.domain.Payment
import com.example.querydsl.repository.support.CursorDirection
import com.example.querydsl.repository.support.CursorRequest
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PaymentNewRepositoryImplTest(
    private val paymentNewRepositoryImpl: Payment4RepositoryImpl,
    private val paymentRepository: PaymentRepository,
) : SpringBootTestSupport() {

    private lateinit var payments: List<Payment>

    /**
     * 각 테스트 전 결제 데이터 10건을 초기화한다.
     * amount 1~10, ID는 자동 증가(ASC) 순서로 생성된다.
     */
    @BeforeEach
    fun setUp() {
        paymentRepository.deleteAll()
        payments = (1..10).map { save(Payment(it.toBigDecimal())) }
    }

    // ===== FIRST =====

    @Test
    fun `FIRST - DESC 정렬로 가장 최근 데이터 2건을 반환한다`() {
        val result = paymentNewRepositoryImpl.findByCursor(
            CursorRequest(cursorKey = null, direction = CursorDirection.FIRST, pageSize = 2)
        )

        // DESC 이므로 ID 높은 순 반환
        then(result.content).hasSize(2)
        then(result.content.map { it.id }).containsExactly(payments[9].id, payments[8].id)
        then(result.hasNext).isTrue()
        then(result.hasPrev).isFalse()
        then(result.nextCursor).isEqualTo(payments[8].id.toString())
        then(result.prevCursor).isNull()
    }

    @Test
    fun `FIRST - 전체 데이터가 pageSize 이하이면 hasNext가 false이다`() {
        val result = paymentNewRepositoryImpl.findByCursor(
            CursorRequest(cursorKey = null, direction = CursorDirection.FIRST, pageSize = 20)
        )

        then(result.content).hasSize(10)
        then(result.hasNext).isFalse()
        then(result.hasPrev).isFalse()
        then(result.nextCursor).isNull()
        then(result.prevCursor).isNull()
    }

    // ===== NEXT =====

    @Test
    fun `NEXT - 커서 이후 다음 페이지를 반환한다`() {
        // FIRST 결과의 nextCursor(= payments[8].id) 를 커서로 사용
        val cursor = payments[8].id.toString() // id9 (0-indexed: payments[8] = 9번째)

        val result = paymentNewRepositoryImpl.findByCursor(
            CursorRequest(cursorKey = cursor, direction = CursorDirection.NEXT, pageSize = 2)
        )

        // id < cursor(9) 이면서 DESC → id8, id7
        then(result.content).hasSize(2)
        then(result.content.map { it.id }).containsExactly(payments[7].id, payments[6].id)
        then(result.hasNext).isTrue()
        then(result.hasPrev).isTrue()
        then(result.nextCursor).isEqualTo(payments[6].id.toString())
        then(result.prevCursor).isEqualTo(payments[7].id.toString())
    }

    @Test
    fun `NEXT - 마지막 페이지에서는 hasNext가 false이고 nextCursor가 null이다`() {
        // id3 커서 → id < id3 → id2, id1 만 남음 (pageSize=2 딱 맞음)
        val cursor = payments[2].id.toString()

        val result = paymentNewRepositoryImpl.findByCursor(
            CursorRequest(cursorKey = cursor, direction = CursorDirection.NEXT, pageSize = 2)
        )

        then(result.content).hasSize(2)
        then(result.content.map { it.id }).containsExactly(payments[1].id, payments[0].id)
        then(result.hasNext).isFalse()
        then(result.hasPrev).isTrue()
        then(result.nextCursor).isNull()
        then(result.prevCursor).isEqualTo(payments[1].id.toString())
    }

    @Test
    fun `NEXT - 커서 이후 데이터가 없으면 빈 결과를 반환한다`() {
        // 가장 낮은 id 커서 → 이후 데이터 없음
        val cursor = payments[0].id.toString()

        val result = paymentNewRepositoryImpl.findByCursor(
            CursorRequest(cursorKey = cursor, direction = CursorDirection.NEXT, pageSize = 2)
        )

        then(result.content).isEmpty()
        then(result.hasNext).isFalse()
        then(result.hasPrev).isFalse()
        then(result.nextCursor).isNull()
        then(result.prevCursor).isNull()
    }

    // ===== PREV =====

    @Test
    fun `PREV - 커서보다 큰 ID를 ASC 조회 후 역순으로 반환한다`() {
        // cursor = id8 → id > id8 → [id9, id10] ASC → reversed → [id10, id9]
        val cursor = payments[7].id.toString()

        val result = paymentNewRepositoryImpl.findByCursor(
            CursorRequest(cursorKey = cursor, direction = CursorDirection.PREV, pageSize = 2)
        )

        then(result.content).hasSize(2)
        then(result.content.map { it.id }).containsExactly(payments[9].id, payments[8].id)
        then(result.hasNext).isTrue()
        then(result.hasPrev).isFalse()
        then(result.nextCursor).isEqualTo(payments[8].id.toString())
        then(result.prevCursor).isNull()
    }

    @Test
    fun `PREV - 이전 데이터가 더 있으면 hasPrev가 true이다`() {
        // cursor = id5 → id > id5 → [id6,id7,id8] ASC → take(2)=[id6,id7] → reversed → [id7,id6]
        val cursor = payments[4].id.toString()

        val result = paymentNewRepositoryImpl.findByCursor(
            CursorRequest(cursorKey = cursor, direction = CursorDirection.PREV, pageSize = 2)
        )

        then(result.content).hasSize(2)
        then(result.content.map { it.id }).containsExactly(payments[6].id, payments[5].id)
        then(result.hasNext).isTrue()
        then(result.hasPrev).isTrue()
        then(result.prevCursor).isEqualTo(payments[5].id.toString())
    }

    // ===== LAST =====

    @Test
    fun `LAST - ASC 조회 후 역순으로 가장 오래된 데이터를 반환한다`() {
        // ASC LIMIT 3 → [id1,id2,id3] → take(2)=[id1,id2] → reversed → [id2,id1]
        val result = paymentNewRepositoryImpl.findByCursor(
            CursorRequest(cursorKey = null, direction = CursorDirection.LAST, pageSize = 2)
        )

        then(result.content).hasSize(2)
        then(result.content.map { it.id }).containsExactly(payments[1].id, payments[0].id)
        then(result.hasNext).isFalse()
        then(result.hasPrev).isTrue()
        then(result.nextCursor).isNull()
        then(result.prevCursor).isEqualTo(payments[1].id.toString())
    }

    @Test
    fun `LAST - 전체 데이터가 pageSize 이하이면 hasPrev가 false이다`() {
        val result = paymentNewRepositoryImpl.findByCursor(
            CursorRequest(cursorKey = null, direction = CursorDirection.LAST, pageSize = 20)
        )

        then(result.content).hasSize(10)
        then(result.hasNext).isFalse()
        then(result.hasPrev).isFalse()
        then(result.nextCursor).isNull()
        then(result.prevCursor).isNull()
    }

    // ===== Empty =====

    @Test
    fun `데이터가 없으면 모든 플래그가 false이고 커서가 null이다`() {
        paymentRepository.deleteAll()

        val result = paymentNewRepositoryImpl.findByCursor(
            CursorRequest(cursorKey = null, direction = CursorDirection.FIRST, pageSize = 2)
        )

        then(result.content).isEmpty()
        then(result.hasNext).isFalse()
        then(result.hasPrev).isFalse()
        then(result.nextCursor).isNull()
        then(result.prevCursor).isNull()
    }
}
