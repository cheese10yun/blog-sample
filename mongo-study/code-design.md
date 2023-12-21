## DDD 및 Rich Object에 대한 회의감

```kotlin

class Member(
    @Field(name = "member_id")
    val memberId: String,

    @Field(name = "name")
    var name: String,

    @Field(name = "email")
    val email: String
) : Auditable()
```

* name은 변수로, 업데이트가 가능함
* email은 불변으로 업데이트 불가능

```kotlin

fun xxxx() {
    val member = memberRepositroy.findById("ID..")
    member.updateName("new Name")
}
```

* 정통적인 도메인 기반 변경
* 변경에 대한 책임과 역할을 객체 스스로가 판단하여 OOP 적인 코드
* 그러나 외부에서 손쉽게 변경 가능 사실상 member를 죄회할 수 있다면 모든 공간에서 수정이 가능함, repositroy save를 통해서 가능
* 단순 setter도 제거하기 에는 많은 비용이 발생함 [좋은 코드 설계를 위한 답없는 고민들-Setter 없애기](https://cheese10yun.github.io/code-design/#setter), 물론 setter를 없애는 것도 방법이지만 그에 따른 비용과 현실적인 어려움이 있음

### 나름의 대안

```kotlin
class Member(
    @Field(name = "member_id")
    val memberId: String,

    @Field(name = "name")
    val name: String,

    @Field(name = "email")
    val email: String
) : Auditable()

data class MemberUpdateForm(
    val id: ObjectId,
    val name: String,
    val email: String
)
```

* 모든 필드를 val으로 지정
* MemberUpdateForm 처럼 논리적인 변경의 단위를 value object으로 작성

```kotlin
interface MemberRepository : MongoRepository<Member, ObjectId>, MemberCustomRepository,
    QuerydslPredicateExecutor<Member>

interface MemberCustomRepository {
    fun findByName(name: String): List<Member>

    fun updateProfile()
}

class MemberCustomRepositoryImpl(mongoTemplate: MongoTemplate) : MemberCustomRepository,
    MongoCustomRepositorySupport<Member>(
        Member::class.java,
        mongoTemplate
    ) {

    fun updateProfile(form: MemberUpdateForm): UpdateResult {
        return mongoTemplate.updateFirst(
            Query(Criteria.where("_id").`is`(form.id)),
            Update()
                .set("name", form.name)
                .set("email", form.email),
            Member::class.java
        )
    }
}
```

* 명확하게 필드만 업데이트
* 변경에 대한 포인트는 무조건 updateProfile 통해서만 업데이트 가능

```kotlin
@Service
class MemberQueryService(
    private val memberRepository: MemberRepository
) {

    fun updateProfile(form: MemberUpdateForm) {
        // 유호성 검사 등등 기타 로직
        memberRepository.updateProfile(form)
    }
}
```

* XXXQueryService 으로 한 번감싸서 updateProfile 기능 제공
* XXXRepository를 접근할 수 있는 객체를 QueryService 으로만 한정, XXXRepository는 해당 객체에 대해서 전지전능하기 떄문에 캡슐화 및 구조적인 설게를 모두 무시할 수 있기 때문에 최대한 외부로 공개하는 것을 방지

### 여러 협력이 필요한 경우

```kotlin
@Service
class MemberQueryService(
    private val memberRepository: MemberRepository,
    private val couponRepository: CouponRepository,
    private val couponQueryService: CouponQueryService,

    ) {

    fun getXXX(form: MemberUpdateForm) {
        // 쿠폰 & 유저 관련 조회 코드
        // couponRepository 를 이용해서 접근하지 말것
        // couponQueryService을 통해서만 접근할것
    }
}
```

* QueryService 기반으로 의존성 주입 받아서 진행

```kotlin
@Service
class AggregationCoupon(
    private val memberQueryService: MemberQueryService,
    private val couponQueryService: CouponQueryService,

    ) {

    fun getXXX(form: MemberUpdateForm) {
        // 쿠폰 & 유저 관련 조회 코드
        // memberQueryService
        // couponQueryService
    }
}
```

* 제 3에 영역을 만들어서 로직 진행
* 헥사고날에서는 유스케이스 라는 것으로 행위 기반으로 자연스럽게 유도하는 부분이 있음