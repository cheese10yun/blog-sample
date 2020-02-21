# Querydsl Projection 방법
Querydsl를 이용하는 경우 엔티티와 다른 반환 타입인 경우 `Projections`를 사용합니다. `Projections`을 하는 방법과 선호하는 패턴을 정리해보았습니다.

`Projections`을 이용해서 projection 하는 방법은 크게 3가지가 있습니다.

1. `Projections.bean`을 이용하는 방법
2. `Projections.constructor`를 이용하는 방법
3. `@QueryProjection`를 사용하는 방법

결론부터 말씀드리면 `@QueryProjection`을 사용하는 방법이 가장 좋다고 생각합니다. 각 패턴의 장단점을 설명드리겠습니다.

## Projections.bean
```kotlin
class MemberDtoBean {
    var username: String? = null
    var age: Int? = null
}

class ProjectionTest(
    private val em: EntityManager
) : SpringBootTestSupport() {

    val query = JPAQueryFactory(em)
    ...

    @Test
    internal fun `projection bean`() {
        val members = query
            .select(Projections.bean(
                MemberDtoBean::class.java,
                qMember.username,
                qMember.age
            ))
            .from(qMember)
            .fetch()

        for (member in members) {
            println(member)
        }
    }
}
```
`Projections.bean` 방식은 setter 기반으로 동작하게 됩니다. 그러기 때문에 `MemberDtoBean`객체의 setter 메서드를 열어야 합니다. 일반적으로 Response, Request 객체는 불변 객체를 지향하는 것이 바람직하다고 생각하기 때문에 권장하는 패턴은 아닙니다.

## Projections.constructor
```kotlin
data class MemberDtoConstructor(
    val username: String,
    val age: Int
)

class ProjectionTest(
    private val em: EntityManager
) : SpringBootTestSupport() {

    val query = JPAQueryFactory(em)
    ...

    @Test
    internal fun `projection constructor`() {
        val members = query
            .select(Projections.constructor(
                MemberDtoConstructor::class.java,
                qMember.username,
                qMember.age
            ))
            .from(qMember)
            .fetch()

        for (member in members) {
            println(member)
        }
    }
}
```
`Projections.constructor`를 사용하면 생성자 기반으로 바인딩 하기 때문에 `MemberDtoConstructor`객체를 불변으로 가져갈 수 있습니다. **하지만 바인딩 시키는 작업에 문제가 있습니다.**


```kotlin
.select(Projections.constructor(
    MemberDtoConstructor::class.java,
    qMember.username,
    qMember.age
))
```
위 코드를 보면 `MemberDtoConstructor`객체 생성자에 바인딩 하는 것이 아니라 `Expression<?>... exprs` 값을 넘기는 방식으로 진행합니다.

**즉 값을 넘길 때 생성자와 순서를 일치시켜야 합니다.** 위처럼 개수가 몇 개 안될 때는 문제가 되지 않으나 값이 많아지는 경우 실수할 수 있는 문제가 발생할 수 있는 확률이 높습니다. 이러한 문제가 있어 권장하지 않은 패턴입니다.

## @QueryProjection
```kotlin
data class MemberDtoQueryProjection @QueryProjection constructor(
    val username: String,
    val age: Int
)

class ProjectionTest(
    private val em: EntityManager
) : SpringBootTestSupport() {

    val query = JPAQueryFactory(em)
    ...

    @Test
    internal fun `projection annotation`() {
        val members = query
            .select(QMemberDto(
                qMember.username,
                qMember.age
            ))
            .from(qMember)
            .fetch()

        for (member in members) {
            println(member)
        }
    }
}
```
**`@QueryProjection`를 이용하면 위에서 발생한 불변 객체 선언, 생성자 그대로 사용을 할 수 있어 권장하는 패턴입니다.**

```kotlin
.select(QMemberDto( // QMemberDtoQueryProjection 의 생성자를 이용한다.
    qMember.username,
    qMember.age
))
```

정확히는 `MemberDtoQueryProjection`의 생성자를 사용하는 것이 아니라. `MemberDtoQueryProjection` 기반으로 생성된 `QMemberDtoQueryProjection` 객체의 생성자를 사용하는 것입니다.

```java
@Generated("com.querydsl.codegen.ProjectionSerializer")
public class QMemberDtoQueryProjection extends ConstructorExpression<MemberDtoQueryProjection> {

    private static final long serialVersionUID = -277743863L;

    public QMemberDtoQueryProjection(com.querydsl.core.types.Expression<String> username, com.querydsl.core.types.Expression<Integer> age) {
        super(MemberDtoQueryProjection.class, new Class<?>[]{String.class, int.class}, username, age);
    }
}
```
`QMemberDtoQueryProjection`생성자는 `MemberDtoQueryProjection` 생성자의 변수명과 순서와 정확하게 일치합니다.

그래서 IDE의 자동완성 기능을 이용해서 보다 안전하고 편리하게 생성자에 필요한 값 바인딩을 진행할 수 있습니다. 그래서 가장 권장하는 패턴입니다.

물론 단점도 있습니다. `Dto`라는 특성상 해당 객체는 많은 계층에서 사용하게 됩니다. 그렇게 되면 Querydsl의 의존성이 필요 없는 레이어에서도 해당 의존성이 필요하게 됩니다.

저는 개인적으로 이 정도의 의존관계 때문에 발생하는 의존성 문제 보다 `Projections`를 안전하게 사용할 수 있는 방법이 더 효율적이라고 생각합니다.