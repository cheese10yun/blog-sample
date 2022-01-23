package com.batch.payment.domain.book

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Table(
    name = "book",
    indexes = [
        Index(columnList = "created_at", name = "idx_created_at")
    ]
)
class Book(
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: BookStatus = BookStatus.AVAILABLE_RENTAL
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        internal set

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime
        internal set

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime
        internal set
}

enum class BookStatus(
    val description: String
) {
    RENTAL("대여중"),
    LOST("분실"),
    AVAILABLE_RENTAL("대여가능")
}

interface BookRepository : JpaRepository<Book, Long>