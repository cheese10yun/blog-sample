package com.example.intellijtest

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SmartPhoneTest {

    @Test
    internal fun `dialing delegates to internal phone`() {
        val smartPhone = SmartPhone()
        val dial = smartPhone.dial("111")
        println(dial) // Dialing 111
    }

    @Test
    internal fun `Taking picture delegates to internal camera`() {
        val smartPhone = SmartPhone()
        val message = smartPhone.takePictrue()
        println(message) // Taking Picture
    }
}


data class Project(val map: MutableMap<String, Any>) {
    val name: String by map
    val priority: Int by map
    val completed: Boolean by map


}

class ProjectTest {

    @Test
    internal fun `use map delegate for project`() {
        val project = Project(
            mutableMapOf(
                "name" to "Lean Kotlin",
                "priority" to 5,
                "completed" to true
            )
        )

        println(project)
        // Project(map={name=Lean Kotlin, priority=5, completed=true})
    }
}