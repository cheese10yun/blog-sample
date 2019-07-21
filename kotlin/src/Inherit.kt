fun main(args: Array<String>){

    var s1 = Sub()

    println("s1.sub_member : ${s1.sub_member}")

    s1.sub_method()

    println("s1.super_member : ${s1.super_member}")

    s1.sub_method()

}

open class Super1 {

    var super_member = 100

    fun super_method(){
        println("super1 method")
    }
}

class Sub: Super1{

    var sub_member =200
    constructor(): super()

    fun sub_method(){
        println("sub method")
    }




}