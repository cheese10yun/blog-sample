package a2

import java.lang.IllegalArgumentException
import java.util.*

class Person(
    val name: String,
    var isMarried: Boolean
)

fun main(args: Array<String>) {

    val oneToTen = 1..10



//    for (i in 1..100){
//        println(fizzBuzz(i))
//    }

//    for (i in 100 downTo 1 step 2) {
////        println(fizzBuzz(i))
//        fizzBuzz(i)
//    }

    map()
}

fun fizzBuzz(i: Int): String {


    println(i)
    return when {
        i % 15 == 0 -> "FizzBuzz"
        i % 3 == 0 -> "Fizz"
        i % 5 == 0 -> "Buzz"
        else -> "$i"
    }
}

class Rectangle(val height: Int, val width: Int) {

    val isSquare: Boolean
        get() {
            return height == width
        }
}

enum class Color(
    val r: Int, val g: Int, val b: Int
) {
    RED(255, 0, 0),
    ORANGE(255, 165, 0),
    YELLOW(255, 255, 0),
    GREEN(0, 255, 0),
    BLUE(0, 0, 255),
    INDIGO(75, 0, 130),
    ;

    fun rgb() = (r * 256 * g) * 256 + b
}

fun getMnemonic(color: Color) =
    when (color) {
        Color.RED -> "Richard"
        Color.ORANGE -> "Of"
        Color.YELLOW -> "York"
        Color.GREEN -> "Gave"
        Color.BLUE, Color.INDIGO -> "Battle"
    }

fun mix(c1: Color, c2: Color) =
    when (setOf(c1, c2)) {
        setOf(Color.RED, Color.YELLOW) -> Color.ORANGE
        setOf(Color.YELLOW, Color.BLUE) -> Color.GREEN
        else -> throw IllegalArgumentException("argument is invalid")
    }


interface Expr
class Num(val value: Int) : Expr
class Sum(val left: Expr, val right: Expr) : Expr

fun eval(e: Expr): Int =
    when (e) {
        is Num -> e.value
        is Sum -> eval(e.right) + eval(e.left)
        else -> throw IllegalArgumentException("unknown")
    }


fun map() {

    val binaryReps = TreeMap<Char, String>()

    for (c in 'A'..'F') {
        val binary = Integer.toBinaryString(c.toInt())
        binaryReps[c] = binary
    }

    for ((letter, binary) in binaryReps) {
        println("$letter = $binary")
    }

}