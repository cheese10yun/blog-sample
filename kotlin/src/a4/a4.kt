package a4

import java.io.Serializable
import java.lang.IllegalArgumentException

interface Clickable {
    fun click() // 일반 메서드 선언
    fun showOff() = println("Im clickable!") // 디폴트 구현이 있는 메서드
}

//class Button : Clickable {
//    override fun click() = println("I was clicked")
//}

open class RichButton : Clickable {
    fun disable() {} // 이 함수는 final이다. 하위 클래스가 이 메서드를 오버라이드할 수 없다.

    open fun animate() {} // 이 함수는 open이다. 하위 클래스에서 오버라이드 할 수 있다.

    override fun click() {} // 이 함수는 열려있는 메서드를 오버라이드한다. 오버라이드한 메서드는 기본적으로 open 이다.
}


abstract class Animated { // 이 클래스는 추상클래스다. 이 클래스의 인스턴스를 만들 수 없다.

    abstract fun animate() // 이함수는 추상 함수다. 이 함수에는 구현이 없다 하위 클래스에서는 이 함수를 반드시 오버라이드해야 한다.

    open fun stopAnimating() {} // 추상 클래스에 속했더라도 비추상 함수는 기본적으로 final이지만 open으로 오버라이드를 허용할 수 있다.

    fun animateTwice() {} // 추상 클래스 함수는 기본적으로 final이다.

}

internal open class TalkativeButton {
    private fun yell() = println("Hey!")

    protected fun whisper() = print("Let talk")
}

//fun TalkativeButton.giveSpeech() {
//    yell()
//    whisper()
//}

interface State : Serializable

interface View {
    fun getCurrentState(): State
    fun restoreState(state: State)
}

//class Button : View {
//
////    override fun getCurrentState(): State {}
//
//    override fun restoreState(state: State) {}
//
//    class ButtonState : State {} // 이 클래스는 자바 중첩(static class) 클래스와 대응한다
//
//}

class Outer {
    inner class Inner {
        fun getOuterReference(): Outer = this@Outer
    }
}

//interface Expr
//class Num(val value: Int) : Expr
//class Sum(val left: Expr, val right: Expr) : Expr

sealed class Expr {
    class Num(val value: Int) : Expr()
    class Sum(val left: Expr, val right: Expr) : Expr()
}


fun eval(e: Expr): Int =
    when (e) {
        is Expr.Num -> e.value
        is Expr.Sum -> eval(e.left) + eval(e.right)
    }


//class User constructor(_nickname: String) {
//    val nickname: String
//
//    init {
//        nickname = _nickname
//    }
//}

//interface User {
//    val nickname: String
//}

//class PrivateUser(override val nickname: String) : User
//
//class SubscribingUser(val email: String) : User {
//    override val nickname: String
//        get() = email.substringBefore('@')
//}
//
//class FacebookUser(val accountId: Int): User{
//    override val nickname = getFacebookName(accountId)
//}

//class User(val name: String) {
//    var address: String = "unspecified"
//        set(value: String) {
//            println(
//                """
//                Address was changed for $name:
//                "$field" -> "$value".""".trimIndent()
//            )
//            field = value
//        }
//}

class LengthCounter {
    var counter: Int = 0
        private set

    fun addWord(word: String) {
        counter += word.length
    }
}

fun main() {
//    val user = User("yun")
//    user.address = "신림역"
//    user.address = "낙성대"
}

class Client(val name: String, val postalCode: Int) {

    override fun toString(): String {
        return "Client(name='$name', postalCode=$postalCode)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Client) return false

        if (name != other.name) return false
        if (postalCode != other.postalCode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + postalCode
        return result
    }
}

//data class Client(val name: String, val postalCode: Int)


//fun asd(){
//    A.bar() // 접근 가능
//    A.foo() // 접근 불가능
//}

//class Person2(val firstName: String, val lastName: String) {
//    override fun equals(o: Any?): Boolean {
//        val otherPerson = o as Person2 ?: return false
//        return otherPerson.firstName == firstName && otherPerson.lastName == lastName;
//    }
//}

class Person2(val age: Int? = null) {

    fun isOlderThan(other: Person2): Boolean? {

        if (age == null || other.age == null)
            return null

        return age > other.age
    }
}

fun ignoreNulls(s: String?) {
    val sNotnull: String = s!! // 예외는 이 지점을 가리킨다.
    print(sNotnull.length)
}

data class Point(val x: Int, val y: Int) : Comparable<Point> {

    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Point) return false

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun compareTo(other: Point): Int {
        return compareValuesBy(this, other, Point::x, Point::y)
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }
}


