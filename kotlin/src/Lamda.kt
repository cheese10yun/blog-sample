fun main(args: Array<String>) {
    f1(a1 = {
        print("f1이 전달한 함수가 호출되었습니다.")
    })

    f2(a1 = { x: Int, y: Int ->
        println("f2가 전달하는 함수가 호출되었습니다.")
        println("x : ${x}")
        println("y : ${y}")
    })


    f3(a1 ={x:Int, y:Int->
        x + y
    })
}

fun f1(a1: () -> Unit) {
    a1()
}

fun f2(a1: (Int, Int) -> Unit) {
    a1(100, 100)
}

fun f3(a1: (Int, Int) -> Int) {
    var a2 = a1(100,200)
    println("a2 : ${a2}");
}