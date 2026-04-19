package com.example.querydsl.repository.support

import com.querydsl.core.types.EntityPath
import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.NumberPath
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import kotlin.properties.Delegates.notNull
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import java.util.function.Function
import kotlinx.coroutines.Dispatchers
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport


abstract class Querydsl4RepositorySupport(domainClass: Class<*>) : QuerydslRepositorySupport(domainClass) {

    protected var queryFactory: JPAQueryFactory by notNull()

    @PersistenceContext
    override fun setEntityManager(entityManager: EntityManager) {
        this.queryFactory = JPAQueryFactory(entityManager)
        super.setEntityManager(entityManager)
    }

    protected fun <T> select(expr: Expression<T>): JPAQuery<T> {
        return queryFactory.select(expr)
    }

    protected fun <T> selectFrom(from: EntityPath<T>): JPAQuery<T> {
        return queryFactory.selectFrom(from)
    }

    protected fun from(path: EntityPath<*>): JPAQuery<*> {
        return queryFactory.from(path)
    }

    protected fun <T> applyPagination(
        pageable: Pageable,
        contentQuery: Function<JPAQueryFactory, JPAQuery<T>>,
        countQuery: Function<JPAQueryFactory, JPAQuery<Long>>
    ): Page<T> = runBlocking {
        val jpaContentQuery = contentQuery.apply(queryFactory)
        val content = async(Dispatchers.IO) { querydsl!!.applyPagination(pageable, jpaContentQuery).fetch() as List<T> }
        val count = async(Dispatchers.IO) { countQuery.apply(queryFactory).fetchFirst() }

        PageImpl(content.await(), pageable, count.await())
    }

    protected fun <T> applySlicePagination(
        pageable: Pageable,
        query: Function<JPAQueryFactory, JPAQuery<T>>
    ): Slice<T> {
        val jpaContentQuery = query.apply(queryFactory)
        val content = querydsl!!.applyPagination(pageable, jpaContentQuery).fetch()
        val hasNext = content.size >= pageable.pageSize
        return SliceImpl(content, pageable, hasNext)
    }

    /**
     * 커서 기반 페이지네이션을 적용한다.
     *
     * caller는 [contentQuery]에 정렬/조건 없이 기본 쿼리만 넘기면 되고,
     * where 절(커서 조건), orderBy, limit 은 이 메서드가 자동으로 추가한다.
     *
     * ## 방향별 동작
     *
     * | direction | where 조건            | 정렬      | 비고                        |
     * |-----------|-----------------------|-----------|-----------------------------|
     * | FIRST     | 없음                  | DESC      | 커서 불필요                 |
     * | NEXT      | cursorPath < cursor   | DESC      | 커서 필수 (이전 마지막 ID)  |
     * | PREV      | cursorPath > cursor   | ASC       | 커서 필수 (현재 첫 번째 ID) |
     * | LAST      | 없음                  | ASC       | 커서 불필요                 |
     *
     * ## hasNext / hasPrev 판별
     * DB 조회 시 pageSize + 1 개를 가져온다.
     * [CursorPageResponse.invoke] 내부에서 실제 content 를 잘라내고 남는 1개로 more 여부를 판단한다.
     *
     * @param cursorRequest 방향(direction), 커서 키(cursorKey), 페이지 크기(pageSize)
     * @param cursorPath    커서 기준이 되는 엔티티의 ID 경로 (예: QPayment.payment.id)
     * @param cursorSelector 조회 결과 항목 → 커서 문자열 변환 함수
     * @param contentQuery  기본 조회 쿼리 (where/orderBy/limit 제외)
     */
    fun <T> applyCursorPagination(
        cursorRequest: CursorRequest,
        cursorPath: NumberPath<Long>,
        cursorSelector: (T) -> String,
        contentQuery: Function<JPAQueryFactory, JPAQuery<T>>,
    ): CursorPageResponse<T> {
        val direction = cursorRequest.direction
        val pageSize = cursorRequest.pageSize
        val cursorValue = cursorRequest.cursorKey?.toLong()

        val query = contentQuery.apply(queryFactory)

        // 커서 위치 기준으로 where 조건 추가
        // NEXT: 현재 커서보다 작은 ID (이후 데이터, DESC 정렬이므로)
        // PREV: 현재 커서보다 큰 ID (이전 데이터, ASC 정렬이므로)
        when (direction) {
            CursorDirection.FIRST, CursorDirection.LAST -> Unit
            CursorDirection.NEXT -> {
                requireNotNull(cursorValue) { "Cursor key must be provided for NEXT direction" }
                query.where(cursorPath.lt(cursorValue))
            }
            CursorDirection.PREV -> {
                requireNotNull(cursorValue) { "Cursor key must be provided for PREV direction" }
                query.where(cursorPath.gt(cursorValue))
            }
        }

        // forward(FIRST/NEXT): DESC — 최신순으로 읽고 CursorPageResponse 에서 그대로 사용
        // backward(LAST/PREV) : ASC  — 오래된 순으로 읽고 CursorPageResponse 에서 reversed() 처리
        query.orderBy(
            when {
                direction.isForward -> cursorPath.desc()
                else -> cursorPath.asc()
            }
        )
        // hasNext/hasPrev 판별을 위해 pageSize + 1 개 조회
        query.limit((pageSize + 1).toLong())
        val content = query.fetch()
        return CursorPageResponse(
            content = content,
            direction = direction,
            pageSize = pageSize,
            encodeCursor = cursorSelector,
        )
    }
}

/**
 * 커서 기반 페이지네이션 응답.
 *
 * 생성자는 private 이며, [invoke] 팩토리 함수를 통해서만 생성한다.
 * [applyCursorPagination] 이 pageSize + 1 개를 조회한 raw content 를 그대로 넘기면
 * [invoke] 내부에서 실제 content 를 잘라내고 커서·플래그를 계산한다.
 *
 * ## 방향별 결과
 *
 * | direction | actualContent         | hasNext | hasPrev | nextCursor        | prevCursor        |
 * |-----------|-----------------------|---------|---------|-------------------|-------------------|
 * | FIRST     | take(pageSize)        | 초과 여부 | false   | 마지막 항목 or null | null              |
 * | NEXT      | take(pageSize)        | 초과 여부 | true    | 마지막 항목 or null | 첫 번째 항목       |
 * | PREV      | take(pageSize).reversed() | true | 초과 여부 | 마지막 항목        | 첫 번째 항목 or null |
 * | LAST      | take(pageSize).reversed() | false | 초과 여부 | null              | 첫 번째 항목 or null |
 */
data class CursorPageResponse<T> private constructor(
    val content: List<T>,
    val hasNext: Boolean,
    val hasPrev: Boolean,
    val nextCursor: String?,
    val prevCursor: String?,
    val encodeCursor: (T) -> String,
) {

    companion object {
        /**
         * [content] 가 비어 있으면 모든 플래그를 false, 커서를 null 로 즉시 반환한다.
         *
         * forward(FIRST/NEXT): DB 조회 결과가 이미 DESC 순이므로 그대로 사용한다.
         * - hasNext  : content 가 pageSize 초과이면 다음 페이지 존재
         * - hasPrev  : NEXT 방향이면 항상 true (FIRST 이면 false)
         * - nextCursor: actualContent 의 마지막 항목 (없으면 null)
         * - prevCursor: actualContent 의 첫 번째 항목 (hasPrev 일 때만)
         *
         * backward(LAST/PREV): DB 조회 결과가 ASC 순이므로 reversed() 로 뒤집어 표시 순서를 맞춘다.
         * - hasPrev  : content 가 pageSize 초과이면 이전 페이지 존재
         * - hasNext  : PREV 방향이면 항상 true (LAST 이면 false)
         * - nextCursor: actualContent 의 마지막 항목 (hasNext 일 때만)
         * - prevCursor: actualContent 의 첫 번째 항목 (없으면 null)
         */
        operator fun <T> invoke(
            content: List<T>,
            direction: CursorDirection,
            pageSize: Int,
            encodeCursor: (T) -> String,
        ): CursorPageResponse<T> {
            if (content.isEmpty()) {
                return CursorPageResponse(
                    content = emptyList(),
                    hasNext = false,
                    hasPrev = false,
                    nextCursor = null,
                    prevCursor = null,
                    encodeCursor = encodeCursor,
                )
            }

            return if (direction.isForward) {
                val hasNext = content.size > pageSize
                val actualContent = content.take(pageSize)
                val hasPrev = direction == CursorDirection.NEXT
                CursorPageResponse(
                    content = actualContent,
                    hasNext = hasNext,
                    hasPrev = hasPrev,
                    nextCursor = if (hasNext) encodeCursor(actualContent.last()) else null,
                    prevCursor = if (hasPrev) encodeCursor(actualContent.first()) else null,
                    encodeCursor = encodeCursor,
                )
            } else {
                val hasPrev = content.size > pageSize
                val actualContent = content.take(pageSize).reversed()
                val hasNext = direction == CursorDirection.PREV
                CursorPageResponse(
                    content = actualContent,
                    hasNext = hasNext,
                    hasPrev = hasPrev,
                    nextCursor = if (hasNext) encodeCursor(actualContent.last()) else null,
                    prevCursor = if (hasPrev) encodeCursor(actualContent.first()) else null,
                    encodeCursor = encodeCursor,
                )
            }
        }
    }

}

data class CursorRequest(
    val cursorKey: String?,
    val direction: CursorDirection,
    val pageSize: Int,
)

enum class CursorDirection(val isForward: Boolean) {
    FIRST(isForward = true),
    PREV(isForward = false),
    NEXT(isForward = true),
    LAST(isForward = false),
    ;

    val isBackward: Boolean get() = isForward.not()
}