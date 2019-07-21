fun main(args: Array<String>) {
    f1()
    f2(1, 1.1)
    f3(1, 1.1)
    f3(a2 = 0.11)
    f4()
    val f5 = f5()
    println(f5)
    f6(1.1)
    f7()

}

fun f1() {
    println("f1 run")
}

fun f2(a1: Int, a2: Double) {
    println(a1)
    println(a2)
}

fun f3(a1: Int = 0, a2: Double = 0.0) {
    println(a1)
    println(a2)

}

fun f4(): Unit {
    println("no return")

}

fun f5(): Int {
    return 5
}

fun f6() {
    println("f6")
}

fun f6(a1: Int) {
    println("f6")
}

fun f6(a1: Double) {
    println("f6")
}

fun f6(a1: Int, a2: Int) {
    println("f6")
}

fun f7(){
    fun f8(){
        println("f8")
    }
}