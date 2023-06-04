package com.example.intellijtest

import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/shop")
class ShopApi(
    private val shopRepository: ShopRepository,
) {

    @GetMapping
    fun getShop(
        @PageableDefault pageable: Pageable,
    ): List<Shop> {

        return shopRepository.findAll()
    }

    @PostMapping
    fun save(
        @RequestBody dto: ShopRegistrationRequest,
    ) {

    }
}


class ShopRegistrationRequest(
    val brn: String,
    val name: String,
    val band: String,
    val category: String,
    val email: String,
    val website: String,
    val openingHours: String,
    val seatingCapacity: Int,
    val rating: Int,
    val address: String,
    val addressDetail: String,
    val zipCode: String,
)
