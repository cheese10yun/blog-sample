# 좋은 캡슐화란 무엇인가 - private 생성자와 invoke를 활용한 객체 설계

객체를 설계할 때 가장 중요한 원칙 중 하나는 캡슐화입니다. 캡슐화가 잘 된 객체는 내부 로직을 외부로부터 숨기고, 외부에서는 정해진 방법으로만 객체를 생성하고 사용할 수 있게 합니다. 이번 포스팅에서는 커서 기반 페이지네이션 응답 객체인 `CursorPageResponse`를 예시로, 좋은 캡슐화가 무엇인지 구체적으로 살펴봅니다.

## CursorPageResponse 설계

`CursorPageResponse`는 커서 기반 페이지네이션의 응답을 담는 객체입니다. 코드를 보면 생성자가 `private`으로 선언되어 있습니다.

```kotlin
data class CursorPageResponse<T> private constructor(
    val content: List<T>,
    val hasNext: Boolean,
    val hasPrev: Boolean,
    val nextCursor: T?,
    val prevCursor: T?,
)
```

외부에서 직접 생성자를 호출할 수 없고, `companion object`의 `invoke` 팩토리 함수를 통해서만 객체를 생성할 수 있습니다.

```kotlin
companion object {
    operator fun <T> invoke(
        content: List<T>,
        direction: CursorDirection,
        pageSize: Int,
    ): CursorPageResponse<T> {
        // ... 내부 로직
    }
}
```

Kotlin에서 `companion object`에 `operator fun invoke`를 정의하면 마치 생성자처럼 `CursorPageResponse(...)` 형태로 호출할 수 있습니다. 외부에서 보면 생성자처럼 보이지만, 실제로는 `invoke` 팩토리 함수를 통해 생성됩니다.

## 받는 파라미터와 저장되는 필드가 다르다

이 설계의 핵심은 **`invoke`가 받는 파라미터**와 **data class에 저장되는 필드**가 다르다는 점입니다.

`invoke`가 받는 파라미터:

```kotlin
operator fun <T> invoke(
    content: List<T>,          // 조회된 raw 데이터 (pageSize + 1 개)
    direction: CursorDirection, // 페이지 이동 방향
    pageSize: Int,             // 페이지 크기
)
```

실제 data class에 저장되는 필드:

```kotlin
data class CursorPageResponse<T> private constructor(
    val content: List<T>,  // 실제 노출할 데이터 (pageSize 만큼 잘라낸 결과)
    val hasNext: Boolean,  // 다음 페이지 존재 여부
    val hasPrev: Boolean,  // 이전 페이지 존재 여부
    val nextCursor: T?,    // 다음 페이지 커서 (해당 항목 자체)
    val prevCursor: T?,    // 이전 페이지 커서 (해당 항목 자체)
)
```

파라미터 `content`, `direction`, `pageSize`를 받아 `hasNext`, `hasPrev`, `nextCursor`, `prevCursor`를 계산하는 **핵심 로직이 `invoke` 내부에 캡슐화**되어 있습니다. 예를 들어 다음과 같은 규칙들이 `invoke` 내부에서 처리됩니다.

- `NEXT` 방향이면 `hasPrev`는 항상 `true`
- `LAST` 방향이면 `hasNext`는 항상 `false`
- backward 방향(`LAST`, `PREV`)은 DB에서 ASC로 조회한 결과를 `reversed()`해서 반환
- `pageSize + 1`개를 조회한 결과에서 초과분으로 다음/이전 페이지 존재 여부 판단

이런 규칙들이 모두 `invoke` 안에 있기 때문에 외부에서는 이 로직을 신경 쓸 필요가 없습니다.

## 생성자가 열려 있다면 어떤 문제가 생길까

만약 생성자가 `public`이었다면 외부에서 직접 이렇게 생성할 수 있습니다.

```kotlin
// 생성자가 public인 경우, 외부에서 직접 생성 가능
val response = CursorPageResponse(
    content = payments,
    hasNext = ???,      // 어떻게 계산해야 하지?
    hasPrev = ???,      // 방향에 따라 다른데...
    nextCursor = ???,   // 어떤 항목을 넣어야 하지?
    prevCursor = ???,
)
```

이 상황에서 발생하는 문제는 여러 가지입니다.

**1. 어떤 생성자를 써야 할지 모른다**

두 생성자가 모두 열려 있다면 `private constructor`로 직접 생성해야 할지, `invoke`를 통해 생성해야 할지 혼란이 생깁니다. 직접 생성자를 사용하면 내부의 복잡한 계산 로직을 거치지 않아 잘못된 상태의 객체가 만들어질 수 있습니다.

**2. 객체의 핵심 로직이 외부로 새어나간다**

`CursorPageResponse`를 생성하는 모든 곳에서 `hasNext`, `hasPrev`, `nextCursor`, `prevCursor`를 직접 계산해야 합니다. 예를 들어 한 곳에서 `hasPrev = direction == CursorDirection.NEXT`라는 규칙을 알고 구현했다면, 다른 곳에서도 동일한 규칙을 알고 구현해야 합니다. `CursorPageResponse`의 핵심 로직이 해당 객체에서 책임지지 않고 그 객체를 생성하는 모든 곳으로 분산됩니다.

**3. 규칙 위반 객체를 만들 수 있다**

더 이상 다음 페이지가 없는 상황임에도 `hasNext = true`로 생성하는 것을 막을 방법이 없습니다. 생성자가 열려 있으면 객체가 스스로 자신의 불변 규칙을 보장할 수 없습니다.

## 테스트 코드가 주는 피드백

캡슐화가 잘 되어 있지 않다는 느낌은 테스트 코드를 작성할 때 자연스럽게 받을 수 있습니다.

만약 `CursorPageResponse`의 생성자가 `public`이었다면 테스트를 이렇게 작성해야 합니다.

```kotlin
@Test
fun `NEXT 방향으로 다음 페이지가 있는 경우`() {
    val payments = listOf(payment1, payment2, payment3)
    val pageSize = 2

    // 생성자가 public이면 테스트 작성자가 직접 이 로직을 구현해야 한다
    val hasNext = payments.size > pageSize
    val actualContent = payments.take(pageSize)
    val hasPrev = true // NEXT 방향이므로
    val response = CursorPageResponse(
        content = actualContent,
        hasNext = hasNext,
        hasPrev = hasPrev,
        nextCursor = actualContent.last().id.toString(),
        prevCursor = actualContent.first().id.toString(),
    )

    then(response.hasNext).isTrue()
    // ...
}
```

테스트를 작성하다 보면 자연스럽게 이런 생각이 듭니다.

> "이 `hasNext` 계산 로직을 내가 왜 여기서 하고 있지?"
> "이건 `CursorPageResponse`가 알아야 하는 거 아닌가?"
> "테스트마다 이 로직을 반복해서 짜는 게 맞나?"

반면 `private constructor` + `invoke` 설계에서는 테스트가 이렇게 바뀝니다.

```kotlin
@Test
fun `NEXT - content가 pageSize 초과이면 hasNext true, hasPrev true`() {
    // given
    val content = listOf(1, 2, 3, 4) // pageSize + 1
    val pageSize = 3

    // when
    val response = CursorPageResponse(content, CursorDirection.NEXT, pageSize)

    // then
    then(response.hasNext).isTrue()
    then(response.hasPrev).isTrue()
    then(response.nextCursor).isEqualTo(3)
    then(response.prevCursor).isEqualTo(1)
}
```

테스트 작성자는 `CursorPageResponse`의 내부 로직을 몰라도 됩니다. `content`, `direction`, `pageSize`를 넘기면 올바른 상태의 객체가 만들어진다는 것만 알면 됩니다. 결과적으로 테스트가 단순해지고, 책임이 명확히 분리됩니다.

**테스트 코드는 설계에 대한 피드백 도구입니다.** 테스트를 작성하기 어렵거나, 테스트 준비 코드에서 이미 해당 객체의 핵심 로직을 구현하고 있다면 그것은 캡슐화가 제대로 되지 않았다는 신호입니다.

## 결론

`CursorPageResponse`의 설계를 통해 좋은 캡슐화의 두 가지 핵심을 확인할 수 있습니다.

1. **객체는 자신의 상태를 스스로 결정해야 한다.** `hasNext`, `hasPrev`, `nextCursor`, `prevCursor`를 외부에서 주입받지 않고, `invoke` 내부에서 `direction`과 `pageSize`를 기반으로 스스로 계산합니다.

2. **생성 방법을 하나로 강제하라.** `private constructor` + `invoke` 조합으로 외부에서는 오직 올바른 방법으로만 객체를 생성할 수 있게 합니다. 두 가지 생성 방법이 열려 있으면 어느 것을 써야 할지 혼란이 생기고, 잘못된 상태의 객체가 만들어질 여지가 생깁니다.

캡슐화가 잘 된 객체는 사용하기 쉽고, 테스트 작성도 쉬우며, 로직이 분산되지 않습니다. 객체를 설계할 때 "이 로직이 정말 이 객체의 책임인가?"를 스스로에게 물어보는 습관을 들이면, 자연스럽게 좋은 캡슐화로 이어집니다.
