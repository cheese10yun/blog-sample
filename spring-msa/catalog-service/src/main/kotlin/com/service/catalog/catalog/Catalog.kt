package com.service.catalog.catalog

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import javax.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository


@Entity
@Table(name = "catalog")
class Catalog(
    @Column(name = "product_id", nullable = false, length = 120)
    val productId: String,
    @Column(name = "product_name", nullable = false, length = 50)
    val productName: String,
    @Column(name = "stock", nullable = false, length = 50, unique = true)
    val stock: Int,
    @Column(name = "unit_price", nullable = false, length = 255)
    val unitPrice: Int,
) : EntityAuditing() {
}

interface CatalogRepository : JpaRepository<Catalog, Long> {
    fun findByProductId(productId: String)
}

@EntityListeners(value = [AuditingEntityListener::class])
@MappedSuperclass
abstract class EntityAuditing {
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