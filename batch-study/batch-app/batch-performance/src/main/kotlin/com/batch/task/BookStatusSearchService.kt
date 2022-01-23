package com.batch.task

import com.batch.payment.domain.book.BookStatus
import kotlin.random.Random
import org.springframework.stereotype.Service

@Service
class BookStatusSearchService {

    /**
     * 외부 인프라를 통해서 가쟈옴
     * API를 조회 한다고 가정하고 대략 400ms 발생한다고 가정한다.
     */
    fun getLatestBookStatus(bookId: List<Long>): Map<BookStatus, List<Long>> {
        Thread.sleep(400)
        val groupBy = bookId.groupBy {
            getBookStatus()
        }

        return groupBy
    }

    private fun getBookStatus() =
        when (Random.nextInt(0, 3)) {
            1 -> BookStatus.AVAILABLE_RENTAL
            2 -> BookStatus.RENTAL
            else -> BookStatus.LOST
        }

}