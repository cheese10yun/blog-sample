package com.spring.cloud.organization

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import javax.persistence.*


@Entity
@Table(name = "organizations")
class Organization(
    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "contact_name", nullable = false)
    var contactName: String,

    @Column(name = "contact_email", nullable = false)
    var contactEmail: String,

    @Column(name = "contact_phone", nullable = false)
    var contactPhone: String
) : EntityAuditing()

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