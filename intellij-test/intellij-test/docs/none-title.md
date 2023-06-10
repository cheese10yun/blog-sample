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

## 유효성 검사는 어디 또 해야할까?, 멀티 모듈인 경우는 ?

* 표현 계층에서 벨리데이션 하는 것이 좋음
* 그렇다면 서비스 계층에서 다른 서비스 계층을 호출하는 것은? 
* 멀티 모듈에서는 여러 애플리케이션에서 사용하면 ?

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

성인 Member를 조회 했지만 실제 Member 객체를 리턴하기 때문에 주민등록 필드를 notnull 관련 작업을 진행할 떄는 `member.residentRegistrationNumber!!`을 사용 해야 한다. 이런 경우 Projection을 사용하면 이런 문제를 쉽게 해결할 수 있다.

```kotlin
data class AdultMember(
    // notnull을 보장
    val residentRegistrationNumber: String,
    var status: MemberStatus,
)
```

자세한 Projection 방법은 [Querydsl Projection 방법 소개 및 선호하는 패턴 정리](https://cheese10yun.github.io/querydsl-projections/)에서 포스팅한 내용이 있습니다. Projection을 사용하면 영속성 컨텍스트가 없기 때문에 JPA에서 제공해주는 다양한 기능들을 사용하지 못한다. 그 밖에 단점들도 있지만 이것은 조금더 이후에 살펴보자. 이렇게까지 하면서 해야할 가치가 있을까 라는 의문이 있다.

```json
//  Refind(환불) 객체를 JSON으로 표시
{
  "order": {
    "order_number": "1110",
    "name": "나이키 에어멕스",
    "price": 10000
  },
  "payment": {
    "payment_metohd_type": "CARD",
    "credit_card": {
      "number": "110-123123",
      "card_corp": "SHINHAN"
    },
    "account": null
  }
}
```

위 데이터 구조 처럼 주문에대한 Refund(환불) 객체가있고, 신용카드 결제라면 `credit_card` 정보가 있고, 무통장 입금의 경우에는 `account` 정보가 있다고 가정해보자. `credit_card`, `account` 정보는 상호 베타적인 정보이기 떄문에 두 객체는 nullable 설정할 수 밖에 없다. Refund(환불) 엔티티 객체를 그대로 사용한다면 내가 조회한 데이터와 상관 없이 계솩 null 안정성에 대한 고민을 할 수 밖에 없고 `!!`의 불편한 동행이 계속 된다. 문제는 그것 뿐만이 아니다 환불이라는 컨텍스트의 모호함이 있다. 카드 환불인지, 무통장입금의 환불인지를 명확하게 표시하면 그 컨텍스트를 이해하는 것에 도움이 된다. 물론 변수명으로 표현이 하지만 Projection을 사용해서 `CardRefund` 타입으로 표현하는 것도 좋은 방법이라고 생각한다.

### Projection의 치명적인 단점
* [ ] DDD, rich Object 도매인이 집중됨
* [ ] 인터페이스, 그런데 단순 함수를 사용하고 싶은 것에서 상속 구조는 올바르지 않다고 생각한다, 우선 인터페이스를 두어서 얻는 이점은 세부 구현체를 숨기고 인터페이스를 바라보게 함으로써 클래스 간의 의존관계를 줄이는 것, 다형성을 사용 하는 것 이 핵심이라고 생각합니다.
* [ ] 상속보다 조립을

## Setter를 업애기 위한 노력
* setter를 안쓰기 위한 노력
* 코틀린에서는 변하지 않는 값은 그냥 val 선언
* kotlin에서 필요 이상으로 귀찮음
* 그렇다면 setter를 업애는 것은 정말 효율적인가 ?
  * 거대한 모노로틱 구조에서는 치명적인 단점
  * setter를 업애는 구조로 개편하는 것보다, 서비스 단위를 작게 유지하는 것에 리소스를 쓰는 것이 더 바람직하고 가성기가 좋음
  * 그렇다면 프로젝트의 크기를 적절하게 줄인다면 setter는 필요할까? 필요하지 않을까? 필요는 하지만 절대적으로 필요하지 않음
  * setter가 없다는건 테스트하기의 어려움, 결국 상속보단 조립을으로 키워드 전이