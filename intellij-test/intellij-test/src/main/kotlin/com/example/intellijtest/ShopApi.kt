package com.example.intellijtest

import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class ShopApi(
    private val shopRepository: ShopRepository,
) {

    @GetMapping
    fun getShop(
        @PageableDefault pageable: Pageable,
    ) {

        shopRepository.findAll()
    }
}

class ShopResponse(shop: Shop) {

}
