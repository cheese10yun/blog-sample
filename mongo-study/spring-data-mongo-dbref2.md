## Lookup 방식의 리턴 타입 문제와 개선 방안

기존 구현에서는 Lookup 결과의 리턴 타입을 `PostProjectionLookup`과 같이 별도의 Projection 객체로 지정했습니다. 이 방식은 원래의 `Post` 도큐먼트에 정의된 메서드를 그대로 사용할 수 없다는 단점이 있습니다. 이 문제를 해결하기 위해 리턴 타입을 `Post` 객체로 지정할 수 있습니다.

```kotlin
class PostCustomRepositoryImpl(mongoTemplate: MongoTemplate) : PostCustomRepository, MongoCustomRepositorySupport<Post>(
    Post::class.java,
    mongoTemplate
) {
    override fun findLookUp(limit: Int): List<Post> {
        val lookupStage = Aggregation.lookup(
            "author",        // from: 실제 컬렉션 이름
            "author.\$id",   // localField: DBRef에서 _id가 들어있는 위치
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
                Post.DOCUMENT_NAME,  // 컬렉션 이름
                Post::class.java     // 리턴 타입을 Post 객체로 지정
            )
            .mappedResults
    }
}
```

매번 Projection 객체를 새로 생성하는 방식은 번거로울 뿐만 아니라, `Post` 도큐먼트에 정의된 도메인 로직을 활용할 수 없게 만듭니다. 또한, 상속을 통해 메서드를 재사용하는 방법도 재사용을 위해 억지로 상속을 적용하는 것이므로 최적의 해결책이라고 보기 어렵습니다. 가장 좋은 방법은 aggregate 결과를 바로 `Post` 객체로 리턴하는 것입니다. 이렇게 하면 도메인 로직을 그대로 유지하면서 Lookup 방식의 장점도 함께 활용할 수 있습니다.

**중요한 것은 포인트는 `Post` 클래스 내에서 author 필드를 다음과 같이 lazy로 설정하는 점입니다.**

```kotlin
@Document(collection = "post")
class Post(
    ...
    @DBRef(lazy = true)
    val author: Author,
) : Auditable()
```

이렇게 하면 일반적인 find 조회에서는 author에 접근하기 전까지 추가 쿼리가 발생하지 않아 N+1 문제를 회피할 수 있고, 필요할 때는 Lookup을 통해 author 정보도 함께 조회할 수 있습니다.

단, lazy로 설정되어 있기 때문에 find 쿼리 이후 author 객체에 접근하면 추가적인 N+1 문제가 발생할 수 있으므로, 이 방식은 N+1 문제를 원천적으로 해결하는 방법은 아니며 잠재적인 N+1 문제가 남아 있다는 점을 유념해야 합니다.