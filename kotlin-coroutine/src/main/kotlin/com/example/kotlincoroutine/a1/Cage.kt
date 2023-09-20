package com.example.kotlincoroutine.a1

fun main() {

    val cage = Cage()

    cage.put(Crap("잉어"))

    val first: Crap = cage.getFirst() as Crap

}

class Cage {

    private val animals: MutableList<Animal> = mutableListOf()

    fun getFirst(): Animal {
        return animals.first()
    }

    fun put(animal: Animal) {
        this.animals.add(animal)
    }

    fun moveForm(cage: Cage) {
        this.animals.addAll(cage.animals)
    }
}


abstract class Animal(
    val name: String
)

abstract class Fish(name: String) : Animal(name)

class GoldFish(name: String) : Fish(name)

class Crap(name: String) : Fish(name)