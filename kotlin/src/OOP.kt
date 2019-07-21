fun main(args: Array<String>) {

    var t1 = Class1()
    var t2 = Class2()
    var t3 = Class2()



    println("t2.a1 : " + t2.a1)
    println("t2.a1 : " + t2.a2)

    println("t3.a1 : " + t3.a2)
    println("t3.a1 : " + t3.a2)


    t2.a1 = 100
    t2.a2 = 200

    println("t2.a1 : " + t2.a1)
    println("t2.a1 : " + t2.a2)

    println("t3.a1 : " + t3.a2)
    println("t3.a1 : " + t3.a2)




}

class Class1{

}

class Class2{

    var a1 =0;
    var a2 =0;

}

