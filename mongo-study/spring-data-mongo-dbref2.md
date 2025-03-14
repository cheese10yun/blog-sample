### $lookup 기반 연관 객체 조회 Code 및 활용 예시

아래 코드는 Spring MVC 컨트롤러에서 `$lookup`을 사용하여 Post와 Author 데이터를 한 번의 Aggregation 쿼리로 조회하는 예시입니다. 이 방식은 기존의 `@DBRef` 방식을 사용하여 발생하는 N+1 문제를 효과적으로 회피합니다.

```kotlin
@RestController
@RequestMapping("/posts")
class PostController(
    private val postRepository: PostRepository,
) {

    @GetMapping("/lookup")
    fun getPostsLookUp(@RequestParam(name = "limit") limit: Int): List<Post> = postRepository.findLookUp(limit)
}

data class PostProjectionLookup(
    val id: ObjectId,
    val title: String,
    val content: String,
    val author: AuthorProjection
)

interface PostCustomRepository {
    fun findLookUp(limit: Int): List<PostProjectionLookup>
}

class PostCustomRepositoryImpl(mongoTemplate: MongoTemplate) : PostCustomRepository, MongoCustomRepositorySupport<Post>(
    Post::class.java,
    mongoTemplate
) {

    override fun findLookUp(limit: Int): List<PostProjectionLookup> {
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
        val limitStage = Aggregation.limit(limit.toLong())
        val aggregation = Aggregation.newAggregation(lookupStage, unwindStage, projection, limitStage)
        return mongoTemplate
            .aggregate(
                aggregation,
                Post.DOCUMENT_NAME,               // 컬렉션 이름
                PostProjectionLookup::class.java,
            )
            .mappedResults
    }
}
```

**getPostsLookUp** 메서드는 단일 Aggregation 쿼리를 통해 Post와 Author 데이터를 한 번에 조회합니다. 이 과정에서 `$lookup` 연산자가 두 컬렉션 간의 조인을 수행하고, `$unwind`를 사용해 조인된 Author 데이터를 평탄화합니다. 결과적으로 모든 연관 데이터를 한 번에 가져오기 때문에 각 Post마다 별도의 Author 조회 쿼리가 실행되는 N+1 문제가 발생하지 않으며, 대량의 데이터를 조회하는 상황에서도 성능 저하를 효과적으로 방지할 수 있습니다.

또한, 아래와 같이 단순히 `author_id`만을 저장하는 방식으로 도메인 모델을 구성한 경우에도, `$lookup`을 통해 연관 Author 데이터를 조회할 수 있습니다. 이는 반드시 `@DBRef`로 연관관계를 매핑할 필요 없이, 단순 ID 저장 방식만으로도 연관 데이터를 조인할 수 있음을 보여줍니다.

```kotlin
@Document(collection = "post")
class Post(
    @Id
    var id: ObjectId? = null,
    @Field(name = "title", targetType = FieldType.STRING)
    val title: String,
    @Field(name = "content", targetType = FieldType.STRING)
    val content: String,
    @Field(name = "author_id")
    val authorId: ObjectId
)
```

이와 같이 `$lookup` 기반의 조회 방식은 연관 문서 매핑 없이도 단일 Aggregation 쿼리로 Post와 Author 데이터를 한 번에 가져올 수 있으므로, N+1 문제를 근본적으로 해결할 수 있는 효과적인 대안임을 강조할 수 있습니다.