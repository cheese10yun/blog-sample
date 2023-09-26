package com.example.kotlincoroutine.a1

fun main() {

    val cage = Cage()
    val first: Crap = cage.getFirst() as? Crap ?:throw IllegalArgumentException()

    val cage2 = Cage2<Crap>()
    cage2.put(Crap("잉어"))
    val first1 = cage2.getFirst()
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

class Cage2<T> {

    private val animals: MutableList<T> = mutableListOf()

    fun getFirst(): T {
        return animals.first()
    }

    fun put(animal: T) {
        this.animals.add(animal)
    }

    fun moveForm(cage: Cage2<T>) {
        this.animals.addAll(cage.animals)
    }
}


abstract class Animal(
    val name: String
)

abstract class Fish(name: String) : Animal(name)

class GoldFish(name: String) : Fish(name)

class Crap(name: String) : Fish(name)