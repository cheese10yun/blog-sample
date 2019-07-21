fun main(args: Array<String>) {

    printDirection(Direction.NORTH)
    printDirection(Direction.SOUTH)
    printDirection(Direction.WEST)
    printDirection(Direction.EAST)

}

enum class Direction {
    NORTH, SOUTH, WEST, EAST

}

fun printDirection(a1: Direction) {
    when (a1) {
        Direction.NORTH -> println("value : ${Direction.NORTH}")
        Direction.SOUTH -> println("value : ${Direction.SOUTH}")
        Direction.WEST -> println("value : ${Direction.WEST}")
        Direction.EAST -> println("value : ${Direction.EAST}")
    }

}