fun main(args: Array<String>) {

    var list1 = listOf(10, 20, 30, 40)

    println(list1)


    var list3 = mutableListOf(10, 20, 30, 40)

    list3.add(10)
    list3.add(10)
    list3.add(10)


    println(list3)

}

fun max(a: Int, b: Int) = if (a > b) a else b
