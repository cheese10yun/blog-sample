package org.example.boot3

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Boot3Application

fun main(args: Array<String>) {
	runApplication<Boot3Application>(*args)
}
