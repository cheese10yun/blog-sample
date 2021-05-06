package com.example.springtransaction

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Entity
@Table(name = "book")
class Book(
    @Column(name = "title", nullable = false)
    var title: String,

//    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Order::class)
//    @JoinColumn(name = "order_id", nullable = false)
//    var order: Order
) : AuditingEntity()

@RestController
@RequestMapping("/api/book")
class BookApi(
    private val bookRepository: BookRepository,
    private val bookService: BookService
) {

    @GetMapping("/slave")
    @Transactional(readOnly = true)
    fun getSlave() = bookRepository.findAll()

    @GetMapping("/master")
    @Transactional(readOnly = false)
    fun getMaster() = bookRepository.findAll()


    @GetMapping("/update/slave")
    fun startSlave() {
        bookService.updateSlave()
    }

    @GetMapping("/update/master")
    fun startMaster() {
        bookService.updateMaster()
    }

}

@Service
class BookService(
    private val bookRepository: BookRepository,
    private val bookUpdateService: BookUpdateService
) {

    @Transactional(readOnly = true)
    fun updateSlave() {
        bookUpdateService.updateTitle("new title(slave)")
    }

    @Transactional(readOnly = false)
    fun updateMaster() {
        bookUpdateService.updateTitle("new title(master)")
    }

//    @Transactional(readOnly = false)
//    fun updateTitle(title: String) {
//        val books = bookRepository.findAll()
//        for (book in books) {
//            book.title = title
//        }
//        bookRepository.saveAll(books)
//    }
}

@Service
class BookUpdateService(
    private val bookRepository: BookRepository
){
    @Transactional(readOnly = false)
    fun updateTitle(title: String) {
        val books = bookRepository.findAll()
        for (book in books) {
            book.title = title
        }
        bookRepository.saveAll(books)
    }
}


@Entity
@Table(name = "orders")
class Order(
    @Column(name = "title", nullable = false)
    var number: String
) : AuditingEntity()

interface OrderRepository : JpaRepository<Order, Long>
interface BookRepository : JpaRepository<Book, Long>