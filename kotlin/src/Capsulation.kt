fun main(args: Array<String>) {
    var t1 = TestClass1()

    t1.a1 = 100


    var t2 = TestClass2()

//    t2.a1 = 1 접근할 수 없음

    t2.value1 = 100;

    val value1 = t2.value1

    println(value1)

}

class TestClass1 {
    var a1 = 0
}

class TestClass2 {
    private var a1 = 0


    var value1 : Int
        set(v1) {
            this.a1 = v1
        }
    get() {
        return this.a1
    }
}