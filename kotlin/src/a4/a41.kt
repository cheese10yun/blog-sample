package a4

import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent


//object Payroll {
//    val allEmployees = arrayListOf<Person>()
//    fun calculateSalary() {
//        for (person in allEmployees) {
//        }
//    }
//}
//
//class A {
//    companion object {
//        fun bar() {
//            print("bar...")
//        }
//    }
//
//    fun foo() {
//        print("foo..")
//    }
//}


//class User private constructor(val nickname: String) {
//    companion object {
//        fun newSubscribingUser(email: String) = User(email.substringBefore('@'))
//        fun newFacebookUser(accountId: Int) = User(getFacebookName(accountId))
//    }
//}

//fun newInstance() {
//    User.newSubscribingUser("asd@asd.com")
//    User.newFacebookUser(1)
//}

interface JsonFactory<T> {
    fun fromJson(jsonText: String): T
}

//class Person(val name: String) {
//    companion object : JsonFactory<a4.Person> {
//        override fun fromJson(jsonText: String): a4.Person {
//            return a4.Person("....")
//        }
//    }
//}

//// 비지니스 로직 모듈 : 해당 객체
//class Person(val firstName: String, val lastName: String) {
//    companion object {}
//}
//
//// 클라이언트, 서버 통신 모듈
//fun Person.Companion.fromJson(json: String): a4.Person {
//    return Person("firstName", "lastNameL`")
//}
//
////val person = Person.fromJson(json)
//
//
//val listener = object : MouseAdapter() {
//    override fun mouseClicked(e: MouseEvent?) {
//        super.mouseClicked(e)
//    }
//    override fun mouseEntered(e: MouseEvent?) {
//        super.mouseEntered(e)
//    }
//}

class Person(val name: String, val age: Int) {

    override fun toString(): String {
        return "Person(name='$name', age=$age)"
    }
}


val person = listOf(Person("yun", 29), Person("wan", 28))

fun printMessageWithPrefix(message: Collection<String>, prefix: String) {
    message.forEach {
        print("$prefix $it")
    }
}

fun main() {
    printMessageWithPrefix(listOf("aws", "wws"), "error: ")
//    println(person.maxBy(Person::age))
//    print(person.maxBy { it.age })
//    person.maxBy(Person::age)
//    person.maxBy { p: Person -> p.age }
//    person.maxBy { p: Person -> p.age }
}


fun printProblemCounts(responses: Collection<String>) {
    var clientErrors = 0
    var serverErrors = 0

    responses.forEach {
        if (it.startsWith("4")) {
            clientErrors++
        } else if (it.startsWith("5")) {
            serverErrors++
        }
    }

    println("$clientErrors client errors, $serverErrors server errors")
}




























