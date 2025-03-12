# Spring Data MongoDB에서 N+1 문제 다루기: 성능 이슈와 최적화 전략

Spring Data MongoDB에서는 `@DBRef` 애노테이션을 통해 객체 간의 연관관계를 손쉽게 구성할 수 있습니다. 이 방식은 문서 내에 참조 대상 컬렉션의 이름과 ID를 함께 저장하여 필요 시 연관 문서를 자동으로 로딩해주는 편리함을 제공합니다. 예를 들어, DBRef 방식으로 저장된 Post 문서는 아래와 같이 `$ref` 필드에 참조할 컬렉션의 이름과 `$id` 필드에 실제 참조 대상 문서의 ID를 저장합니다.

```json
{
  "_id": {
    "$oid": "67c49519bb7bbd62011d7b13"
  },
  "author": {
    "$ref": "author",
    "$id": "67c49518bb7bbd62011d7b0e"
  },
  "content": "content-1",
  "title": "title-1"
}
```

이와 같이 DBRef는 연관 문서의 위치 정보를 함께 관리하기 때문에, 단순히 ID만 저장하는 방식에 비해 연관 문서를 손쉽게 로딩할 수 있습니다. 그러나 내부적으로는 각 문서를 조회할 때마다 별도의 추가 조회 쿼리가 발생할 수 있어, 특히 대량의 데이터를 조회하는 경우 N+1 문제가 발생할 위험이 있습니다.

반면, 단순 ObjectId 직접 참조 방식은 문서에 참조 대상 문서의 ObjectId만 저장합니다. 예를 들어, ObjectId 직접 참조 방식으로 저장된 Post 문서는 다음과 같이 구성됩니다.

```json
{
  "_id": "67c49519bb7bbd62011d7b13",
  "author_id": "67c49518bb7bbd62011d7b0e",
  "content": "content-1",
  "title": "title-1"
}
```

이 방식은 메타 정보 없이 가볍게 저장되므로 데이터 구조가 단순하지만, 연관 문서를 로딩할 때는 `$lookup`과 같은 Aggregation 파이프라인이나 별도의 애플리케이션 로직을 구현해야 하므로 자동 연관 문서 로딩의 편리함은 떨어집니다.

본 글에서는 주로 `@DBRef`를 통한 DBRef 방식에 대해 중점적으로 다루면서, DBRef 방식이 제공하는 자동 로딩의 편의성과 함께 발생할 수 있는 N+1 문제에 대해 상세히 분석할 예정입니다. 또한, DBRef 방식과 단순 ObjectId 직접 참조 방식 간의 차이점을 비교하고, 실제 성능 테스트 결과를 바탕으로 대규모 데이터 조회 상황에서 어느 방식이 보다 효율적인지, 그리고 이를 최적화하기 위한 전략에 대해 구체적으로 설명할 것입니다.

## @DBRef를 통한 자동 연관 문서 로딩

DBRef 방식은 Spring Data MongoDB에서 객체 간의 연관관계를 손쉽게 표현할 수 있도록 해줍니다. 이 방식은 문서 내부에 참조 대상 컬렉션의 이름과 ID를 함께 저장하여, 필요할 때 연관 문서를 자동으로 로딩할 수 있습니다. 예를 들어, 아래의 코드에서는 Post 도큐먼트에서 `@DBRef` 애노테이션을 사용하여 Author 객체를 참조하고 있습니다.

```kotlin
@Document(collection = "post")
class Post(
    @Field(name = "title")
    val title: String,
    @Field(name = "content")
    val content: String,
    @DBRef(lazy = false)
    val author: Author
) : Auditable()

@Document(collection = "author")
class Author(
    @Field(name = "name")
    val name: String
) : Auditable()
```

이 코드에서 `lazy = false`로 설정되어 있기 때문에, Post를 조회할 때 관련 Author 문서가 함께 즉시 로딩됩니다. 그러나 자동 로딩 기능은 각 문서를 조회할 때마다 추가 조회 쿼리를 발생시킬 수 있어, 많은 데이터를 조회하는 경우 N+1 문제가 발생할 위험이 있습니다. 따라서, 상황에 따라 `lazy` 옵션을 적절히 설정하여 불필요한 데이터 로딩을 방지하는 것이 중요합니다.

## @DBRef 기반 연관 객체 조회

`@DBRef(lazy = false)`를 사용하면 Post를 조회할 때 연관된 Author 문서를 즉시 로딩합니다. 이 경우, Post마다 Author를 조회하기 때문에 다수의 Post를 조회하면 각각에 대해 별도의 Author 조회 쿼리가 발생하여 N+1 문제가 발생합니다.

반면, `@DBRef(lazy = true)`를 사용하면 Post를 조회할 때 Author 필드는 즉시 로딩되지 않고, 실제로 해당 필드에 접근하는 시점에서 별도의 쿼리가 실행됩니다. 이 방식은 초기 조회 시 불필요한 데이터를 로딩하지 않아 응답 속도가 빠를 수 있으나, Post 객체를 JSON으로 시리얼라이즈하거나 Author 필드에 접근하는 경우 예기치 못한 추가 쿼리가 발생할 위험이 있습니다.

결국, `lazy = false`와 `lazy = true` 설정 모두 각기 다른 방식으로 N+1 문제가 발생할 수 있는 잠재적인 한계를 지니므로, 상황에 따라 신중하게 선택하고 최적화 전략을 고려해야 합니다. 어떤 방식으로 쿼리가 발생하는지 살펴보겠습니다.

### 연관 객체 조회 쿼리

```javascript
db.post.find({}).limit(500)

// limit 만큼 반복
db.author.find({_id: ObjectId("67cd82c3aec68267745dcf85")}).limit(1)
db.author.find({_id: ObjectId("67cd82c3aec68267745dcf84")}).limit(1)
db.author.find({_id: ObjectId("67cd82c3aec68267745dcf83")}).limit(1)
```

위 예시는 Post를 조회한 후, 별도의 쿼리로 Author 문서를 조회하는 과정을 보여줍니다. 만약 500개의 Post를 조회한다면, **각 Post마다 Author 조회 쿼리가 실행되어 총 500번의 `db.author.find` 쿼리가 발생하게 됩니다.** 이처럼 Post 조회 결과의 수에 따라 반복적으로 Author 조회 쿼리가 실행되면 **N+1 문제가 발생**하여 성능 저하를 초래할 수 있습니다.

### 연관 객체 조회 Code

아래 코드는 Spring MVC 컨트롤러에서 Post 도큐먼트를 조회한 후, 두 가지 방식의 응답 처리를 비교하는 예시입니다. `getPostWithAuthor` 메서드는 전체 Post 객체를 리턴하여 JSON 시리얼라이즈 과정에서 Author 필드에 접근하게 됩니다. 반면, `getPostOnly` 메서드는 Projection 패턴을 활용하여 핵심 데이터만을 담은 PostProjection 객체를 리턴하므로, Author 필드에 접근하지 않아 추가 쿼리가 발생하지 않습니다.

```kotlin
@RestController
@RequestMapping("/posts")
class PostController(
    private val postRepository: PostRepository,
) {

    @GetMapping("/post-with-author")
    fun getPostWithAuthor(@RequestParam(name = "limit") limit: Int): List<Post> = postRepository.find(limit)

    @GetMapping("/post-only")
    fun getPostOnly(@RequestParam(name = "limit") limit: Int): List<PostProjection> = postRepository.find(limit).map { PostProjection(it) }
}

data class PostProjection(
    val id: ObjectId,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    constructor(post: Post) : this(
        id = post.id!!,
        title = post.title,
        content = post.content,
        createdAt = post.createdAt,
        updatedAt = post.updatedAt,
    )
}

class PostCustomRepositoryImpl(mongoTemplate: MongoTemplate) : PostCustomRepository, MongoCustomRepositorySupport<Post>(
    Post::class.java,
    mongoTemplate
) {

    override fun find(limit: Int): List<Post> {
        return mongoTemplate.find(Query().limit(limit))
    }
}
```

* **getPostWithAuthor**:
  - 리턴 타입이 `List<Post>`이므로, Post 객체를 JSON으로 시리얼라이즈하는 과정에서 Author 필드에 접근하게 됩니다.
  - 이로 인해 `lazy = false`든 `lazy = true`든 상관없이, 각 Post마다 별도의 `db.author.find` 쿼리가 실행되어 N+1 문제가 반복됩니다.

* **getPostOnly**:
  - 리턴 타입이 `List<PostProjection>`이므로, 응답에 필요한 핵심 데이터만 반환되고 Author 필드에 직접 접근하지 않습니다.
  - 따라서, lazy = true인 경우 추가적인 Author 조회 쿼리가 발생하지 않아 N+1 문제를 회피할 수 있습니다.

실제 동작이 어떻게 나가는지 본격적으로 살펴보겠습니다.

### Eager 로딩, @DBRef(lazy = false) 결과

아래 이미지는 Eager 로딩 방식으로 Post를 조회할 때 Author 문서까지 함께 로딩되는 쿼리 흐름을 보여줍니다. 쿼리 로그에서 Post와 Author에 대해 별도의 쿼리가 발생하는 것을 확인할 수 있으며, 여러 Post를 조회할 경우 N+1 문제가 명확하게 드러납니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-3.png)

### Lazy 로딩, @DBRef(lazy = true) 결과

아래 이미지는 Post를 조회한 후 실제로 Author 필드에 접근할 때 별도의 Author 조회 쿼리가 실행되는 과정을 보여줍니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-1.png)

`@DBRef(lazy = true)`를 사용하더라도, Post 객체에서 Author 필드에 접근하면 추가 쿼리가 발생합니다. 예를 들어, `db.post.find({}).limit(1000)` 쿼리는 23ms 정도 소요되지만, 각 Post의 Author를 조회하는 쿼리 `db.author.find({_id: ObjectId("")}).limit(1)`가 1,000번 실행되어 총 2,085ms가 소요됩니다.

이처럼 실제 Author 필드에 접근하는 경우, `@DBRef(lazy = false)`와 유사한 성능 저하가 발생하게 됩니다.

반면, Post 조회 시 Author 필드에 전혀 접근하지 않으면 불필요한 Author 조회 쿼리가 발생하지 않아 응답 속도가 훨씬 빨라집니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-2.png)

아래 이미지는 이러한 상황을 보여주며, `db.post.find({}).limit(1000)` 쿼리가 실행되어 약 22.7ms 정도 소요되었고, Author에 대한 접근이 없으므로 추가 쿼리가 발생하지 않은 것을 확인할 수 있습니다.

#### Lazy 로딩을 위한 프록시 구성 방법

Kotlin에서는 클래스가 기본적으로 final로 선언됩니다. 즉, 특별한 설정 없이 작성된 클래스는 확장(상속)이 불가능한 상태입니다. Proxy 기반의 Lazy 로딩 기능은 실제 객체 대신 프록시 객체를 생성하여 해당 객체의 속성에 접근할 때 실제 데이터를 로딩하는 방식으로 동작하는데, 이를 위해서는 대상 클래스가 open이어야 합니다. 만약 클래스가 final이라면, 프록시 객체를 생성할 수 없으므로 Lazy 로딩이 제대로 동작하지 않습니다.

이를 해결하기 위해 Kotlin에서는 `all-open` 플러그인 또는 `kotlin-spring` 플러그인을 적용하여, 특정 애노테이션(예: `@Document`)이 붙은 클래스를 자동으로 open으로 변환할 수 있습니다. 아래 예시는 이러한 설정을 적용하는 방법을 보여줍니다.

```kotlin
plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.6.21"
    // 또는 id("org.jetbrains.kotlin.plugin.allopen") ...
}

allOpen {
    annotation("org.springframework.data.mongodb.core.mapping.Document")
}
```

이 설정을 적용하면, `@Document` 애노테이션이 붙은 클래스들은 자동으로 open으로 처리되어 프록시 객체 생성이 가능해집니다. 즉, Lazy 로딩이 원활하게 동작할 수 있게 됩니다.

- **allOpen 미적용 시**: 클래스는 기본적으로 final 상태이며, 이로 인해 Proxy 기반의 Lazy 로딩이 불가능합니다.
- **allOpen 적용 시**: 클래스가 open으로 변환되어 Lazy 로딩을 위한 Proxy 객체 생성이 가능해집니다.

따라서, Spring Data MongoDB에서 Lazy 로딩 기능을 활용하려면 반드시 이러한 설정을 통해 대상 도메인 클래스가 open 상태로 유지되도록 해야 합니다.

## $lookup 기반 연관 객체 조회

아래 코드는 `$lookup`을 통해 Post와 Author 데이터를 조인하여 단일 Aggregation 쿼리로 필요한 데이터를 한 번에 조회하는 예시입니다. 특히 대량의 데이터를 조회할 때 DBRef 방식에서 발생하는 N+1 문제를 해결하는 데 유리합니다.

### 연관 객체 조회 쿼리

```javascript
db.post.aggregate(
    [
        {
            "$lookup": {
                "from": "author",
                "localField": "author.$id",
                "foreignField": "_id",
                "as": "author"
            }
        },
        {
            "$unwind": {
                "path": "$author",
                "preserveNullAndEmptyArrays": true
            }
        },
        {
            "$project": {
                "title": 1.0,
                "content": 1.0,
                "author": 1.0,
                "updated_at": 1.0,
                "created_at": 1.0
            }
        },
        {
            "$limit": 500.0
        }
    ]
)
```

이 예제는 `$lookup`을 사용하여 Post와 Author 컬렉션을 조인하는 방법을 보여줍니다. 단일 Aggregation 파이프라인으로 모든 연관 데이터를 한 번에 가져올 수 있어 N+1 문제를 효과적으로 회피할 수 있습니다.

### 연관 객체 조회 Code

```kotlin
@RestController
@RequestMapping("/posts")
class PostController(
    private val postRepository: PostRepository
) {
    @GetMapping("/lookup")
    fun getPostsLookUp(@RequestParam(name = "limit") limit: Int) = postRepository.findLookUp(limit)
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
            "author.\$id",    // localField: DBRef에서 _id가 들어있는 위치
            "_id",            // foreignField: authors 컬렉션의 _id
            "author"       // as: 결과를 저장할 필드 이름
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

이 코드는 `$lookup`을 통해 Post와 Author 데이터를 한 번에 조회하여 단일 Aggregation 쿼리로 연관 데이터를 가져오므로 N+1 문제를 피할 수 있습니다.

### $lookup 결과

아래 이미지는 `$lookup` 방식으로 조회한 결과를 보여줍니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-4.png)

## 성능 측정

아래 이미지는 여러 조회 조건에 대해 평균 응답 시간을 시각적으로 비교한 벤치마크 결과를 보여줍니다. 각 조회 조건마다 10번씩 테스트한 후 그 평균값을 사용하여 성능을 측정했습니다. 결과에서는 MongoDB의 $lookup 방식을 사용한 경우, DBRef를 이용하여 즉시 로딩한 경우, 그리고 DBRef의 lazy 로딩을 적용한 경우 중 실제로 author 필드에 접근한 경우와 접근하지 않은 경우의 성능 차이를 비교하고 있습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-5.png)

| rows  | $lookup | DBRef lazy false | DBRef lazy true(author 접근) | DBRef lazy true(author 미접근) |
|-------|---------|------------------|----------------------------|-----------------------------|
| 1     | 9.2ms   | 9.6ms            | 9.3ms                      | 8.5ms                       |
| 50    | 11.6ms  | 69.7ms           | 69.4ms                     | 8.9ms                       |
| 100   | 16.2ms  | 130.1ms          | 133.5ms                    | 11.5ms                      |
| 500   | 42.2ms  | 574.2ms          | 575.9ms                    | 23.5ms                      |
| 1,000 | 69.5ms  | 1167.4ms         | 1178.3ms                   | 41.9ms                      |
| 5,000 | 257.2ms | 6043.1ms         | 6181.5ms                   | 129.6ms                     |

성능 테스트 결과를 요약하면, 단일 문서 조회에서는 모든 방식이 거의 동일한 응답 속도를 보입니다.

하지만 **조회 대상 문서 수가 늘어나면 각 방식 간의 성능 차이가 더욱 뚜렷하게 나타납니다.** 예를 들어, DBRef를 lazy로 설정하고 author 필드에 접근하지 않는 경우는 Post 도큐먼트에 대한 단순 find 쿼리만 실행되므로 가장 빠른 응답 속도를 기록합니다.

반면, DBRef 방식에서 실제로 author 필드에 접근하면, 각 Post마다 추가 쿼리가 실행되어 N+1 문제가 발생합니다. **또한, $lookup 방식은 aggregate 파이프라인을 통한 조인으로 데이터를 한 번에 가져올 수 있어 N+1 문제를 회피할 수 있습니다.**

특히, 1,000건을 조회할 때 약 1,000ms 정도의 응답 속도는 너무 느려 실제 서비스에 적용하기 어려울 수 있기 때문에 **$lookup 방식이 가장 현실적인 대안으로 평가될 수 있습니다.**

## $lookup 방식의 리턴 타입 문제와 개선 방안

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

매번 Projection 객체를 새로 생성하는 방식은 번거로울 뿐만 아니라, `Post` 도큐먼트에 정의된 도메인 로직을 활용할 수 없게 만듭니다. 또한, 상속을 통해 메서드를 재사용하는 방법도 재사용을 위해 억지로 상속을 적용하는 것이므로 최적의 해결책이라고 보기 어렵습니다. 가장 좋은 방법은 aggregate 결과를 바로 `Post` 객체로 리턴하는 것입니다. 이렇게 하면 도메인 로직을 그대로 유지하면서 $lookup 방식의 장점도 함께 활용할 수 있습니다.

**중요한 것은 포인트는 `Post` 클래스 내에서 author 필드를 다음과 같이 lazy로 설정하는 점입니다.**

```kotlin
@Document(collection = "post")
class Post(
    ...
    @DBRef(lazy = true
)

val author: Author,
)
```

이렇게 하면 일반적인 find 조회에서는 author에 접근하기 전까지 추가 쿼리가 발생하지 않아 N+1 문제를 회피할 수 있고, 필요할 때는 $lookup 통해 author 정보도 함께 조회할 수 있습니다.

단, lazy로 설정되어 있기 때문에 find 쿼리 이후 author 객체에 접근하면 추가적인 N+1 문제가 발생할 수 있으므로, 이 방식은 N+1 문제를 원천적으로 해결하는 방법은 아니며 **잠재적인 N+1 문제가 남아 있다는 점을 유념해야 합니다.**

## 결론

??