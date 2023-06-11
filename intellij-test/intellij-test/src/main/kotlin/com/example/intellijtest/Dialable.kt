package com.example.intellijtest


interface Dialable {
    val name: String
    fun dial(number: String): String
}

class Phone(override val name: String) : Dialable {
    override fun dial(number: String): String = "Dialing $number"
}

interface Snappable {
    fun takePictrue(): String
}

class Camera : Snappable {
    override fun takePictrue() = "Taking Picture"
}

class SmartPhone(
    private val phone: Dialable = Phone("name"),
    private val camera: Snappable = Camera()
) : Dialable by phone, Snappable by camera