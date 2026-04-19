package com.example.querydsl.repository.support

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class CursorPageResponseTest {

    // ──────────────────────────────────────────────
    // 공통
    // ──────────────────────────────────────────────

    @Test
    fun `content가 비어있으면 모든 플래그 false, 커서 null`() {
        // given
        val emptyContent = emptyList<Int>()

        CursorDirection.entries.forEach { direction ->
            // when
            val response = CursorPageResponse(emptyContent, direction, pageSize = 10)

            // then
            then(response.content).isEmpty()
            then(response.hasNext).isFalse()
            then(response.hasPrev).isFalse()
            then(response.nextCursor).isNull()
            then(response.prevCursor).isNull()
        }
    }

    // ──────────────────────────────────────────────
    // FIRST: forward, hasPrev 항상 false
    // ──────────────────────────────────────────────

    @Test
    fun `FIRST - content가 pageSize와 같으면 hasNext false, 커서 모두 null`() {
        // given
        val content = listOf(1, 2, 3)
        val pageSize = 3

        // when
        val response = CursorPageResponse(content, CursorDirection.FIRST, pageSize)

        // then
        then(response.content).hasSize(3)
        then(response.hasNext).isFalse()
        then(response.hasPrev).isFalse()
        then(response.nextCursor).isNull()
        then(response.prevCursor).isNull()
    }

    @Test
    fun `FIRST - content가 pageSize 초과이면 hasNext true, nextCursor는 actualContent 마지막 항목`() {
        // given
        val content = listOf(1, 2, 3, 4) // pageSize+1
        val pageSize = 3

        // when
        val response = CursorPageResponse(content, CursorDirection.FIRST, pageSize)

        // then
        then(response.content).containsExactly(1, 2, 3)
        then(response.hasNext).isTrue()
        then(response.hasPrev).isFalse()
        then(response.nextCursor).isEqualTo(3)
        then(response.prevCursor).isNull()
    }

    @Test
    fun `FIRST - content 순서가 그대로 유지된다`() {
        // given
        val content = listOf(10, 20, 30)
        val pageSize = 3

        // when
        val response = CursorPageResponse(content, CursorDirection.FIRST, pageSize)

        // then
        then(response.content).containsExactly(10, 20, 30)
    }

    // ──────────────────────────────────────────────
    // NEXT: forward, hasPrev 항상 true
    // ──────────────────────────────────────────────

    @Test
    fun `NEXT - content가 pageSize와 같으면 hasNext false, hasPrev true, nextCursor null, prevCursor 첫 번째 항목`() {
        // given
        val content = listOf(1, 2, 3)
        val pageSize = 3

        // when
        val response = CursorPageResponse(content, CursorDirection.NEXT, pageSize)

        // then
        then(response.content).containsExactly(1, 2, 3)
        then(response.hasNext).isFalse()
        then(response.hasPrev).isTrue()
        then(response.nextCursor).isNull()
        then(response.prevCursor).isEqualTo(1)
    }

    @Test
    fun `NEXT - content가 pageSize 초과이면 hasNext true, hasPrev true, 양방향 커서 모두 설정`() {
        // given
        val content = listOf(1, 2, 3, 4) // pageSize+1
        val pageSize = 3

        // when
        val response = CursorPageResponse(content, CursorDirection.NEXT, pageSize)

        // then
        then(response.content).containsExactly(1, 2, 3)
        then(response.hasNext).isTrue()
        then(response.hasPrev).isTrue()
        then(response.nextCursor).isEqualTo(3)
        then(response.prevCursor).isEqualTo(1)
    }

    // ──────────────────────────────────────────────
    // LAST: backward, hasNext 항상 false
    // ──────────────────────────────────────────────

    @Test
    fun `LAST - content가 pageSize와 같으면 hasPrev false, hasNext false, 커서 모두 null`() {
        // given
        val content = listOf(1, 2, 3)
        val pageSize = 3

        // when
        val response = CursorPageResponse(content, CursorDirection.LAST, pageSize)

        // then
        then(response.content).hasSize(3)
        then(response.hasPrev).isFalse()
        then(response.hasNext).isFalse()
        then(response.prevCursor).isNull()
        then(response.nextCursor).isNull()
    }

    @Test
    fun `LAST - content가 pageSize 초과이면 hasPrev true, hasNext false, prevCursor 첫 번째 항목`() {
        // given
        val content = listOf(1, 2, 3, 4) // pageSize+1, DB에서 ASC로 온 결과
        val pageSize = 3

        // when
        val response = CursorPageResponse(content, CursorDirection.LAST, pageSize)

        // then - take(3).reversed() = [3,2,1]
        then(response.content).containsExactly(3, 2, 1)
        then(response.hasPrev).isTrue()
        then(response.hasNext).isFalse()
        then(response.prevCursor).isEqualTo(3) // reversed 후 첫 번째 항목
        then(response.nextCursor).isNull()
    }

    @Test
    fun `LAST - DB에서 ASC로 받은 content가 reversed되어 반환된다`() {
        // given
        val content = listOf(1, 2, 3) // DB ASC 순서
        val pageSize = 3

        // when
        val response = CursorPageResponse(content, CursorDirection.LAST, pageSize)

        // then - reversed
        then(response.content).containsExactly(3, 2, 1)
    }

    // ──────────────────────────────────────────────
    // PREV: backward, hasNext 항상 true
    // ──────────────────────────────────────────────

    @Test
    fun `PREV - content가 pageSize와 같으면 hasPrev false, hasNext true, nextCursor 마지막 항목, prevCursor null`() {
        // given
        val content = listOf(1, 2, 3) // DB ASC 순서
        val pageSize = 3

        // when
        val response = CursorPageResponse(content, CursorDirection.PREV, pageSize)

        // then - take(3).reversed() = [3,2,1]
        then(response.content).containsExactly(3, 2, 1)
        then(response.hasPrev).isFalse()
        then(response.hasNext).isTrue()
        then(response.nextCursor).isEqualTo(1) // reversed 후 마지막 항목
        then(response.prevCursor).isNull()
    }

    @Test
    fun `PREV - content가 pageSize 초과이면 hasPrev true, hasNext true, 양방향 커서 모두 설정`() {
        // given
        val content = listOf(1, 2, 3, 4) // pageSize+1, DB ASC 순서
        val pageSize = 3

        // when
        val response = CursorPageResponse(content, CursorDirection.PREV, pageSize)

        // then - take(3).reversed() = [3,2,1]
        then(response.content).containsExactly(3, 2, 1)
        then(response.hasPrev).isTrue()
        then(response.hasNext).isTrue()
        then(response.nextCursor).isEqualTo(1) // reversed 후 마지막 항목
        then(response.prevCursor).isEqualTo(3) // reversed 후 첫 번째 항목
    }
}
