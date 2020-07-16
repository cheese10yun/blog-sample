# 함수형 인터페이스와 람다 표현식

* 함수형 인터페이스
  * 추상 메서드를 딱 하나만 가직 있는 인터페이스, SAM (Single Abstract Method) 인터페이스
  * @FuncationInterface 애노테이션​을 가지고 있는 인터페이스
* 람다 표현식
  * 함수형 인터페이스의 인스턴스를 만드는 방법으로 쓰일 수 있다
  * 코드를 간략화 할 수 있다.
  * 메서드 매개변수, 리턴 타입, 변수로 만들어 사용할 수 있다.
* 자바에서 함수형 프로그래밍
  * 함수를 First class object로 사용할 수 있다.
  * 순수 함수 (Pure function)
    * 사이드 이펙트가 없다.(함수 밖에 있는 값을 변경하지 않는다.)
    * 상태가 없다. (함수 밖에 있는 값을 사용하지 않는다.)
  * 고차 함수
    * 함수가 함수를 매개변수로 받을 수 있고 함수를 리턴할 수도 있다.
  * 불변성

```kotlin
interface RunSomething {
    fun doit(): Unit
}

class Kfoo {
    fun bar() {
        val runSomething = object : RunSomething {
            override fun doit() {
                println("123")
            }
        }
    }
}
```

# 자바에서 제공하는 함수형 인터페이스

## Function<T, R>
* T타입을받아서R타입을리턴하는함수인터페이스

```kotlin
@Test
internal fun Function() {
    val plus10 = Function<Int, Int> { it + 10 }
    val multiply2 = Function<Int, Int> { it * 2 }

    println("compose:  ${plus10.compose(multiply2).apply(2)}") // (2 * 2) + 10
    println("andThen:  ${plus10.andThen(multiply2).apply(2)}") // (10 + 2) * 2
}
```

