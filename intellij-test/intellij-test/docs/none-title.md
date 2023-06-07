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


# None title

## 파라미터의 복잡도를 어떻게 제어할 것인가?

### 조회 코드에서 직접 지정

```kotlin
class MemberRepositoryImpl(
    private val query: JPAQueryFactory,
) : MemberRepositoryCustom {
    
    override fun findBy(gender: String): List<Member> = query
        .selectFrom(member)
         // 정상적인 회원 상태를 직접 명시
        .where(member.status.`in`(setOf(MemberStatus.NORMAL, MemberStatus.DORMANCY)))
        .fetch()
}
```
장점으로는 외부에서 가져다 사용하는 경우 Member의 구체적인 상태의 상세 규칙을 몰라도 안전하게 조회할 수 있다는 점이다.  단점으로는 LOCK, BAN 상태의 조회가 필요할때 거의 유사한 코드가 중복해서 나온다는 것이다. 


### 조회 파라미터로 넘겨 받기 
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
장점으로는 여러 상태를 조합해서 사용이 가능하기 때문에 유연하게 대처가 가능하다. 단점으로는 조회하는 코드에서 실제 엑티비한 유저에 대한 구체적인 상태를 명시 해야 한다는 것이다.  

### 정리
위 예제 처럼 단순히 한 가지 필드를 가지고 구분할 수 있는 정도는 크게 문제가 되지 않지만 여러 필드들의 조합으로 특정 상태를 결정 짓는다면 복잡도에 대한 제어를 어떻게할 것인가에 대해서 고민이 필요하다. 정답이 있는 문제라고 생각하진 않지만 많은 사람들이 고민을 해봤으면 한다.

나름의 결론이 있다면(또 어떻게 바뀔지 모르겠지만) 네이밍을 통해서 그 의도를 드러나게 하는 것이 좋다고 생각한다.

```kotlin
@Service
class MemberQueryService(
    private val memberRepository: MemberRepository,
) {

    // 활성화 상태인 유저 성별로 조회, 파라미터로 상태를 직접 넘겨 받아도 무방
    fun findActivityMemberBy(gender: String): List<Member> {
        return memberRepository.findBy(gender)
    }
}

class MemberRepositoryImpl(
  private val query: JPAQueryFactory,
) : MemberRepositoryCustom {

    // 도메인적 의도가 들어나지 않아도 무방  
    override fun findBy(gender: String): List<Member> = query
      .selectFrom(member)
      .where(member.status.`in`(MemberStatus.NORMAL, MemberStatus.DORMANCY))
      .fetch()
}
```

MemberQueryService 같은 서비스 계층을 두고 여기에서 네이밍으로 명확하게 그 의도를 설정하면 파라미터 상태를 받아도, 안받아도 크게 지장이 없다고 생각한다. 또 Repository는 인프라레이어에 가깝기 떄문에 비즈니스 관련 네이밍을 작성하지 않는것이 더 바람직 하다고 생각한다. MemberQueryService 처럼 조회 전용 서비스를 작성하는 것을 선호하는데 관련 포스팅은 [Spring Guide - Service 가이드](https://cheese10yun.github.io/spring-guide-service/#-2)에 정리되어 있다. 객체의 책임의 크기에 관심이 있다면 읽어봐도 좋다.


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

