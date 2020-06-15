# Junit5

## Instance 특징

Junit5는 테스트 메서드마다 인스턴스를 새로 생성하는 것이 기본 전략입니다. 이는 테스트 코드(메서드) 간의 디펜던시 줄이기 위해서입니다. 아래 코드를 통해서 살펴보겠습니다.

```kotlin
internal class Junit5 {

    private var value = 0
    
    @Test
    internal fun `value add 1`() {
        value++

        println("value : $value")
        println("Junit5 : $this")
    }

    @Test
    internal fun `value add 2`() {
        value++

        println("value : $value")
        println("Junit5 : $this")
    }
}
```
각가의 테스트 코드마다 `value`을 증가시키고 있습니다. `value add 1`, `value add 2` 두 메서드 중 한 메서드에서는 `value : 2`가 출력되어야 합니다. 하지만 출력 결과를 확인해 보면 모두 `value : 1`의 값이 출력되는 것을 확인할 수 있습니다.

![](images/junit5-instance-1.png)

그 이유는 위에서 언급했듯이 Junit5는 테스트 메서드마다 인스턴스를 새로 생성하는 것이 기본 전략이기 때문에 `value` 값이 초기화되는 것입니다. `Junit5 : $this` 출력을 확인해보면 인스턴스 주솟값이 다른 것을 확인할 수 있습니다. 이렇게 되면 `value add 1`, `value add 2` 테스트 메서드 간에 디펜더시가 줄어들게 됩니다.

만약 테스트 메서드마다 해당 값을 공유해서 사용하고 싶으면 `companion object (static)`으로 변수를 지정하는 것입니다. 아레 코드처럼 변경하고 테스트 코드를 실행해보면 다음과 같은 결과를 확인할 수 있습니다.

```kotlin
internal class Junit5 {

    // private var value = 0

        companion object {
            private var value = 0
    }
    ...
}
```

![](images/junit5-instance-2.png)

`value : $value`의 출력을 확인해보면 해당 값이 증가된 것을 확인할 수 있습니다. 또 Junit5 인스턴스가 계속 생성되는 것을 방지하고 싶은 경우에는 `@TestInstance(TestInstance.Lifecycle.PER_CLASS)` 에노테이션을 사용하면 됩니다.

```kotlin
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class Junit5 {

    private var value = 0

    @Test
    internal fun `value add 1`() {
        value++

        println("value : $value")
        println("Junit5 : $this")
    }

    @Test
    internal fun `value add 2`() {
        value++

        println("value : $value")
        println("Junit5 : $this")
    }
}
```

![](images/junit5-instance-3.png)

`Junit5` 주솟값을 보면 동일한 주솟값을 출력하는 것을 확인할 수 있습니다. 그 결과 `private var value` 변숫값이 테스트 메서드에서 공유되는 것을 확인할 수 있습니다. 물론 테스트 코드 간의 디펜더시를 줄이는 것이 올바른 테스트 방식이라고 생각합니다. **하지만 테스트 메서드마다 인스턴스를 계속 생성하는 것이 효율적이지 않다고 생각합니다.** 테스트 코드는 디펜던시ㄷ 없이 작성하고, `@TestInstance(TestInstance.Lifecycle.PER_CLASS)`을 통해서 인스턴스를 계속 생성을 막는 것도 좋은 방법이라고 생각합니다.