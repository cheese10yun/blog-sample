package a4

interface Clickable {
    fun click() // 일반 메서드 선언
    fun showOff() = println("Im clickable!") // 디폴트 구현이 있는 메서드
}

class Button : Clickable {
    override fun click() = println("I was clicked")
}

open class RichButton : Clickable {
    fun disable() {} // 이 함수는 final이다. 하위 클래스가 이 메서드를 오버라이드할 수 없다.

    open fun animate() {} // 이 함수는 open이다. 하위 클래스에서 오버라이드 할 수 있다.

    override fun click() {} // 이 함수는 열려있는 메서드를 오버라이드한다. 오버라이드한 메서드는 기본적으로 open 이다.
}


abstract class Animated { // 이 클래스는 추상클래스다. 이 클래스의 인스턴스를 만들 수 없다.

    abstract fun animate() // 이함수는 추상 함수다. 이 함수에는 구현이 없다 하위 클래스에서는 이 함수를 반드시 오버라이드해야 한다.

    open fun stopAnimating() {} // 추상 클래스에 속했더라도 비추상 함수는 기본적으로 final이지만 open으로 오버라이드를 허용할 수 있다.

    fun animateTwice() {} // 추상 클래스 함수는 기본적으로 final이다.

}

internal open class TalkativeButton {
    private fun yell() = println("Hey!")

    protected fun whisper() = print("Let talk")
}

fun TalkativeButton.giveSpeech() {
    yell()
    whisper()
}