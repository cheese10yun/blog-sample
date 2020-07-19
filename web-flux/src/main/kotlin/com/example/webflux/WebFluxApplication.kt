package com.example.webflux

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
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
    fun getCustomer(@PathVariable id: Int): ResponseEntity<Customer> {
        return ResponseEntity(customerService.getCustomer(id), HttpStatus.OK)
    }

    @GetMapping("/customer")
    fun getCustomers(@RequestParam(required = false, defaultValue = "") nameFilter: String): ResponseEntity<List<Customer>> {
        return ResponseEntity(customerService.searchCustomers(nameFilter), HttpStatus.OK)
    }


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
    fun getCustomer(id: Int): Customer?
    fun searchCustomers(nameFilter: String): List<Customer>

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

    override fun getCustomer(id: Int) = customers[id]

    override fun searchCustomers(nameFilter: String): List<Customer> {
        return customers.filter {
            it.value.name.contains(nameFilter, true)
        }.map(Map.Entry<Int, Customer>::value).toList()
    }


}