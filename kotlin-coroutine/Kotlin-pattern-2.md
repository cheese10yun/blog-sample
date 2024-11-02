# Kotlin에서 객체 생성의 안전성과 유효성 강화하기

코틀린에서 객체 생성과 값 타입 관리의 안전성을 강화하는 다양한 방법에 대해 알아보겠습니다. 이번 포스팅에서는 `User`라는 엔티티 클래스를 예로 들어 생성자 제약 사항을 안전하게 처리하는 방법과, `value class`를 활용해 값 타입을 효율적으로 관리하는 방법에 대해 설명하겠습니다.

## 기존 생성자 코드의 문제점

아래는 간단한 `User` 클래스입니다. 이 클래스는 JPA 엔티티로 정의되어 있으며, 데이터베이스와 매핑되는 사용자 정보를 나타냅니다.

```kotlin
@Entity(name = "user")
@Table(name = "user")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var name: String,
    val email: String,
    @Enumerated(EnumType.STRING)
    var stats: UserStats
)
```

위 코드를 보면 객체 생성 시 다음과 같은 문제가 발생할 수 있습니다:

```kotlin
// 잘못된 객체 생성 예시
val user = User(
    id = 1L,  // 외부에서 설정하면 안 됨
    name = "  John Doe  ",  // 공백 제거 불가
    email = "  john.doe@example.com  ",  // 공백 제거 불가
    stats = UserStats.ACTIVE  // 생성 시 초기값은 무조건 NORMAL이어야 함
)
```

1. `id`는 데이터베이스에서 자동으로 생성되기 때문에, 외부에서 객체 생성 시 값을 전달하면 안 됩니다.
2. `stats` 필드는 초기 생성 시 무조건 `UserStats.NORMAL`이어야 하므로, 외부에서 제어권을 가지면 안 됩니다.
3. `name`과 `email` 필드는 공백을 제거해야 하는데, 현재 생성자로는 이러한 처리가 어렵습니다.
4. 그 외에도 객체 생성 시 내부적으로 유효성 검사를 하는 것이 제한적입니다.

이러한 문제들을 해결하기 위해, 우리는 안전한 객체 생성 방식을 도입할 수 있습니다.

#### Companion Object를 활용한 객체 생성 안전성 강화

이 문제를 해결하기 위해, `companion object`와 `operator fun invoke`를 활용한 객체 생성 방식을 제안합니다. 아래는 수정된 코드입니다:

```kotlin
class User private constructor(
    var id: Long? = null,
    var name: String,
    val email: String,
    @Enumerated(EnumType.STRING)
    var stats: UserStats
) {
    companion object {
        operator fun invoke(
            name: String,
            email: String
        ): User {
            // 유효성 검사 로직 추가
            require(name.isNotBlank()) { "Name must not be blank" }
            require(email.isNotBlank()) { "Email must not be blank" }

            return User(
                name = name.trim(),
                email = email.trim(),
                id = null,
                stats = UserStats.NORMAL
            )
        }
    }
}
```

이제 외부에서 객체를 생성할 때는 다음과 같이 사용할 수 있습니다:

```kotlin
// 안전한 객체 생성 예시
val user = User(
    name = "  John Doe  ",
    email = "  john.doe@example.com  "
)
```

위 코드는 기존의 생성자 호출 방식과 동일하게 사용할 수 있지만, 내부적으로는 안전한 로직을 적용하여 객체를 생성하도록 유도합니다. 이를 통해 불필요한 오류를 방지하고 객체 생성 과정을 간소화할 수 있습니다.

### 주요 개선 사항

1. **기본 생성자 막기**: `private constructor`를 사용해 기본 생성자를 외부에서 호출하지 못하게 막았습니다. 이를 통해 객체 생성을 `companion object` 내부의 로직으로만 유도할 수 있게 되었습니다.
2. **invoke 연산자 활용**: `companion object`에 `operator fun invoke`를 정의하여 객체 생성 시 사용자가 보다 명확한 API를 사용할 수 있게 했습니다. 이는 기존의 생성자 호출 방식과 동일하게 사용할 수 있으면서도 `name`과 `email`의 앞뒤 공백을 자동으로 제거하고, `stats` 필드는 무조건 `UserStats.NORMAL`로 설정하게 되었습니다.
3. **안전한 필드 초기화**: `id`와 같이 외부에서 설정되면 안 되는 필드는 생성자에서 제외하여, 개발자가 실수로 이를 설정하는 것을 방지했습니다.

## Value Class를 활용한 값 타입 관리

또한, 이메일과 같은 값 타입을 별도로 관리하기 위해 코틀린의 `value class`를 활용하는 것도 좋은 방법입니다. 아래는 `Email`이라는 값 클래스를 정의하고 사용하는 예시입니다:

```kotlin
@JvmInline
value class Email(val value: String) {
    init {
        require(value.isNotBlank()) { "Email must not be blank" }
        require(value.contains("@")) { "Email must contain '@'" }
    }
}
```

### Email Value Class 적용

위에서 정의한 `Email` 클래스를 `User` 클래스와 `Order` 클래스에 적용해보겠습니다:

```kotlin
class User private constructor(
    var id: Long? = null,
    var name: String,
    val email: EJ
    var stats: UserStats
) {
    companion object {
        operator fun invoke(
            name: String,
            email: String
        ): User {
            // 유효성 검사 로직 추가
            require(name.isNotBlank()) { "Name must not be blank" }

            return User(
                name = name.trim(),
                email = Email(email),
                id = null,
                stats = UserStats.NORMAL
            )
        }
    }
}

// Order 클래스 정의
class Order private constructor(
    var id: Long? = null,
    val email: Email,
    var amount: Double
) {
    companion object {
        operator fun invoke(
            email: String,
            amount: Double
        ): Order {
            // 유효성 검사 로직 추가
            require(amount > 0) { "Amount must be greater than zero" }

            return Order(
                email = Email(email),
                id = null,
                amount = amount
            )
        }
    }
}
```

### Value Class를 통한 객체 관리의 이점

1. **명확한 타입 정의**: `Email`이라는 값 클래스를 사용함으로써, 이메일 주소를 단순히 문자열로 다루는 대신 명확한 타입으로 관리할 수 있습니다. 이를 통해 이메일 값이 기대하는 형태로만 사용되도록 강제할 수 있습니다.
2. **유효성 검사 통합**: 이메일과 관련된 유효성 검사 로직을 `Email` 클래스 내부에 정의함으로써, 이메일이 생성되는 모든 곳에서 일관된 유효성 검사를 적용할 수 있습니다. 이를 통해 코드 중복을 줄이고 유지보수성을 높일 수 있습니다.
3. **가독성 향상**: 이메일과 같은 값 타입을 별도로 정의함으로써, 코드의 가독성을 높이고, 객체의 의미를 보다 명확하게 전달할 수 있습니다. 이를 통해 코드가 더 직관적이며 이해하기 쉬워집니다.
4. **재사용성**: `Email` 클래스는 `User` 클래스뿐만 아니라, 예를 들어 `Order`와 같은 다른 도메인 객체에서도 재사용할 수 있습니다. 이를 통해 일관된 이메일 관리와 유효성 검사를 보장하며, 코드 중복을 최소화할 수 있습니다. 이렇게 공통적으로 사용되는 값 타입을 재사용함으로써 코드의 유지보수성과 확장성을 크게 향상시킬 수 있습니다.

### Value Class 사용 시 주의 사항

`value class`로 감싼 타입은 JPA나 MongoDB와 같은 데이터베이스 접근 라이브러리를 사용할 때 자동으로 매핑되지 않기 때문에, 적절한 컨버터를 정의해줘야 합니다. 예를 들어, JPA에서는 `AttributeConverter`를 사용하여 `Email` 클래스를 문자열로 변환하고 다시 복원할 수 있는 컨버터를 구현해야 합니다.

## 결론

코틀린에서 객체를 생성하는 다양한 방법이 있지만, 제약 사항이 많은 경우에는 `companion object`와 `invoke` 연산자를 활용하는 방식이 매우 유용합니다. 또한, 값 타입을 별도로 관리하기 위해 `value class`를 사용하는 것도 좋은 접근법입니다. 이를 통해 외부에서 설정하지 말아야 할 필드를 보호하고, 객체 생성 시 발생할 수 있는 오류를 줄일 수 있으며, 값 타입에 대한 유효성 검사와 관리도 일관되게 할 수 있습니다.

여러분도 코틀린을 사용할 때, 이러한 안전한 객체 생성 방식과 값 타입 관리를 적용해보세요! 필요에 따라 객체의 생성 로직을 유연하고 안전하게 제어할 수 있을 것입니다.

