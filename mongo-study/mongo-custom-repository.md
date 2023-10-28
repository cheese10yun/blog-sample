# Spring Data MongoDB Custom Repository

[Spring-JPA Best Practices step-15 - Querydsl를 이용해서 Repository 확장하기 (1)](https://cheese10yun.github.io/spring-jpa-best-15/)

에서 말한 것처럼 블라블라 custom repo + custom repo 구현 체로 Repository를 확장하는게 가능하다 블라 블라블라


## Custom Repository

```kotlin
@Document(collection = "members")
class Member(
    @Field(name = "name")
    val name: String,

    @Field(name = "email")
    val email: String
) : Auditable()

interface MemberRepository : MongoRepository<Member, ObjectId>, MemberCustomRepository, QuerydslPredicateExecutor<Member>

interface MemberCustomRepository

class MemberCustomRepositoryImpl(private val mongoTemplate: MongoTemplate) : MemberCustomRepository
```

MemberCustomRepository 인터페이스를 두고 실제 구현체를 연결하여 최종적으로 MemberRepository에서 MemberCustomRepository를 구현하는 방식으로 기존 방식과 동일


```kotlin
interface MemberCustomRepository {
    fun findByName(name: String): List<Member>
    fun findByEmail(email: String): List<Member>
}

class MemberCustomRepositoryImpl(private val mongoTemplate: MongoTemplate) : MemberCustomRepository {
    override fun findByName(name: String): List<Member> {
        val query = Query(Criteria.where("name").`is`(name))
        return mongoTemplate.find(query, Member::class.java)
    }

    override fun findByEmail(email: String): List<Member> {
        val query = Query(Criteria.where("email").`is`(email))
        return mongoTemplate.find(query, Member::class.java)
    }
}
```

![](images/custom-01.png)

## Test Code

```kotlin
@Test
fun `findByEmail asd`() {
    //given
    memberRepository.findByEmail("sample@test.com")

    //when
}
```

복잡한 쿼리의 구현은 MemberCustomRepositoryImpl에서 처리하고 외부에서는 MemberRepository만 바라보게 할 수 있습니다.


## CustomRepositoryImpl를 서포트하은 MongoCustomRepositorySupport 

![Querydsl Repository Support 활용](https://cheese10yun.github.io/querydsl-support/)에서 포스팅한 CustomRepositoryImpl에서 자주 사용하는 로직과 반복되는 복잡한 코드들을 `QuerydslRepositorySupport`를 통해서 해결했습니다.

마찬 가지로 몽고도 동일하게 블라블라

```kotlin
abstract class MongoCustomRepositorySupport<T>(
    protected val documentClass: Class<T>,
    protected val mongoTemplate: MongoTemplate
) {
    private val logger by logger()

    protected fun logQuery(
        query: Query,
        name: String? = null,
    ) {
        if (logger.isDebugEnabled) {
            logger.debug("Executing MongoDB $name Query: $query")
        }
    }

    protected fun <S : T> applyPagination(
        pageable: Pageable,
        contentQuery: (Query) -> List<S>,
        countQuery: (Query) -> Long
    ) = runBlocking {
        val content = async { contentQuery(Query().with(pageable)) }
        val totalCount = async { countQuery(Query()) }
        PageImpl(content.await(), pageable, totalCount.await())
    }

    protected fun <S : T> applySlicePagination(
        pageable: Pageable,
        contentQuery: (Query) -> List<S>
    ): Slice<S> {
        val queryForContent = Query().with(pageable)
        val content = contentQuery(queryForContent)
        val hasNext = content.size > pageable.pageSize

        return SliceImpl(content.take(pageable.pageSize), pageable, hasNext)
    }
}
```

[JPA 페이징 Performance 향상 방법](https://cheese10yun.github.io/page-performance/)과 동일한 패턴 블라블라



```kotlin

interface MemberCustomRepository {
    ...
    fun findPageBy(
        pageable: Pageable,
        name: String?,
        email: String?
    ): Page<Member>
}

class MemberCustomRepositoryImpl(mongoTemplate: MongoTemplate) : MemberCustomRepository, MongoCustomRepositorySupport<Member>(
    Member::class.java,
    mongoTemplate
) {
    ...
    
    override fun findPageBy(
        pageable: Pageable,
        name: String?,
        email: String?
    ): Page<Member> {

        val queryBuilder: (Query) -> Query = { query ->
            val criteria = Criteria().apply {
                name?.let { and("name").regex(".*$it.*", "i") }
                email?.let { and("email").regex(".*$it.*", "i") }
            }
            query.addCriteria(criteria)
        }

        return applyPagination(
            pageable = pageable,
            contentQuery = { query ->
                mongoTemplate.find(queryBuilder(query), documentClass)
            },
            countQuery = { query ->
                mongoTemplate.count(queryBuilder(query), documentClass)
            }
        )
    }
}
```

설명 블라블라



