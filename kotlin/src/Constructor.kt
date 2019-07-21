fun main(array: Array<String>) {

    var t1 = Test1()
    var t2 = Test2(1, 1)

    println(t2.a1)
    println(t2.a2)


    val t3 = Test3(1, 4)

    println(t3.a2)


}

class Test1 {
    init {
        println("constructor")
    }
}

class Test2 constructor(a1: Int, a2: Int) {
    var a1 = a1
    var a2 = a2
}

class Test3 constructor(a1: Int) {

    var a1 = a1
    var a2 = 3

    constructor(a1: Int, a2: Int) : this(a1) {
        this.a2 = a2

    }
}