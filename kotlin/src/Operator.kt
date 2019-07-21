fun main(args: Array<String>) {


    var a1 = 10
    var a2 = -a1

    println("a2 = ${a2}")


    a2 = a1.unaryMinus()

    println("a2  = ${a2}")


    var a3 = true
    var a4 = !a3

    println("a4:${a4}")

    a4 = a3.not()
    println("a4:${a4}")


    var a5 = 10
    var a6 = 10

    var a7 = a5++
    var a8 = a6--


    println("a5 : ${a5}, ${a7}")
    println("a6 : ${a6}, ${a8}")


    a5 = 10
    a6 = 10
    a7 = ++a5
    a8 = --a6

    println("a5 : ${a5}, ${a7}")
    println("a6 : ${a6}, ${a8}")

}