package com.example.mongostudy

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/persons")
class PersonController(
    private val personRepository: PersonRepository,
    private val personQueryService: PersonQueryService
) {

    @GetMapping
    fun getAllPersons(): List<Person> {
        return personRepository.findAll()
    }

    @GetMapping("/query")
    fun getQuery(
        @RequestParam("firstName") firstName: String
    ): List<Person> {
        return personQueryService.findBy(firstName)
    }

    @GetMapping("/sample")
    fun sample(
    ) = personQueryService.groupByLastName()

    @PostMapping("/")
    fun createPerson(@RequestBody person: Person): Person {
        return personRepository.save(person)
    }

    @GetMapping("/{id}")
    fun getPersonById(@PathVariable id: String): Person? {
        return personRepository.findById(id).orElse(null)
    }
}