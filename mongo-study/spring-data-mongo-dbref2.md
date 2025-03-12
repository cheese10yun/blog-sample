### $lookup 기반 연관 객체 조회 Code

아래 코드는 Spring MVC 컨트롤러에서 `$lookup`을 사용하여 Post와 Author 데이터를 한 번의 Aggregation 쿼리로 조회하는 예시입니다. 이 방식은 DBRef 방식에서 발생하는 N+1 문제를 효과적으로 회피합니다.

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
    val author: AuthorProjection,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
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
                Post.DOCUMENT_NAME,               // 컬렉션 이름
                PostProjectionLookup::class.java,
            )
            .mappedResults
    }
}
```

**getPostsLookUp** 메서드는 단일 Aggregation 쿼리를 통해 Post와 Author 데이터를 한 번에 조회합니다. 이 과정에서 `$lookup` 연산자가 두 컬렉션 간의 조인을 수행하고, `$unwind`를 사용해 조인된 Author 데이터를 평탄화합니다. 결과적으로, 모든 연관 데이터를 한 번에 가져오기 때문에 각 Post마다 별도의 Author 조회 쿼리가 실행되는 **N+1 문제가 발생하지 않으며,** 대량의 데이터를 조회하는 상황에서도 성능 저하를 효과적으로 방지할 수 있습니다.

실제 동작이 어떻게 나가는지 본격적으로 살펴보겠습니다.