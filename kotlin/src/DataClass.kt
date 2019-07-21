fun main(args: Array<String>) {

    var d1 = DataClass1(100, "str1")

    println("d1.a1 = ${d1.a1}")
    println("d1.a2 = ${d1.a2}")

}

data class DataClass1(var a1 : Int, var a2 : String)