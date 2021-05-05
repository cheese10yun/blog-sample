package com.example.springtransaction

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
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
    private val bookRepository: BookRepository
) {

    @GetMapping
    @Transactional(readOnly = true)
    fun get() = bookRepository.findAll()
}

@Entity
@Table(name = "orders")
class Order(
    @Column(name = "title", nullable = false)
    var number: String
) : AuditingEntity()

interface OrderRepository : JpaRepository<Order, Long>
interface BookRepository : JpaRepository<Book, Long>