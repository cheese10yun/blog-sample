package com.spring.cloud.licensing

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "license")
data class License(
    @Column(name = "organization_id", nullable = false)
    var organizationId: String,

    @Column(name = "product_name", nullable = false)
    var productName: String

) : EntityAuditing()

interface LicenseRepository : JpaRepository<License, Long>

@RestController
@RequestMapping("/licenses")
class LicenseApi(
    private val licenseRepository: LicenseRepository,
    private val serviceConfig: ServiceConfig
) {

    @GetMapping
    fun getByPage(pageable: Pageable) =
        licenseRepository.findAll(pageable)


    @PostMapping
    fun create() {
        licenseRepository.save(License(UUID.randomUUID().toString(), "name"))
    }

    @GetMapping("/property")
    fun getProperty() =
        serviceConfig.exampleProperty
}

@Component
@RefreshScope
class ServiceConfig {
    @Value("\${example.property}")
    lateinit var exampleProperty: String
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