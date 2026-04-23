# 테스트가 어렵다면 캡슐화를 의심하라

테스트를 작성하다 보면 어느 순간 이런 상황을 마주칠 때가 있습니다. 테스트 본문보다 준비 코드가 더 길고, 케이스를 추가할수록 동일한 계산 로직이 반복됩니다. "이게 맞나?" 싶은 찜찜함이 드는 순간입니다.

이 불편함은 테스트 코드가 미숙한 탓이 아닙니다. **테스트가 어렵다는 것 자체가 설계에 대한 피드백입니다.** 더 구체적으로는 캡슐화가 제대로 되지 않았다는 신호일 가능성이 높습니다.

이번 포스팅에서는 커서 기반 페이지네이션 응답 객체인 `CursorPageResponse`를 예시로, 생성자가 열려 있을 때 테스트가 어떻게 무너지는지, 그리고 `private constructor` + `invoke` 설계로 캡슐화를 강제했을 때 테스트가 어떻게 단순해지는지를 살펴봅니다.

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

`invoke` 내부 로직은 다음과 같습니다.

```kotlin
operator fun <T> invoke(
    content: List<T>,
    direction: CursorDirection,
    pageSize: Int,
): CursorPageResponse<T> {
    if (content.isEmpty()) {
        return CursorPageResponse(
            content = emptyList(),
            hasNext = false,
            hasPrev = false,
            nextCursor = null,
            prevCursor = null,
        )
    }

    return if (direction.isForward) {
        // forward(FIRST/NEXT): DB에서 DESC로 조회한 결과를 그대로 사용
        val hasNext = content.size > pageSize
        val actualContent = content.take(pageSize)
        val hasPrev = direction == CursorDirection.NEXT
        CursorPageResponse(
            content = actualContent,
            hasNext = hasNext,
            hasPrev = hasPrev,
            nextCursor = if (hasNext) actualContent.last() else null,
            prevCursor = if (hasPrev) actualContent.first() else null,
        )
    } else {
        // backward(LAST/PREV): DB에서 ASC로 조회한 결과를 reversed()해서 반환
        val hasPrev = content.size > pageSize
        val actualContent = content.take(pageSize).reversed()
        val hasNext = direction == CursorDirection.PREV
        CursorPageResponse(
            content = actualContent,
            hasNext = hasNext,
            hasPrev = hasPrev,
            nextCursor = if (hasNext) actualContent.last() else null,
            prevCursor = if (hasPrev) actualContent.first() else null,
        )
    }
}
```

direction별로 동작 방식이 다릅니다.

| direction | content 처리 | hasNext | hasPrev |
|:----------|:------------|:--------|:--------|
| FIRST | 그대로 사용 | pageSize 초과 여부 | 항상 false |
| NEXT  | 그대로 사용 | pageSize 초과 여부 | 항상 true  |
| LAST  | reversed() | 항상 false | pageSize 초과 여부 |
| PREV  | reversed() | 항상 true  | pageSize 초과 여부 |

`applyCursorPagination`이 `pageSize + 1`개를 조회해서 넘기면, `invoke` 내부에서 초과분 유무로 다음/이전 페이지 존재를 판단하고 `take(pageSize)`로 실제 노출할 데이터를 잘라냅니다. 커서(`nextCursor`, `prevCursor`)는 잘라낸 결과의 첫 번째 또는 마지막 항목을 저장합니다.

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
    val nextCursor: T?,    // 다음 페이지 커서
    val prevCursor: T?,    // 이전 페이지 커서
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

생성자와 `invoke` 두 경로가 모두 열려 있다면 어느 쪽을 써야 하는지 혼란이 생깁니다. 생성자를 직접 사용하면 내부 계산 로직을 거치지 않아 잘못된 상태의 객체가 만들어질 수 있습니다.

**2. 객체의 핵심 로직이 외부로 새어나간다**

`CursorPageResponse`를 생성하는 모든 곳에서 `hasNext`, `hasPrev`, `nextCursor`, `prevCursor`를 직접 계산해야 합니다. `hasPrev = direction == CursorDirection.NEXT`라는 규칙을 한 곳에서 구현했다면, 다른 곳에서도 동일한 규칙을 알고 구현해야 합니다. `CursorPageResponse`가 책임져야 할 로직이 해당 객체를 생성하는 모든 곳으로 분산됩니다.

**3. 규칙 위반 객체를 만들 수 있다**

더 이상 다음 페이지가 없는 상황임에도 `hasNext = true`로 생성하는 것을 막을 방법이 없습니다. 생성자가 열려 있으면 객체가 스스로 자신의 불변 규칙을 보장할 수 없습니다.

## 테스트 코드가 주는 피드백

생성자가 열려 있을 때 발생하는 문제는 테스트를 작성하는 순간 가장 직접적으로 드러납니다. 테스트가 어렵다면, 그것은 캡슐화가 제대로 되지 않았다는 신호입니다.

### 테스트 준비 코드가 곧 로직 중복이다

생성자가 `public`이었다면, NEXT 방향 테스트 하나를 이렇게 작성해야 합니다.

```kotlin
@Test
fun `NEXT 방향으로 다음 페이지가 있는 경우`() {
    val payments = listOf(payment1, payment2, payment3)
    val pageSize = 2

    // 테스트 작성자가 직접 계산 로직을 구현해야 한다
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

문제는 하나의 테스트로 끝나지 않는다는 데 있습니다. FIRST, NEXT, PREV, LAST 각 방향별로, 다음 페이지 있음/없음, 이전 페이지 있음/없음 등의 경우를 모두 테스트해야 합니다. 그 결과 테스트마다 동일한 계산 로직이 반복됩니다.

```kotlin
@Test
fun `FIRST 방향으로 다음 페이지가 있는 경우`() {
    val hasNext = payments.size > pageSize       // 중복
    val actualContent = payments.take(pageSize)  // 중복
    val hasPrev = false                          // FIRST 방향이므로
    val response = CursorPageResponse(content = actualContent, hasNext = hasNext, ...)
}

@Test
fun `PREV 방향으로 이전 페이지가 있는 경우`() {
    val hasPrev = payments.size > pageSize               // 중복
    val actualContent = payments.take(pageSize).reversed() // 중복
    val hasNext = true                                   // PREV 방향이므로
    val response = CursorPageResponse(content = actualContent, hasPrev = hasPrev, ...)
}
```

이 로직 중복은 단순한 코드 반복이 아닙니다. **`CursorPageResponse` 내부에 있어야 할 규칙이 테스트 코드 전반에 분산된 것**입니다. 이 상태에서는 `CursorPageResponse`를 테스트하는 게 아니라, 테스트 작성자가 직접 그 규칙을 재구현하고 있는 셈입니다.

### 로직이 변경되면 테스트도 함께 깨진다

캡슐화가 깨진 설계의 가장 큰 문제는 **변경에 취약하다**는 점입니다.

예를 들어 `nextCursor`를 항목 자체(`T`)가 아니라 항목의 ID(`String`)로 인코딩하는 방식으로 스펙이 바뀐다고 가정합니다.

```kotlin
// 변경 전: 항목 자체를 커서로
nextCursor = actualContent.last()

// 변경 후: ID를 문자열로 인코딩
nextCursor = actualContent.last().id.toString()
```

`private constructor` + `invoke` 설계라면 `invoke` 내부 한 곳만 수정하면 됩니다. 하지만 생성자가 열려 있어 각 테스트가 커서를 직접 계산하고 있다면, 이 규칙이 반영된 모든 테스트를 찾아 수정해야 합니다. 수정이 누락된 테스트는 잘못된 기댓값을 갖게 되어 **테스트가 통과해도 실제 동작이 틀릴 수 있는 상황**이 만들어집니다.

### 캡슐화 후 테스트는 어떻게 바뀌는가

`private constructor` + `invoke` 설계에서는 테스트가 이렇게 바뀝니다.

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

@Test
fun `PREV - content가 pageSize 초과이면 hasPrev true, 결과는 reversed`() {
    // given
    val content = listOf(1, 2, 3, 4) // ASC로 조회된 결과 (pageSize + 1)
    val pageSize = 3

    // when
    val response = CursorPageResponse(content, CursorDirection.PREV, pageSize)

    // then
    then(response.hasPrev).isTrue()
    then(response.hasNext).isTrue()
    then(response.content).isEqualTo(listOf(3, 2, 1)) // reversed
    then(response.prevCursor).isEqualTo(3)
    then(response.nextCursor).isEqualTo(1)
}
```

각 테스트는 `content`, `direction`, `pageSize`만 넘기면 됩니다. 계산 로직은 `invoke` 안에 있고, 테스트는 **입력과 기대 결과만 서술하는 구조**가 됩니다. `nextCursor` 인코딩 방식이 바뀌어도 `invoke` 내부만 수정하면 테스트는 기댓값만 고치면 됩니다.

테스트 작성자는 `CursorPageResponse`의 내부 로직을 몰라도 됩니다. `content`, `direction`, `pageSize`를 넘기면 올바른 상태의 객체가 만들어진다는 것만 알면 됩니다.

## 결론

**테스트가 어렵다면 캡슐화를 의심하라.** 테스트 준비 코드가 길어지거나 테스트마다 같은 계산 로직이 반복된다면, 그것은 테스트 코드의 문제가 아니라 해당 로직이 올바른 위치에 있지 않다는 신호입니다.

`CursorPageResponse`의 설계를 통해 확인한 것처럼, 캡슐화가 잘 된 객체는 두 가지를 보장합니다.

1. **객체는 자신의 상태를 스스로 결정한다.** `hasNext`, `hasPrev`, `nextCursor`, `prevCursor`를 외부에서 주입받지 않고, `invoke` 내부에서 `direction`과 `pageSize`를 기반으로 스스로 계산합니다.

2. **생성 방법을 하나로 강제한다.** `private constructor` + `invoke` 조합으로 외부에서는 오직 올바른 방법으로만 객체를 생성할 수 있게 합니다. 두 가지 생성 경로가 열려 있으면 어느 것을 써야 할지 혼란이 생기고, 잘못된 상태의 객체가 만들어질 여지가 생깁니다.

객체를 설계할 때 "이 로직이 정말 이 객체의 책임인가?"를 스스로에게 물어보는 것도 좋지만, 테스트를 작성하면서 느끼는 불편함 자체가 더 직접적인 피드백입니다. 테스트가 짧고 "입력 → 결과" 구조로 단순하게 작성된다면 설계가 올바른 방향이라는 증거이고, 반대라면 캡슐화를 의심해볼 시점입니다.
