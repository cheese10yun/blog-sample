package com.example.webflux

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
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
    fun getCustomer(id: Int): Mono<Customer>?
    fun searchCustomers(nameFilter: String): Flux<Customer>
    fun createCustomer(customerMono: Mono<Customer>): Mono<*>

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

    override fun getCustomer(id: Int) = customers[id]?.toMono()

    override fun searchCustomers(nameFilter: String): Flux<Customer> {
        return customers.filter {
            it.value.name.contains(nameFilter, true)
        }.map(Map.Entry<Int, Customer>::value).toFlux()
    }

    override fun createCustomer(customerMono: Mono<Customer>): Mono<*> {
        return customerMono.map {
            customers[it.id] = it
            Mono.empty<Any>()
        }
    }
}