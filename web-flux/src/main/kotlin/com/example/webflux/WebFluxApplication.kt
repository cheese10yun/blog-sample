package com.example.webflux

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.bodyToMono
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

@SpringBootApplication
class WebFluxApplication

fun main(args: Array<String>) {
    runApplication<WebFluxApplication>(*args)
}

@RestController
class CustomerController(
    private val customerService: CustomerService
) {

    @GetMapping("/customer/{id}")
    fun getCustomer(@PathVariable id: Int): ResponseEntity<Mono<Customer>> =
        ResponseEntity(customerService.getCustomer(id), HttpStatus.OK)

    @GetMapping("/customer")
    fun getCustomers(@RequestParam(required = false, defaultValue = "") nameFilter: String) =
        ResponseEntity(customerService.searchCustomers(nameFilter), HttpStatus.OK)

    @PostMapping("/customer")
    fun createCustomer(@RequestBody customerMono: Mono<Customer>) =
        ResponseEntity(customerService.createCustomer(customerMono), HttpStatus.CREATED)
}

data class Customer(
    var id: Int = 0,
    var name: String = "",
    var telephone: Telephone? = null
)

data class Telephone(
    var countryCode: String = "",
    var telephoneNumber: String = ""
)

interface CustomerService {
    fun getCustomer(id: Int): Mono<Customer>
    fun searchCustomers(nameFilter: String): Flux<Customer>
    fun createCustomer(customerMono: Mono<Customer>): Mono<Customer>
}

@Service
class CustomerServiceImpl : CustomerService {
    companion object {
        val initialCustomers = listOf(
            Customer(1, "kotlin"),
            Customer(2, "Spring"),
            Customer(3, "Microservice", Telephone("+82", "01029333211111"))
        )

        val customers = ConcurrentHashMap<Int, Customer>(initialCustomers.associateBy(Customer::id))
    }

    override fun getCustomer(id: Int) = customers[id]?.toMono() ?: Mono.empty()

    override fun searchCustomers(nameFilter: String): Flux<Customer> {
        return customers.filter {
            it.value.name.contains(nameFilter, true)
        }.map(Map.Entry<Int, Customer>::value).toFlux()
    }

    override fun createCustomer(customerMono: Mono<Customer>) =
        customerMono.flatMap {
            if (customers[it.id] == null) {
                customers[it.id] = it
                it.toMono()
            } else {
                Mono.error(CustomerExistException("Customer ${it.id} already exist"))
            }
        }
}

@Component
class CustomRouter(
    private val customerHandler: CustomerHandler
) {

//    @Bean
//    fun customerRoutes(): RouterFunction<*> = router {
//        "/functional".nest {
//            "/customer".nest {
//                GET("/") {
////                    ok().body("hello world".toMono(), String::class.java)
//                    ok().body(Customer(1, "functional web").toMono(), Customer::class.java)
////                    it: ServerRequest -> customerHandler.get(it)
//                }
//            }
//        }
//    }

    @Bean
    fun customerRoutes() = router {
        "/functional".nest {
            "/customer".nest {
                GET("/{id}", customerHandler::get)
                POST("/{id}", customerHandler::create)
            }

            "/customers".nest {
                GET("/", customerHandler::search)
            }
        }
    }
}

@Component
class CustomerHandler(
    private val customerService: CustomerService
) {

    fun get(serverRequest: ServerRequest) =
        customerService.getCustomer(serverRequest.pathVariable("id").toInt())
            .flatMap { ok().body(fromObject(it)) }
            .switchIfEmpty(notFound().build())

    fun search(serverRequest: ServerRequest) =
        ok().body(customerService.searchCustomers(serverRequest.queryParam("nameFilter").orElse("")), Customer::class.java)

    fun create(serverRequest: ServerRequest) =
        customerService.createCustomer(serverRequest.bodyToMono())
            .flatMap {
                created(URI.create("/functional/customer/${it.id}")).build()
            }.onErrorResume {
                badRequest().body(fromObject(ErrorResponse(
                    error = "error creating customer",
                    message = it.message ?: "error"))
                )
            }

    data class ErrorResponse(
        val error: String,
        val message: String
    )
}

class CustomerExistException(override val message: String) : Exception(message)