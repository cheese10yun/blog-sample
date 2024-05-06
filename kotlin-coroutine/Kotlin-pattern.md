# 코툴린 자주 사용하는 패턴 정리

* [ ]  runCatching
* [ ]  data class copy
* [ ]  pari triple
* [ ]  by logger
* [ ]  class address, addressDetail 필드 노출
* [ ]  init 순서
* [ ]  by not null
* [ ]  const val

블로그 포스트를 위한 내용 정리와 소제목 개선을 도와드리겠습니다. 아래는 보다 명확하고 전문적인 톤으로 수정한 내용입니다:

---

## 불변 객체의 효율적 관리: `copy()` 메소드 활용

불변 객체는 데이터의 안정성과 예측 가능성을 제공하여 소프트웨어 개발에서 권장되는 패턴입니다. 특히, 암호화와 같은 데이터의 보안적 처리에 있어서 불변 객체를 사용하면 변경될 필요가 없는 정보의 무결성을 유지할 수 있습니다. Kotlin의 `data class`는 이러한 불변 객체를 다루기 위한 유용한 기능 중 하나로 `copy()` 메소드를 제공합니다. 이 메소드를 사용하면 객체의 일부만을 변경한 새로운 객체를 생성할 수 있어, 기존 객체의 불변성을 해치지 않으면서 필요한 부분만 업데이트가 가능합니다.

### 코드 예시 및 설명

아래의 테스트 코드는 `User`라는 데이터 클래스의 인스턴스를 생성한 후, `copy()` 메소드를 사용하여 이메일 주소만을 암호화된 형태로 변경하는 예를 보여줍니다.

```kotlin
@Test
fun `불변 객체의 유지보수를 위한 copy 활용 예시`() {
    val user = User(
        name = "name",
        email = "email@asd.com"
    )

    val userCopy = user.copy(
        email = "email@asd.com 암호화"
    )

    // User(name=name, email=email@asd.com)
    println("user: $user")
    // User(name=name, email=email@asd.com 암호화)
    println("userCopy: $userCopy")

    // 428039780
    println("user: ${System.identityHashCode(user)}")
    // 48361312
    println("userCopy: ${System.identityHashCode(userCopy)}")
}
```

- **원본 객체 출력**: `user: User(name=name, email=email@asd.com)`
- **복사 후 업데이트된 객체 출력**: `userCopy: User(name=name, email=email@asd.com 암호화)`
- **객체 식별자 비교**: 두 객체의 `System.identityHashCode` 값을 출력하여 각각 다른 객체임을 확인할 수 있습니다.

### 포인트 정리

- `copy()` 메소드는 원본 객체의 일부 속성을 변경하여 새로운 객체를 생성합니다. 이 방식은 기존 객체의 불변성을 유지하면서 필요한 데이터만 갱신할 수 있는 효율적인 방법을 제공합니다.
- `val` 키워드를 사용하여 불변성을 명시하는 것은 데이터 보호 및 버그 방지에 중요합니다. 특히 암호화와 같이 데이터 보안이 중요한 작업에서는 불변 객체의 사용이 더욱 중요합니다.

이 방법은 데이터의 무결성을 유지하면서도 효율적인 데이터 관리를 가능하게 하여, 유지보수성을 높이고 시스템의 안정성을 강화합니다. 불변 객체와 `copy()` 메소드의 적절한 사용은 모던 소프트웨어 개발의 중요한 측면 중 하나입니다.

## Pair와 Triple 객체의 유용성과 효율적 사용

코틀린에서는 간단한 객체를 빠르게 생성하고 사용할 수 있도록 `Pair`와 `Triple`이라는 두 가지 유틸리티 클래스를 제공합니다. 이러한 클래스는 특히 서비스 내부 로직에서만 사용되는 임시 데이터를 다룰 때, 매번 새로운 DTO(Data Transfer Object)를 만드는 것보다 더 효율적일 수 있습니다. 또한, 멀티 모듈 프로젝트에서 여러 모듈 간에 DTO 클래스를 공유해야 할 때 이러한 객체의 사용이 유용합니다.

### 코드 예시 및 설명

다음은 `UserPointCalculator` 클래스의 구현 예시입니다. 이 클래스는 사용자 정보를 MySQL 데이터베이스에서 가져오고, 사용자의 포인트 정보는 Redis에서 가져와 계산을 진행합니다. 이 과정에서 Triple 객체를 활용하여 각 사용자의 이름, 이메일, 포인트 정보를 효과적으로 관리합니다.

```kotlin
class UserPointCalculator(
    private val userRepository: UserRepository,
    private val userPointRepository: UserPointRepository
) {
    fun calculate() {
        val users = userRepository.findUserByIds(listOf(1, 2, 3))
        val points = userPointRepository.findUserPoint(listOf(1, 2, 3))
            .associateBy { it.id }

        val userPoints = users.map {
            Triple(
                first = it.name,
                second = it.email,
                third = points[it.id]!!.point
            )
        }

        for (userPoint in userPoints) {
            println("user name: ${userPoint.first}, user email  ${userPoint.second}, user point  ${userPoint.third}")
        }
    }
}
```

**분석**: 이 예에서 `Triple` 객체는 각 사용자의 이름, 이메일, 포인트를 저장하는데 사용됩니다. 이는 데이터베이스와 다른 저장소에서 정보를 읽어와 조합할 때 유용하게 사용됩니다.

### Pair와 Triple 사용 시 가독성 향상

Pair와 Triple은 기본적으로 `first`, `second`, `third`라는 속성명을 사용합니다. 이 속성명은 코드의 가독성을 저하시킬 수 있으므로, 구조 분해 할당(destructuring declaration)을 사용하여 보다 의미 있는 변수명을 사용하는 것이 좋습니다.

```kotlin
fun calculate() {
    // 반복 처리
    for ((userName, userEmail, userPoint) in userPoints) {
        println("user name: ${userName}, user email ${userEmail}, user point $userPoint")
    }
    // 개별 처리
    val (userName, userEmail, userPoint) = userPoints.first()
}
```

이와 같은 처리 방식은 `Pair`와 `Triple`을 사용할 때 코드의 명확성을 향상시키고, 데이터를 보다 효율적으로 다루는 데 도움이 됩니다. 데이터를 직관적으로 알아볼 수 있도록 이름을 명확하게 지정함으로써, 코드의 가독성과 유지보수성을 크게 개선할 수 있습니다.

이 방법은 간단한 데이터 구조를 사용하면서도 프로그램의 복잡성을 줄이고, 클린 코드를 유지하는 데 기여합니다.

### 포인트 정리

`Pair`와 `Triple` 객체는 임시 데이터 또는 내부 로직에서만 사용되는 데이터를 간편하게 다루기 위한 우수한 도구입니다. 이들은 DTO를 정의하는 복잡성을 피할 수 있으며, 특히 간단한 데이터 그룹을 빠르게 다루어야 할 때 효율적입니다. 하지만, 이들을 사용할 때는 변수명을 명확하게 지정하여 코드의 가독성을 유지하는 것이 중요합니다.

이 글은 Pair와 Triple 객체의 적절한 사용 사례를 통해 개발자들이 코드의 간결성과 유지보수성을 향상시키는 방법을 제시합니다.

## `typealias`를 활용한 코드 개선

코틀린의 `typealias` 기능은 복잡한 타입 선언에 대한 간결하고 의미 있는 이름을 제공함으로써 코드의 가독성과 유지보수성을 크게 향상시킵니다. 특히, 프로젝트 내에서 자주 사용되는 타입 조합에 별칭을 부여함으로써, 코드의 일관성을 유지하고 타입 변경 시의 유연성을 높일 수 있습니다.

### 코드 예시 및 설명

다음 예제에서는 사용자 정보(`User`)와 사용자의 포인트(`UserPoint`)를 연결하는 `Pair`에 `UserPointAssociation`이라는 `typealias`를 사용합니다. 이는 `Pair<User, UserPoint>`의 사용을 간소화하고, 의미를 명확히 합니다.

```kotlin
typealias UserPointAssociation = Pair<User, UserPoint>

val userPointAssociations = users.map {
    UserPointAssociation(
        first = it,
        second = points[it.id]!!
    )
}

val (user, userPoint) = userPointAssociations.first()
```

- `UserPointAssociation` 타입 별칭은 `Pair<User, UserPoint>`를 대체하여 코드의 목적을 더 명확하게 표현합니다.
- `map` 함수 내에서 `UserPointAssociation` 생성자를 사용하여 각 사용자와 해당 포인트 객체를 쌍으로 묶습니다. 이는 데이터의 논리적 연관성을 직관적으로 보여줍니다.
- 구조 분해 할당을 통해 `user`와 `userPoint` 변수에 각각 사용자 정보와 포인트 정보를 할당함으로써, 코드의 가독성을 더욱 향상시킵니다.
-

### 포인트 정리

- **명확성**: `UserPointAssociation`과 같은 타입 별칭을 사용하면, 복잡한 타입 조합도 의미 있는 이름으로 대체되어 코드의 목적이 분명해집니다.
- **유연성**: 타입 별칭을 사용하면, 기본 타입 구조에 변경이 필요할 때 별칭 정의만 수정하면 되므로 코드 전체에 걸쳐 유연하게 변경을 적용할 수 있습니다.
- **재사용성**: 일단 정의된 타입 별칭은 프로젝트 전반에 걸쳐 재사용될 수 있으며, 코드의 일관성을 유지하는 데 도움이 됩니다.

`typealias`는 복잡한 타입을 간소화하고, 코드의 의도를 명확히 전달하는 데 큰 도움을 줍니다. 이는 특히 크고 복잡한 프로젝트에서 타입 관리를 효과적으로 수행하는 데 중요한 역할을 합니다.

## `runCatching` 함수 활용하기

코틀린의 `runCatching` 함수는 코드 블록을 안전하게 실행하고 그 결과를 `Result` 객체로 캡처하는 유용한 도구입니다. 이 함수는 예외 발생 시 처리를 간소화하며, 성공적인 실행과 예외 상황을 우아하게 분리할 수 있게 도와줍니다.

### 코드 예시 및 설명

다음은 `runCatching`을 사용하여 데이터베이스에서 사용자 정보를 조회하고, 실패 시 로깅하는 코드 예제입니다:

```kotlin
fun loadUserData(userId: Int): User? {
    return runCatching {
        userRepository.findById(userId)
    }.onFailure {
        log.error("Failed to fetch user data for user ID $userId", it)
    }.getOrNull()
}
```

#### 설명:

- `userRepository.findById(userId)` 호출이 `runCatching`으로 감싸져 있어, 이 함수가 예외를 발생시킬 경우 이를 `Result` 객체가 캡처합니다.
- `onFailure`는 예외가 발생했을 때 실행되는 블록으로, 여기서 실패 로그를 기록합니다.
- `getOrNull` 메서드는 `Result` 객체에서 결과를 안전하게 추출합니다. 예외가 발생하면 `null`을 반환합니다.

### 포인트 정리

- **안전한 예외 처리**: `runCatching`을 사용하면 예외 발생 가능성이 있는 코드를 안전하게 실행하고, 예외를 간편하게 처리할 수 있습니다.
- **코드의 간결성**: `try-catch` 블록에 비해 코드가 더 간결하고 명확해집니다. 이를 통해 코드의 가독성이 향상되고, 유지보수가 용이해집니다.
- **유연한 결과 처리**: `Result` 객체를 통해 성공적인 실행 결과를 직접적으로 처리하거나, 예외 발생 시 대안적인 실행 경로를 정의할 수 있습니다.
- **확장 함수 사용**: `Result` 타입에는 `getOrElse`, `getOrNull` 같은 확장 함수가 포함되어 있어, 성공적인 결과 또는 예외 처리를 우아하게 구현할 수 있습니다.

`runCatching` 함수는 코틀린에서 제공하는 강력한 예외 처리 메커니즘 중 하나로, 개발자가 보다 체계적이고 안전한 코드를 작성할 수 있도록 지원합니다. 이 기능을 활용함으로써, 예외 처리 로직을 간소화하고, 프로그램의 안정성을 높일 수 있습니다.

## by 키워드 사용

## 외부에 노출 필드를 최소화

class address, addressDetail 필드 노출

## by not null

## const val
