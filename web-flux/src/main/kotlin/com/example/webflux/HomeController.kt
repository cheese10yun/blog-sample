package com.example.webflux

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.reactive.result.view.Rendering
import reactor.core.publisher.Mono

@Controller
class HomeController(
    private val itemRepository: ItemRepository,
    private val cartRepository: CartRepository
) {

    @GetMapping
    fun home(): Mono<Rendering> {
        val findAll = this.itemRepository.findAll()
        return Mono.just(
            Rendering.view("home.html")
                .modelAttribute("items", findAll)
                .modelAttribute(
                    "Cart",
                    this.cartRepository.findById("My Cart").defaultIfEmpty(Cart("MyC Cart"))
                )
                .build()
        )
    }
}