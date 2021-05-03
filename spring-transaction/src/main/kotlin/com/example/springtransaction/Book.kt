package com.example.springtransaction

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Table(name = "book")
class Book(
    @Column(name = "title", nullable = false)
    var title: String,

//    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Order::class)
//    @JoinColumn(name = "order_id", nullable = false)
//    var order: Order
) : AuditingEntity()

@Entity
@Table(name = "orders")
class Order(
    @Column(name = "title", nullable = false)
    var number: String
) : AuditingEntity()

interface OrderRepository : JpaRepository<Order, Long>
interface BookRepository : JpaRepository<Book, Long>