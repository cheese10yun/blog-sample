fun main(args: Array<String>){

    var yun = Yun()

    yun.method1()
    yun.method2()

}

interface Inter1{
    fun method1()
    fun method2(){
        println("interface1 method2")
    }
}

interface Inter2{
    fun method3()
    fun method4(){
        println("interface1 method4")
    }
}

fun test_method(a1: Inter1){
    a1.method1()
    a1.method2()
}

fun test_method(a1: Inter2){
    a1.method3()
    a1.method4()
}

class Yun : Inter1 {

    override fun method1() {
        println("yun method1")
    }

    override fun method2() {
        println("yun method2")
    }

}