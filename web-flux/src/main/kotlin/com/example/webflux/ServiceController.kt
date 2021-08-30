package com.example.webflux

import java.time.Duration
import java.util.Random
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink

@RestController
class ServiceController(
    private val kitchenService: KitchenService
) {

    @GetMapping(value = ["/server"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun serverDishes() = kitchenService.getDishes()
}

@Controller
class HomeController {

    @GetMapping
    fun home() = Mono.just("home")
}

@Service
class KitchenService {

    private val picker = Random()
    private val menu = listOf(
        Dish("Sesame chicken"),
        Dish("Lo mein noodles, plain"),
        Dish("Sweet & sour beef")
    )

    /**
     * 요리 스트림 생성
     */
    fun getDishes() =
        Flux.generate { sink: SynchronousSink<Dish> ->
            sink.next(randomDish())
        }
            .delayElements(Duration.ofMillis(250))

    /**
     * 요리 무작위 선택
     */
    private fun randomDish() =
        menu[picker.nextInt(menu.size)]

}

data class Dish(
    val description: String
) {
    var delivered = false
        private set

    companion object {

        fun deliver(dish: Dish) {
            val deliverDish = Dish(dish.description)
            deliverDish.also {
                it.delivered = true
            }
        }
    }
}