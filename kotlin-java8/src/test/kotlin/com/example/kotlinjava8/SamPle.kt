package com.example.kotlinjava8

import com.example.kotlinjava8.Apple.Companion.filterApples
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.FileReader
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.stream.Collectors


class SamPle {

    val apples = listOf(
        Apple(10, Color.GREEN),
        Apple(10, Color.GREEN),
        Apple(180, Color.GREEN),
        Apple(230, Color.GREEN),
        Apple(832, Color.GREEN),
        Apple(11, Color.GREEN),
        Apple(23, Color.GREEN)
    )

    @Test
    fun name() {
        filterApples(apples, AppleHeavyWeightPredicate())
            .forEach {
                println(it)
            }

        filterApples(apples, object : ApplePredicate<Apple> {
            override fun test(t: Apple): Boolean {
                return Color.READ == t.color
            }
        })
    }

    @Test
    internal fun asd() {
        apples.sortedBy { it.weight }
            .listIterator()
            .forEach { println(it) }
    }

    @Test
    internal fun asdasdsad() {
        Comparator<Apple> { apple1, appl2 -> apple1.weight.compareTo(appl2.weight) }
        val r2 = Runnable { println("1") }
    }

    @Test
    internal fun `execute around pattern`() {


        forEach(
            listOf(1, 2, 3, 4, 5),
            Consumer {
                println(it)
            }
        )

    }

    fun <T> filter(list: List<T>, p: Predicate<T>?): List<T> {
        return list.stream()
            .filter(p)
            .collect(Collectors.toList())
    }

    private fun <T> forEach(list: List<T>, c: Consumer<T>) {
        for (t in list) {
            c.accept(t)
        }
    }
}

enum class Color {
    READ, GREEN
}

interface ApplePredicate<T> {
    fun test(t: T): Boolean
}

interface AppleFormatter {
    fun accept(apple: Apple): String
}

data class Apple(
    var weight: Int,
    var color: Color
) {
    companion object {

        fun processFile(): String {
            BufferedReader(FileReader("data.txt")).use { br -> return br.readLine() }
        }

        fun filterApples(inventory: List<Apple>, predicate: ApplePredicate<Apple>): List<Apple> {
            return inventory.filter { predicate.test(it) }
        }

        fun prettyPrintApple(inventory: List<Apple>, formatter: AppleFormatter) {
            for (apple in inventory) {
                println(formatter.accept(apple))
            }
        }
    }
}

class AppleHeavyWeightPredicate : ApplePredicate<Apple> {
    override fun test(apple: Apple): Boolean {
        return apple.weight > 150
    }
}


class AppleFancyFormatter : AppleFormatter {
    override fun accept(apple: Apple): String {
        val s = when {
            apple.weight > 150 -> "heavy"
            else -> "light"
        }
        return "A $s ${apple.color} apple"
    }
}
