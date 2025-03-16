## $lookup 방식의 리턴 타입 문제와 개선 방안

기존 구현에서는 `$lookup` Aggregation 쿼리 결과의 리턴 타입을 `PostProjectionLookup`과 같이 별도의 Projection 객체로 지정했습니다. 그러나 이 방식은 Post 도큐먼트 내에 정의된 비즈니스 로직(DDD 관점에서 핵심 도메인 기능)을 그대로 활용할 수 없다는 단점이 있습니다. 즉, Post 객체가 갖는 도메인 메서드를 통해 비즈니스 규칙을 적용할 수 없어 객체지향 설계 원칙에 위배되는 문제가 발생합니다.

또한, 상속을 통해 메서드를 재사용하는 방법은 재사용을 위해 인위적으로 상속 관계를 도입하는 것이므로 최적의 해결책이라고 보기 어렵습니다. 이러한 문제를 해결하기 위해 가장 좋은 방법은 Aggregation 쿼리의 결과를 바로 `Post` 객체로 리턴하는 것입니다. 이렇게 하면 Post 도큐먼트에 정의된 도메인 로직을 그대로 유지할 수 있으며, `$lookup` 방식의 장점을 활용해 N+1 문제를 회피할 수 있습니다.

```kotlin
class PostCustomRepositoryImpl(mongoTemplate: MongoTemplate) : PostCustomRepository, MongoCustomRepositorySupport<Post>(
    Post::class.java,
    mongoTemplate
) {
    override fun findLookUp(limit: Int): List<Post> {
        val lookupStage = Aggregation.lookup(
            "author",        // from: 실제 컬렉션 이름
            "author.$id",    // localField: DBRef에서 _id가 들어있는 위치
            "_id",           // foreignField: authors 컬렉션의 _id
            "author"         // as: 결과를 저장할 필드 이름
        )
        val unwindStage = Aggregation.unwind("author", true)
        val projection = Aggregation.project()
            .andInclude("title")
            .andInclude("content")
            .andInclude("author")
            .andInclude("updated_at")
            .andInclude("created_at")
        val limitStage = Aggregation.limit(limit.toLong())
        val aggregation = Aggregation.newAggregation(lookupStage, unwindStage, projection, limitStage)
        return mongoTemplate
            .aggregate(
                aggregation,
                "post",
                Post::class.java     // 리턴 타입을 Post 객체로 지정
            )
            .mappedResults
    }
}
```

이와 같이 구현하면, Post 객체에 내장된 도메인 로직을 그대로 활용할 수 있으며, 불필요한 상속 관계를 도입하지 않고도 `$lookup` 방식을 통해 연관 데이터를 한 번에 조회할 수 있습니다.

**중요한 것은, Post 클래스 내에서 author 필드를 다음과 같이 lazy로 설정하는 점입니다.**

```kotlin
@Document(collection = "post")
class Post(
    // ...
    @DBRef(lazy = true)
    val author: Author,
)
```

이렇게 하면, 일반적인 find 조회에서는 author 필드에 접근하기 전까지 추가 쿼리가 발생하지 않아 N+1 문제를 어느 정도 회피할 수 있고, 필요할 때는 `$lookup`을 통해 author 정보도 함께 조회할 수 있습니다. 단, lazy로 설정되어 있기 때문에 find 쿼리 이후 author 객체에 접근하면 추가적인 N+1 문제가 발생할 수 있으므로, 이 방식은 N+1 문제를 완전히 해결하는 것은 아니며 잠재적인 문제가 남아 있음을 유념해야 합니다. 이러한 잠재적 문제를 근본적으로 해결하려면, 객체 간의 연관관계를 맺지 않고 단순한 값 객체인 `author_id`를 보유하는 모델링 방식을 고려해야 합니다.

예를 들어, 아래와 같이 Post 도큐먼트를 구성할 수 있습니다.

```kotlin
@Document(collection = "post")
class Post(
    // ...
    @Field(name = "author_id")
    val authorId: ObjectId
)
```

이 경우, Post는 단순 값 객체인 `author_id`만을 저장하며, Author 객체에 접근이 필요한 경우에는 `$lookup`을 통해 두 컬렉션을 조인하여 데이터를 가져올 수 있습니다.

또한, 만약 `@DBRef(lazy = true)`로 연관관계를 설정하는 것을 선택한다면, `MongoRepository`에서 제공하는 기본 메서드들(예: `findByIdOrNull`)을 `$lookup` 기반으로 재정의하여 한 번의 Aggregation 쿼리로 연관 데이터를 조회하도록 구현하는 것도 좋은 선택입니다.

```kotlin
class PostCustomRepositoryImpl(mongoTemplate: MongoTemplate) : PostCustomRepository, MongoCustomRepositorySupport<Post>(
    Post::class.java,
    mongoTemplate
) {
    override fun findByIdOrNull(id: ObjectId): Post? {
        val match = Aggregation.match(Criteria.where("_id").`is`(id))
        val lookupStage = Aggregation.lookup(
            "author",
            "author.$id",
            "_id",
            "author"
        )
        val unwindStage = Aggregation.unwind("author", true)
        val projection = Aggregation.project()
            .andInclude("title", "content", "author", "updated_at", "created_at")
        val aggregation = Aggregation.newAggregation(match, lookupStage, unwindStage, projection)
        return mongoTemplate
            .aggregate(
                aggregation,
                "post",
                Post::class.java
            )
            .uniqueMappedResult
    }
}
```

이와 같이 구현하면, `@DBRef(lazy = true)`를 사용하는 경우에도 기본 메서드를 `$lookup` 기반으로 재정의하여 한 번의 Aggregation 쿼리로 연관 데이터를 조회할 수 있습니다. 이를 통해 N+1 문제를 효과적으로 회피하면서 Post 도큐먼트에 정의된 도메인 로직도 그대로 활용할 수 있습니다.

---

## 결론

이번 글에서는 Spring Data MongoDB에서 `@DBRef`를 활용한 객체 간 연관관계 설정, 이로 인한 N+1 문제, 그리고 이를 해결하기 위한 다양한 최적화 전략을 살펴보았습니다.

- **@DBRef 방식의 한계:**  
  `@DBRef`는 연관 문서를 로딩할 때 각 Post마다 추가 조회 쿼리가 실행되어 N+1 문제가 발생할 위험이 있습니다.

- **Lazy 옵션의 한계:**  
  `@DBRef(lazy = true)`를 사용해도, 실제 Author 데이터에 접근하는 시점에서는 추가 쿼리가 발생하여 N+1 문제가 완전히 해소되지는 않습니다.

- **효과적인 대안:**  
  MongoDB의 `$lookup` 연산자를 활용하여 단일 Aggregation 쿼리로 Post와 Author 데이터를 한 번에 조회하면, 각 Post마다 별도의 조회 쿼리 실행을 방지할 수 있어 N+1 문제를 효과적으로 회피할 수 있습니다.

- **대체 모델링 접근:**  
  Post 도큐먼트에서 연관 객체를 `@DBRef` 대신 단순 값 객체인 `author_id`로 보유하거나, Repository 기본 메서드를 `$lookup` 기반으로 재정의하는 방법도 고려할 수 있습니다.

- **DDD 관점의 고려:**  
  객체 간 연관관계를 단순 성능 문제만으로 판단하기보다는, DDD에서 제시하는 에그리거트 경계를 기준으로 도메인 모델을 구성하는 접근을 통해 도메인 로직과 성능 최적화를 균형 있게 반영할 수 있습니다.

이와 같이 다양한 전략을 상황에 맞게 조합하면, N+1 문제를 최소화하면서도 도메인 로직을 효과적으로 유지할 수 있는 유연한 설계를 구현할 수 있습니다.