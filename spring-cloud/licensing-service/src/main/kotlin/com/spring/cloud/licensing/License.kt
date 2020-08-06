package com.spring.cloud.licensing

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.cloud.netflix.ribbon.RibbonClient
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
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
    private val serviceConfig: ServiceConfig,
    private val organizationClient: OrganizationClient
) {

    @GetMapping
    @HystrixCommand(
        //        commandProperties = [
//            HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "12000")
//        ],
        commandProperties = [
            HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "10000")
        ]
    )
    fun getByPage(pageable: Pageable, @RequestParam sleep: Long?): Page<License> {

        sleep?.let {
            Thread.sleep(it)
        }

        return licenseRepository.findAll(pageable)
    }

    @PostMapping
    fun create() {
        licenseRepository.save(License(UUID.randomUUID().toString(), "name"))
    }

    @GetMapping("/organizations/{organizationId}")
    fun getOrganizationId(@PathVariable organizationId: Long): OrganizationClient.OrganizationResponse {
        return organizationClient.getOrganization(organizationId)
    }

    @GetMapping("/{id}")
    @HystrixCommand(
        fallbackMethod = "buildFallbackLicense",
        threadPoolKey = "licenseThreadPool",
        threadPoolProperties = [
            HystrixProperty(name = "coreSize", value = "30"),
            HystrixProperty(name = "maxQueueSize", value = "10")
        ]
    )
    fun getBy(@PathVariable id: Long): LicenseResponse {
        randomSleep()
        return LicenseResponse(licenseRepository.findById(id).orElseThrow { IllegalArgumentException("$id is not found") })
    }

    class LicenseResponse constructor(license: License) {
        val organizationId = license.organizationId
        val productName = license.productName
    }

    private fun buildFallbackLicense(id: Long) =
        LicenseResponse(License("default value $id ", "default value"))

    private fun randomSleep() {
        if (Random().nextInt((3 - 1) + 1) + 1 == 3) {
            Thread.sleep(11000)
        }
    }

    @GetMapping("/property")
    fun getProperty(): String {
        val exampleProperty = serviceConfig.exampleProperty
        println("exampleProperty: $exampleProperty")
        return exampleProperty
    }
}

@Component
@RefreshScope
class ServiceConfig {
    @Value("\${example.property}")
    lateinit var exampleProperty: String
}

@FeignClient("organization-service")
@RibbonClient("organization-service")
interface OrganizationClient {

    @GetMapping("/organizations/{organizationId}")
    fun getOrganization(@PathVariable organizationId: Long): OrganizationResponse

    data class OrganizationResponse(
        val id: Long,
        val name: String,
        val contactName: String,
        val contactEmail: String,
        val contactPhone: String
    )
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