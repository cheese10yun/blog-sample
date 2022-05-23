package com.example.msaerrorresponse

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.core.isSuccessful
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.result.onError
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Service
class BookReservationService() {
    private val BOOK_SERVICE_HOST: String = "http://book-service.copm"

    fun doReservation(bookId: Long, userId: Long) {
        val book = "$BOOK_SERVICE_HOST/api/v1/books/$bookId"
            .httpGet()
            .responseObject<Book>()
            .third
            .onError {
                if (it.response.isSuccessful.not()) {
                    throw IllegalArgumentException("bookId: $bookId not found")
                }
            }
            .get()

        val bookReservation = BookReservation(
            bookId = book.id,
            bookStatus = book.status,
            userId = userId
        )
        // bookReservationRepository.save(bookReservation) JPA를 사용한다고 가정하고 책 예약 반영


        // Book Service API에 책 상태 업데이트
        "$BOOK_SERVICE_HOST/api/v1/books"
            .httpPost()
            .header("Content-Type", "application/json")
            .jsonBody(
                """
                    {
                      "status": "RESERVATION"
                    }
                """.trimIndent()
            )
            .response()
            .third
            .onError {
                if (it.response.isSuccessful.not()) {
                    throw IllegalArgumentException("Book Status Failed")
                }
            }
            .get()

    }
}


//@Service
//class BookReservationService2() {
//    //    private val BOOK_SERVICE_HOST: String = "http://book-service.copm"
//    val bookClient = BookClient()
//
//    fun doReservation(bookId: Long, userId: Long) {
//
//        val boo = bookClient.getBook(bookId)
//
//        val bookReservation = BookReservation(
//            bookId = boo.id,
//            bookStatus = boo.status,
//            userId = userId
//        )
//        // bookReservationRepository.save(bookReservation) JPA를 사용한다고 가정하고 책 예약 반영
//
//        // Book Service API에 책 상태 업데이트
//        bookClient.updateBookStatus(status = "RESERVATION")
//    }
//}

@Service
class BookReservationService3(
    private val objectMapper: ObjectMapper
) {
    //    private val BOOK_SERVICE_HOST: String = "http://book-service.copm"
    val bookClient = BookClient(objectMapper = objectMapper)

    fun doReservation(bookId: Long, userId: Long) {
        val responseBook = bookClient.getBook(bookId)
        if (responseBook.first.not()) {
            throw IllegalArgumentException("bookId: $bookId not found")
        }
        val book = responseBook.second!!

        val bookReservation = BookReservation(
            bookId = book.id,
            bookStatus = book.status,
            userId = userId
        )
        // bookReservationRepository.save(bookReservation) JPA를 사용한다고 가정하고 책 예약 반영

        // Book Service API에 책 상태 업데이트
        val bookStatusUpdateResponse = bookClient.updateBookStatus(status = "RESERVATION", bookId = bookId)

        if (bookStatusUpdateResponse.first.not()) {
            throw IllegalArgumentException("book status update failed")
        }
    }
}


@Entity
@Table(name = "book_reservation")
data class BookReservation(
    val bookId: Long,
    val bookStatus: String,
    val userId: Long
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}

interface BookReservationRepository : JpaRepository<BookReservation, Long> {

}



@Service
class BookReservationFindService(
    private val bookReservationRepository: BookReservationRepository
) {
    fun findById(id: Long): BookReservation {
        return bookReservationRepository.findById(id).orElseThrow { throw IllegalArgumentException("id: $id Not found") }
    }
}

class BookClientRegistrationService() {
    private val bookClient = BookClient()

    fun register(
        bookName: String,
        bookCode: String,
        publisher: String
    ) {
        // 필요한 비즈니스 로직 진행 ...
        // 로직 진행 이후 HTTP Client 호출
        bookClient.registerBook(
            bookName = bookName,
            bookCode = bookCode,
            publisher = publisher,
        )
    }
}

class BookClient(
    private val host: String = "http://book-service.copm",
    private val objectMapper: ObjectMapper? = null
) {

    fun getBook(bookId: Long) =
        "$host/api/v1/books/$bookId"
            .httpGet()
            .responseObject<Book>()
// 비즈니스 예외는 서비스 레이어에서 책임 진다.
//            .third
//            .onError {
//                if (it.response.isSuccessful.not()) {
//                    throw IllegalArgumentException("bookId: $bookId not found")
//                }
//            }
//            .get()
//            .second
            .run {
                Pair(
                    this.second.isSuccessful,
                    when {
                        this.second.isSuccessful -> this.third.get()
                        else -> null
                    }
                )
            }

    fun updateBookStatus(
        bookId: Long,
        status: String
    ) =
        "$host/api/v1/books/$bookId"
            .httpPut()
            .header("Content-Type", "application/json")
            .jsonBody(
                """
                    {
                      "status": "$status"
                    }
                """.trimIndent()
            )
// 비즈니스 예외는 서비스 레이어에서 책임 진다.
//            .response()
//            .third
//            .onError {
//                if (it.response.isSuccessful.not()) {
//                    throw IllegalArgumentException("Book Status Failed")
//                }
//            }
            .response()
            .run {
                Pair(
                    second.isSuccessful,
                    when {
                        second.isSuccessful -> null
                        else -> {
                            objectMapper!!.readValue(
                                String(second.body().toByteArray()),
                                ErrorResponse::class.java
                            )
                        }
                    }
                )
            }

    fun registerBook(
        bookName: String,
        bookCode: String,
        publisher: String
    ) {
        // bookCode가 publisher에 맞게 적절하게 생생했는지 비즈니스 유효성 검증이 필요..

        "$host/api/v1/books"
            .httpPost()
            .header("Content-Type", "application/json")
            .jsonBody("...")
    }
}

data class Book(
    val id: Long,
    val name: String,
    val status: String
)

class BookBulkStatusUpdate() {

    fun update(bookId: Long, bookStatus: String) {
        BookClient()
            .updateBookStatus(bookId, bookStatus)

        // 벌크 업데이트 진행 여부를 데이터베이스에 기록하는 로직...
    }
}