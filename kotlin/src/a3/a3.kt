package a3

fun String.lastChar(): Char = this.get(this.length - 1)

fun main() {

    print("kotlin".lastChar())


    val hashSetOf = hashSetOf(1, 7, 53)
    println(hashSetOf) // [1, 53, 7]


    val arrayListOf = arrayListOf(1, 7, 53)
    println(arrayListOf) // [1, 7, 53]

    val hashMapOf = hashMapOf(1 to "one", 7 to "seven", 53 to "fifty-three")
    println(hashMapOf) // {1=one, 53=fifty-three, 7=seven}

    println(hashSetOf.javaClass) // class java.util.HashSet
    println(arrayListOf.javaClass) // class java.util.ArrayList
    println(hashMapOf.javaClass) // class java.util.HashMap


    val strings = listOf("first", "second", "third")
    println(strings.last()) // third

    val numbers = setOf(1, 14, 2)
    println(numbers.max()) // 14

    println(joinToString(arrayListOf, "; ", "(", ")"))
    println(joinToString(arrayListOf, separator = "; ", prefix = "(", postfix = ")"))

    println(joinToString(arrayListOf, separator = ", "))
    println(joinToString(arrayListOf, separator = ", ")) // 1, 7, 53
    println(joinToString(arrayListOf, separator = "; ")) // 1; 7; 53


    print(arrayListOf.joinToString2("; ", "#", "@"))


}

fun <T> joinToString(
    collection: Collection<T>,
    separator: String = ", ",
    prefix: String = "",
    postfix: String = ""
): String {
    val result = StringBuilder(prefix)

    for ((index, element) in collection.withIndex()) {
        if (index > 0) result.append(separator)
        result.append(element)
    }

    result.append(postfix)

    return result.toString()
}


fun <T>Collection<T>.joinToString2(
    separator: String = ", ",
    prefix: String = "",
    postfix: String = ""
): String {
    val result = StringBuilder(prefix)

    for ((index, element) in this.withIndex()) {
        if (index > 0) result.append(separator)
        result.append(element)
    }

    result.append(postfix)

    return result.toString()
}





