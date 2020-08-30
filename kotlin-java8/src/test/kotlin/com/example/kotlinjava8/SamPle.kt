package com.example.kotlinjava8

import com.example.kotlinjava8.Apple.Companion.filterApples
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.FileReader
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors
import java.util.stream.Collectors.toList


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

        apples.sortedBy { it.weight }
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
    internal fun `Consumer`() {
        forEach(
            listOf(1, 2, 3, 4, 5),
            Consumer {
                println(it)
            }
        )
    }

    @Test
    internal fun `function`() {

        val map = map(
            listOf(1, 2, 3, 4),
            Function(Int::toString)
        )!!
    }

    @Test
    internal fun `메서드 참조`() {
        val listOf = listOf("1", "2", "3", "4")
    }

    @Test
    internal fun `딱 한 번만 탐색할 수 있다`() {
        val stream = listOf("a", "b", "c", "d").stream()

        stream.forEach(System.out::println)
        stream.forEach(System.out::println)
    }

    @Test
    internal fun `컬렉션 내부 반복`() {
        val menus = listOf(
            Menu("치킨", false, 100, Type.MEAT),
            Menu("피자", false, 200, Type.OTHER),
            Menu("짜장면", false, 300, Type.OTHER)
        )
        val menuNames = mutableListOf<String>()

        for (menu in menus) {
            menuNames.add(menu.name)
        }

    }

    @Test
    internal fun `스트림 내부 반복`() {
        val menus = listOf(
            Menu("치킨", false, 100, Type.MEAT),
            Menu("피자", false, 200, Type.OTHER),
            Menu("짜장면", false, 300, Type.OTHER)
        )

        menus.stream()
            .map { it.name } // map 메서드를 it.name 메서드로 파라미터화해서 요리명을 춫ㄹ
            .collect(toList()) // 파이프라인을 실행한다. 반복자는 필요 없다.
    }

    @Test
    internal fun `TAKEWHILE 활용`() {
        val menus = listOf(
            Menu("치킨", false, 100, Type.MEAT),
            Menu("피자", false, 200, Type.OTHER),
            Menu("짜장면", false, 300, Type.OTHER),
            Menu("탕수육", false, 400, Type.OTHER),
            Menu("돈까스", false, 500, Type.MEAT),
            Menu("비빔밥", false, 600, Type.OTHER),
            Menu("참치", false, 100, Type.OTHER)
        )

        val collect = menus.stream()
//            .filter { it.calories < 320 }
//            .takeWhile { it.calories < 320 }
            .dropWhile { it.calories < 320 }
            .collect(toList())

        println(collect)
    }

    @Test
    internal fun `flatMap`() {

        val listOf = listOf("Hello", "World")

        val collect = listOf.stream()
            .map { it.split("") }
            .distinct()
            .collect(toList())

        println(collect)

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

    fun <T, R> map(list: List<T>, f: Function<T, R>): List<R>? {
        val result: MutableList<R> = ArrayList()
        for (t in list) {
            result.add(f.apply(t))
        }
        return result
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
