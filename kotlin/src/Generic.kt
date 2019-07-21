fun main(args: Array<String>){

    var t1 = Ttt<String>()

    t1.test_method("1123123")


    var t2 = Ttt<Int>()
    t2.test_method(111)


}

class Ttt<T>{
    fun test_method(a1: T){
        println("a1 : ${a1}")
    }
}