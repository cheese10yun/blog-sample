fun main(args: Array<String>) {

    var sub1 = Sub1()

    sub1.method1()
    sub1.method2()


}


open abstract class Super2 {
    fun method1(){
        println("super2 method...")
    }

    open abstract fun method2()

}

class Sub1: Super2(){

    override fun method2() {
        println("sub1 method2")
    }

}