# 이것 저것

* [ ] 조회 하는 경우 파라미터를 열것인가 받을 것인가
* [ ] notnull을 보장 받고 싶은 경우, 상호 베타적인 값들을 조회할 때
  * 프로젝션을 선호 하는 이유
  * 상속보단 조합을
* [ ] 유효성 검사는 어디 또 해야할까?, 멀티 모듈인 경우는 ?
  * 파라미터로 검증 할지 여부를 받는다
* [ ] repositroy에서는 네이밍 생략 서비스에서 표현
* [ ] setter를 없애는게 큰 장점일까?
  * 코틀린에서는 변하지 않는 값은 그냥 val 선언
  * 생성자로 제어하기 어려운 부분
  * 그럼에도 가능한 케이스 조회 전용,

# 좋은 코드 설계를 위한 답없는 고민들

좋으 코드 설계를 위한 고민들을 평소에 많이 해왔고, 그에 관련한 학습들도 진행 했었다. OOP, DDD, Clean Code, Clean Architecture 등등을 통해서 나름의 주관이 생겼으며 경력 초반에는 이런 것들을 지키기 위해 많이 노력해왔다. 현재는 이런 개념들을 선택적으로 적용하며 또 어떠한 의미에서는 이런것들을 지키는 것들에 대해서 가성비가 좋지 않다고 까지 생각한다. 해당 포스팅에서 작성한 내용은 개발하면서 코드 설계 적인 부분에 대해서 아직까지 고민을 하고 있는 부분들에 대해서 정리한 것들이다.

## 복잡도를 어디서 제어(책임)할 것인가?

당연히 요구사항이 복잡하니 코드 또한 복잡해 진다. 결국 이러한 복잡도를 어느 코드에서는 해결 해야하는데 이 부분에 대한 고민이다. 

```kotlin
enum class MemberStatus(
    desc: String
) {
    NORMAL("정상"), // 이메일 받는 회원 
    UNVERIFIED("미인증"), // 이메일 받는 회원
    LOCK("계정 일지 정지"), // 이메일 제외 회원
    BAN("계정 영구정지"); // 이메일 제외 회원
}
```
예를 들어 특정 성별 중 현재 활성화된 회원들 전체에게 이메일을 보내는 로직에서 활성화 회원들을 조회하는 코드가 있다고 가정 해보자.

### 조회 코드에서 복잡도 제어

```kotlin
class MemberRepositoryImpl(
    private val query: JPAQueryFactory,
) : MemberRepositoryCustom {
    
    override fun findBy(gender: String): List<Member> = query
        .selectFrom(member)
         // 정상적인 회원 상태를 직접 명시
        .where(member.genter.eq(gender))
        .where(member.status.`in`(setOf(MemberStatus.NORMAL, MemberStatus.UNVERIFIED)))
        .fetch()
}
```
조회 코드에서 회원 상태의 복잡도를 직접 제어하면 외부 객체에서 현재 활성화 상태에 대한 복잡도에 대해서 자유로워 진다. 즉 호출하는 객체에서는 회원의 상태에 대해서 알바가 없어 진다는 장점이 있다. 하지만 단점 또한 있다. 회원 상태가 다른 조회 로직이 있다면 거의 유사한 코드가 중복해서 나온다는 것이다.

### 조회를 호출하는 코드에서 복잡도 제어

```kotlin
class MemberRepositoryImpl(
    private val query: JPAQueryFactory,
) : MemberRepositoryCustom {
    
    // 파라미터로 직접 받는다.
    override fun findBy(gender: String, memberStatuses: Set<MemberStatus>): List<Member> = query
        .selectFrom(member)
        .where(member.status.`in`(memberStatuses))
        .fetch()
}
```
조회를 호출하는 코드에서 복잡도 제어를 하면 회원 상태에 대한 세부적인 규칙을 해당 객체를 호출하는 곳에 복잡도가 위임된다. 즉 호출하는 쪽에서 회원 상태에 대해서 명확하게 알고 있어야 한다. 물론 이정도 상태 정도야 복잡도가 높다고 할 수 없지만 여러 필드들의 조합을 분석해서 조회 해야하는 경우는 복잡도가 높아진다. 또 요구사항이 바뀌어서 코드를 변경했다면 호출하는 코드들을 모두 찾아가서 변경해야 한다. 그 복잡도를 외부에서 제어 했기 때문에 당연한 결과이다. 

### 정리

단순 파라미터로 받을것인가 아닌가에 대한 단순한 고민이 아니라 복잡도를 어디에서 제어할것인가? 그에 따른 장단점이 있고 어떠한 근거로 어떠한 방법을 택할것인가 또 그 근거는 무엇인가에 대한 고민을 해봤으면 한다. 나름의 결론이 있다면(또 어떻게 바뀔지 모르겠지만) 네이밍을 통해서 그 의도를 드러나게 하는 것이 좋다고 생각한다.

```kotlin
@Service
class MemberQueryService(
    private val memberRepository: MemberRepository,
) {

    // 활성화 상태인 유저 성별로 조회, 명확하게 해당 의도 전달
    fun findActivityMemberBy(gender: String): List<Member> {
        return memberRepository.findBy(gender, setOf(MemberStatus.NORMAL, MemberStatus.UNVERIFIED))
    }
}

class MemberRepositoryImpl(
    private val query: JPAQueryFactory,
) : MemberRepositoryCustom {

    // 제너럴하게 파라미터로 넘겨 받음
    override fun findBy(gender: String, memberStatuses: Set<MemberStatus>): List<Member> = query
        .selectFrom(member)
        .where(member.status.`in`(memberStatuses))
        .fetch()
}
```
MemberQueryService 같은 서비스 계층을 두고 해당 객체에서 네이밍으로 명확하게 그 의도를 표현하고, Repository 계층에서는 제너럴하게 파라미터로 받아 처리한다. 이렇게 하면 서비스 계층에서는 명확하게 현재 활성화 상태의 유저를 조회하게 되며, 인프라 계층에서는 제너럴하게 조회로직을 작성함으로써 중복 코드 및 유사 코드를 방지할 수 있다. 인프라스트럭처에 직접적인 의존성을 갖게하는 것보다 MemberQueryService 처럼 서비스 계층을 통해 인프라스트럭처를 간접적으로 의존하는 것이 여러모로 좋다고 생각한다. 관련 포스팅은
[Spring Guide - Service 가이드](https://cheese10yun.github.io/spring-guide-service/#-2)에 정리되어 있다.


## Notnull을 보장 받고 싶은데...

```kotlin
class Member(
    // 주민등록 번호, 선인 회원인 경우 반드시 주민번호 등록 등록되어 있다.
    @Column(name = "resident_registration_number", nullable = true)
    val residentRegistrationNumber: String?,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: MemberStatus = MemberStatus.NORMAL,
) : EntityAuditing()


class MemberRepositoryImpl(
  private val query: JPAQueryFactory,
) : MemberRepositoryCustom {
    
    // 성인 Member 조회
    override fun findBy(age: Int): List<Member> = query
        .selectFrom(member)
        .where(member.status.`in`(MemberStatus.NORMAL, MemberStatus.DORMANCY))
        .where(member.age.gt(age))
        .fetch()
}
```
성인 Member를 조회 했지만 실제 Member 객체를 리턴하기 때문에 주민등록 필드를 notnull 관련 작업을 진행할 떄는 `member.residentRegistrationNumber!!`을 사용 해야 한다.

개인적으로 JPA를 사용하지만 더티 체킹을 통한 업데이트 등은 생상성과 효율성을 매우 높여주지만, 이것을 사용하지 않을 떄는 Projection을 사용해서 리턴하는 것이 좋다고 생각한다.

```kotlin
data class AdultMember(
    // ...
    val residentRegistrationNumber: String,
    var status: MemberStatus,
)
```
성인 Member Projection 객체를 만들고 여기서 Notnull을 보장하자. 물론 모든 경우에 이런 패턴이 적합한것은 아니지만 Notnull 필드가 많고 그 것에 따라 의미가 크게 달라지는 구간에서는 활용성이 높다고 생각한다. 예를 들어 카드 결제, 무통장 결제 등등 관련 필드가 너무 다르기 떄문에 이렇게 구분해서 객체를 만들어서 사용하는 것도 좋은 방법이다. Projection 방법은 ![Querydsl Projection 방법 소개 및 선호하는 패턴 정리](https://cheese10yun.github.io/querydsl-projections/)에서 포스팅한 내용이 있습니다.

