package com.example.batch.domain.order.domain

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.*


@Entity
@Table(name = "orders2")
@Access(AccessType.FIELD) // 용도는 ?
data class Order2(
        @Column(name = "amount", nullable = false)
        var amount: BigDecimal
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0


    @CreationTimestamp
    @Column(name = "created_at", nullable = true, updatable = false)
    lateinit var createdAt: LocalDateTime
        protected set

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = true)
    lateinit var updatedAt: LocalDateTime
        protected set

}