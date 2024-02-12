package com.example.kotlincoroutine.a3

class Cage2<T> {

    private val animals: MutableList<T> = mutableListOf()

    fun getFirst(): T {
        return animals.first()
    }

    fun put(animal: T) {
        this.animals.add(animal)
    }

    fun moveForm(otherCage: Cage2<out T>) {
        this.animals.addAll(otherCage.animals)
    }

    fun moveForm(otherCage: Cage2<in T>) {
        otherCage.animals.addAll(this.animals)
    }
}