fun main(args: Array<String>) {


    var array1 = arrayOf(10, 20, 30, 40, 50)
    println("array1[0]" + array1[0])
    println("array1[1]" + array1[1])

    for (value in array1) {
        println("${value}")
    }

    var array2 = Array(5, { a -> a })

    for (value in array2) {
        println("${value}")
    }


    var array3 = Array(5, { a -> a * 2 })

    for (value in array3) {
        println("${value}")
    }

    println(array3.size)

    var a1 = array1.get(1)
    var a2 = array1.get(2)

    println("${a1} ${a2}")


    var array6 = array1.copyOfRange(0, 1)

    println("${array6[0]}")
}