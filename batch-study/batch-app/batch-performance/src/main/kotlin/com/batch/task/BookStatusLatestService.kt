package com.batch.task

import com.batch.payment.domain.book.BookStatus

import com.batch.payment.domain.book.QBook.book as qBook
import com.batch.payment.domain.book.Book
import com.querydsl.jpa.impl.JPAQueryFactory
import kotlin.random.Random
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookStatusLatestService(
    private val query: JPAQueryFactory
) {

    /**
     * 외부 인프라를 통해서 가쟈옴
     * API를 조회 한다고 가정하고 대략 400ms 발생한다고 가정한다.
     * update where in 으로 업데이트
     */
    @Transactional(transactionManager = "transactionManager")
    fun updateInLatestBookStatus(bookId: List<Long>) {
//        Thread.sleep(400)
        val bookStatusGroupBy = bookId.groupBy {
            getBookStatus()
        }

        val bookLostStatusIds = bookStatusGroupBy[BookStatus.LOST]
        val bookRentalStatusIds = bookStatusGroupBy[BookStatus.RENTAL]
        val bookAvailableRentalStatusIds = bookStatusGroupBy[BookStatus.AVAILABLE_RENTAL]

        if (bookLostStatusIds.isNullOrEmpty().not()) {
            query.update(qBook)
                .set(qBook.status, BookStatus.LOST)
                .where(qBook.id.`in`(bookLostStatusIds))
                .execute()
        }

        if (bookRentalStatusIds.isNullOrEmpty().not()) {
            query.update(qBook)
                .set(qBook.status, BookStatus.RENTAL)
                .where(qBook.id.`in`(bookRentalStatusIds))
                .execute()
        }

        if (bookAvailableRentalStatusIds.isNullOrEmpty().not()) {
            query.update(qBook)
                .set(qBook.status, BookStatus.AVAILABLE_RENTAL)
                .where(qBook.id.`in`(bookAvailableRentalStatusIds))
                .execute()
        }
    }

    @Transactional(transactionManager = "transactionManager")
    fun updateLatestBookStatus(books: List<Book>) {
        for (book in books) {
            book.status = getBookStatus()
        }
    }

    private fun getBookStatus() =
        when (Random.nextInt(0, 3)) {
            1 -> BookStatus.AVAILABLE_RENTAL
            2 -> BookStatus.RENTAL
            else -> BookStatus.LOST
        }

}