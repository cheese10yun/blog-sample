package com.cheese.yun.domain.model

import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class Address(
    @Column(name = "zip_code", nullable = false)
    val zipCode: String,
    @Column(name = "city", nullable = false)
    val city: String,
    @Column(name = "detail_address", nullable = false)
    val detailAddress: String
)