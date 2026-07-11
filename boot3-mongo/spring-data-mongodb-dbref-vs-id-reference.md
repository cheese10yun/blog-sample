# Spring Data MongoDB 연관관계 설계: @DBRef vs ID 참조, 그리고 N+1 문제

## 시작하며

MongoDB에서는 비정규화를 지향하기 때문에 RDBMS처럼 조인을 빈번하게 사용하지는 않지만, 도큐먼트를 조인해서 조회하고 싶은 상황은 종종 발생합니다. 이때 Spring Data MongoDB 환경에서 연관관계를 맺는 방법은 크게 두 가지입니다. `@DBRef`를 이용해 객체를 직접 참조하는 방식과, 단순히 연관 도큐먼트의 ID 값만 보유하는 방식입니다. 어떤 방식을 선택하느냐에 따라 N+1 문제가 발생할 수 있으며, 이는 서비스 응답 속도에 큰 영향을 미칩니다.

본 글에서는 @DBRef를 통한 객체 연관관계 매핑에서 발생하는 N+1 문제와 그 한계를 구체적으로 살펴보고, lazy 옵션 활용, Aggregation의 $lookup 연산자 기반 조회, 단순 ID 참조 모델링 등 다양한 최적화 전략을 코드 예시와 함께 소개합니다. 또한, 각 방식의 성능을 측정한 벤치마크 결과를 통해 실무에서 적용 가능한 성능 개선 효과를 명확히 전달하고, 나아가 **DDD의 바운디드 컨텍스트와 애그리거트 경계를 기준으로 어떤 참조 방식을 선택해야 하는지**에 대한 설계 관점의 가이드를 제시하고자 합니다.

## MongoDB에서 연관관계를 맺는 두 가지 방식

Spring Data MongoDB에서 도큐먼트 간의 연관관계를 표현하는 방법은 크게 두 가지입니다.

1. **@DBRef 기반 객체 참조 방식**: 도큐먼트가 연관 객체를 직접 참조하며, 조회 시 연관 도큐먼트를 자동으로 로딩
2. **단순 ID 참조 방식**: 도큐먼트가 연관 도큐먼트의 ID 값만 보유하며, 연관 데이터가 필요할 때 명시적으로 조회

### @DBRef 기반 객체 참조 방식

Spring Data MongoDB에서는 `@DBRef` 애노테이션을 사용하여 객체 간의 연관관계를 정의할 수 있습니다. 이 방식은 문서 내부에 참조 대상 컬렉션의 이름과 해당 문서의 ID를 함께 저장함으로써, 필요 시 연관 문서를 로딩할 수 있도록 지원합니다. 예를 들어, 아래 JSON 예시와 같이 Post 문서의 `author` 필드에 `"author"`라는 컬렉션 이름과 실제 Author 문서의 ID가 기록됩니다.

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

이 방식은 객체 그래프를 통해 연관 데이터에 접근할 수 있어 직관적이며, MongoDB의 데이터 모델을 명확하게 정의하는 데 도움을 줍니다. 하지만 연관 문서를 로딩할 때 별도의 추가 조회 쿼리가 실행되는 단점이 있습니다. 즉, Post 문서를 조회한 후 해당 Author 문서에 접근하면 각 Post마다 추가 조회가 발생하여, **특히 대량의 데이터를 다룰 때 ORM/ODM에서 흔히 발생하는 N+1 문제가 그대로 발생할 위험이 있습니다.**

### 단순 ID 참조 방식

반면 ID 참조 방식은 객체 간의 연관관계를 맺지 않고, 아래와 같이 연관 도큐먼트의 ID 값만 단순 필드로 보유합니다.

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

이 방식은 객체 그래프 탐색에 의한 자동 로딩 자체가 없기 때문에 N+1 문제가 원천적으로 발생하지 않습니다. 다만 Author 데이터가 필요한 경우에는 개발자가 직접 조회 쿼리를 작성해야 합니다.

### RDBMS의 조인과 MongoDB의 $lookup

두 방식의 차이를 이해하기 위해서는 MongoDB의 조인 방식을 짚고 넘어갈 필요가 있습니다. MySQL 같은 관계형 데이터베이스에서는 연관 데이터를 조인 쿼리 한 번으로 가져올 수 있습니다. 반면 MongoDB에서 두 컬렉션을 조인하려면 `$lookup` 연산자를 사용해야 하는데, **`$lookup`은 단순 find 쿼리에서는 사용할 수 없고 반드시 Aggregation Pipeline, 즉 aggregate 쿼리로 조회해야 합니다.**

여기서 @DBRef 방식의 구조적인 한계가 드러납니다. Spring Data MongoDB의 @DBRef 자동 로딩은 find 쿼리 기반으로 동작하기 때문에, 연관 도큐먼트를 조인해서 한 번에 가져오지 못하고 각 도큐먼트마다 추가 find 쿼리를 실행할 수밖에 없습니다. 이것이 바로 @DBRef 방식에서 N+1 문제가 구조적으로 발생하는 이유입니다.

앞으로 본 포스팅에서는 @DBRef 방식의 N+1 문제를 실제 쿼리와 벤치마크로 확인하고, $lookup 기반 조회와 ID 참조 방식이라는 대안을 살펴본 뒤, 마지막으로 DDD 관점에서 어떤 방식을 선택해야 하는지 정리하겠습니다.

## @DBRef를 통한 자동 연관 문서 로딩

DBRef 방식은 Spring Data MongoDB에서 객체 간의 연관관계를 손쉽게 표현할 수 있도록 해줍니다. 이 방식은 문서 내부에 참조 대상 컬렉션의 이름과 ID를 함께 저장하여, 필요할 때 연관 문서를 자동으로 로딩할 수 있습니다. 예를 들어, 아래의 코드에서는 Post 도큐먼트에서 `@DBRef` 애노테이션을 사용하여 Author 객체를 참조하고 있습니다.

```kotlin
@Document(collection = "post")
class Post(
    @Id
    var id: ObjectId? = null,
    @Field(name = "title")
    val title: String,
    @Field(name = "content")
    val content: String,
    @DBRef(lazy = false)
    val author: Author
)

@Document(collection = "author")
class Author(
    @Id
    var id: ObjectId? = null,
    @Field(name = "name")
    val name: String
)
```

이 코드에서 `lazy = false`로 설정되어 있기 때문에, Post를 조회할 때 관련 Author 문서가 함께 즉시 로딩됩니다. 그러나 자동 로딩 기능은 각 문서를 조회할 때마다 추가 조회 쿼리를 발생시킬 수 있어, 많은 데이터를 조회하는 경우 N+1 문제가 발생할 위험이 있습니다. 따라서, 상황에 따라 `lazy` 옵션을 적절히 설정하여 불필요한 데이터 로딩을 방지하는 것이 중요합니다.

## @DBRef 기반 연관 객체 조회

`@DBRef(lazy = false)`를 사용하면 Post를 조회할 때 연관된 Author 문서를 즉시 로딩합니다. 이 경우, Post마다 Author를 조회하기 때문에 다수의 Post를 조회하면 각각에 대해 별도의 Author 조회 쿼리가 발생하여 N+1 문제가 발생합니다.

반면, `@DBRef(lazy = true)`를 사용하면 Post를 조회할 때 Author 필드는 즉시 로딩되지 않고, 실제로 해당 필드에 접근하는 시점에서 별도의 쿼리가 실행됩니다. 이 방식은 초기 조회 시 불필요한 데이터를 로딩하지 않아 응답 속도가 빠를 수 있으나, Post 객체를 JSON으로 시리얼라이즈하거나 Author 필드에 접근하는 경우 예기치 못한 추가 쿼리가 발생할 위험이 있습니다.

**결국, `lazy = false`와 `lazy = true` 설정 모두 각기 다른 방식으로 N+1 문제가 발생할 수 있는 잠재적인 한계를 지니므로, 상황에 따라 신중하게 선택하고 최적화 전략을 고려해야 합니다.** 어떤 방식으로 쿼리가 발생하는지 살펴보겠습니다.

### 연관 객체 조회 쿼리

```javascript
db.post.find({}).limit(500)
// post rows 만큼 반복
db.author.find({_id: ObjectId("67cd82c3aec68267745dcf85")}).limit(1)
db.author.find({_id: ObjectId("67cd82c3aec68267745dcf84")}).limit(1)
db.author.find({_id: ObjectId("67cd82c3aec68267745dcf83")}).limit(1)
// ...
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
    val content: String
) {
    constructor(post: Post) : this(
        id = post.id!!,
        title = post.title,
        content = post.content,
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

아래 이미지는 `@DBRef(lazy = false)`을 사용하여 Post를 조회할 때, 연관된 Author 문서까지 별도의 쿼리로 로딩되는 과정을 보여줍니다. 쿼리 로그에서 Post와 Author 각각에 대해 별도의 조회가 발생하는 것을 확인할 수 있으며, 여러 Post를 조회할 경우 N+1 문제가 명확하게 드러납니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-3.png)

예를 들어, `db.post.find({}).limit(1000)` 쿼리는 약 17ms 정도 소요되지만, 각 Post마다 Author를 조회하는 `db.author.find({_id: ObjectId("")}).limit(1)` 쿼리가 1,000번 실행되어 총 2,361ms가 소요됩니다. 이 결과는 N+1 쿼리 문제로 인해 전체 조회 시간이 크게 늘어남을 명확하게 보여줍니다.

### Lazy 로딩, @DBRef(lazy = true) 결과

아래 이미지는 Post를 조회한 후 실제로 Author 필드에 접근할 때 별도의 Author 조회 쿼리가 실행되는 과정을 보여줍니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-1.png)

`@DBRef(lazy = true)`를 사용하더라도, Post 객체에서 Author 필드에 접근하면 추가 쿼리가 발생합니다. 예를 들어, `db.post.find({}).limit(1000)` 쿼리는 23ms 정도 소요되지만, 각 Post의 Author를 조회하는 쿼리 `db.author.find({_id: ObjectId("")}).limit(1)`가 1,000번 실행되어 총 2,085ms가 소요됩니다.

이처럼 실제 Author 필드에 접근하는 경우, `@DBRef(lazy = false)`와 유사한 성능 저하가 발생하게 됩니다.

반면, Post 조회 시 Author 필드에 전혀 접근하지 않으면 불필요한 Author 조회 쿼리가 발생하지 않아 응답 속도가 훨씬 빨라집니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-2.png)

아래 이미지는 이러한 상황을 보여주며, `db.post.find({}).limit(1000)` 쿼리가 실행되어 약 22.7ms 정도 소요되었고, Author에 대한 접근이 없으므로 추가 쿼리가 발생하지 않은 것을 확인할 수 있습니다.

#### Lazy 로딩 활성화를 위한 Proxy 설정 방법

Kotlin에서는 클래스가 기본적으로 final로 선언되기 때문에, 특별한 설정 없이 작성된 클래스는 상속이 불가능합니다. Proxy 기반의 Lazy 로딩은 실제 객체 대신 프록시 객체를 생성하여 해당 객체의 속성에 접근할 때 실제 데이터를 로딩하는 방식으로 동작합니다. 이를 위해서는 대상 클래스가 open이어야 하는데, 만약 클래스가 final이면 프록시 객체를 생성할 수 없으므로 Lazy 로딩이 제대로 동작하지 않습니다.

예를 들어, Author 클래스가 final 상태라면 Spring Data MongoDB는 CGLIB를 사용해 해당 클래스를 상속하는 프록시 객체를 생성하려 할 때 다음과 같은 오류가 발생합니다.

```
java.lang.IllegalArgumentException: Cannot subclass final class com.example.mongostudy.dbref.Author
    at org.springframework.cglib.proxy.Enhancer.generateClass(Enhancer.java:660)
    ...
```

이를 해결하기 위해 Kotlin에서는 `all-open`(또는 그 상위 preset인 `kotlin-spring`) 플러그인을 적용하여, 특정 애노테이션이 붙은 클래스를 자동으로 open으로 변환할 수 있습니다. 다만 한 가지 주의할 점이 있습니다. `kotlin-spring` 플러그인이 기본으로 열어주는 애노테이션은 `@Component`, `@Async`, `@Transactional`, `@Cacheable`, `@SpringBootTest`와 이들을 메타 애노테이션으로 갖는 클래스(`@Service`, `@Repository`, `@Configuration` 등)로 한정되어 있어, **`@Document`는 여기에 포함되지 않습니다.** 따라서 도메인 클래스를 open으로 만들려면 아래처럼 `allOpen` 블록에 `@Document`를 직접 등록해 주어야 합니다.

```kotlin
plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.6.21"
    // kotlin-spring을 쓰더라도 @Document는 자동으로 열리지 않으므로
    // allopen 플러그인으로 @Document를 명시적으로 등록해야 한다.
    id("org.jetbrains.kotlin.plugin.allopen") version "1.6.21"
}

allOpen {
    annotation("org.springframework.data.mongodb.core.mapping.Document")
}
```

이 설정을 적용하면, `@Document` 애노테이션이 붙은 클래스들이 open으로 처리되어 프록시 객체 생성이 가능해집니다. 즉, Lazy 로딩이 원활하게 동작할 수 있습니다.

아래 이미지는 Lazy 로딩이 활성화된 상태에서 Author 필드를 지연 로딩하기 위해 생성된 LazyLoadingProxy 객체를 보여줍니다. 이 프록시 객체는 실제 Author 데이터에 접근할 때 필요한 시점에 데이터를 로딩하도록 구성되어 있습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-6.png)

결국, 도메인 클래스가 open 상태로 변환되지 않으면 프록시 객체 생성이 불가능하여 Lazy 로딩이 실패하게 됩니다. 따라서, Spring Data MongoDB에서 Lazy 로딩 기능을 활용하려면 반드시 위와 같은 설정을 통해 대상 도메인 클래스가 open 상태로 유지되도록 해야 합니다.

## $lookup 기반 연관 객체 조회

위 조회에서 살펴보았듯이, `@DBRef` 기반으로 연관 객체를 포함하여 조회하면 **N+1 문제가 발생할 수밖에 없습니다.** 이를 해결하기 위해 MongoDB의 **`$lookup` 연산자**를 활용할 수 있습니다.

`$lookup`은 MongoDB의 Aggregation Pipeline에서 제공되는 연산자로, 두 컬렉션을 조인(join)하는 역할을 합니다. 이는 RDBMS의 Join과 유사하게 동작하여, 한 컬렉션의 데이터를 기준으로 관련된 다른 컬렉션의 데이터를 한 번의 Aggregation 쿼리로 가져올 수 있습니다. 단, 앞서 언급했듯이 **`$lookup`은 단순 find 쿼리에서는 사용할 수 없으며, 반드시 aggregate 쿼리로 조회해야 합니다.** 이렇게 하면 각 **Post마다 별도의 Author 조회 쿼리가 발생하는 N+1 문제를 효과적으로 해결할 수 있습니다.**

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
                "author": 1.0
            }
        },
        {
            "$limit": 500.0
        }
    ]
)
```

이 예제는 `$lookup` 연산자를 사용하여 Post와 Author 컬렉션을 조인하는 방법을 보여줍니다.

`$lookup`은 MongoDB의 Aggregation Pipeline 단계 중 하나로, 한 컬렉션의 필드를 기준으로 다른 컬렉션의 관련 데이터를 조인하여 한 번의 쿼리로 가져올 수 있습니다. 이를 통해 단일 Aggregation 파이프라인으로 모든 연관 데이터를 한 번에 조회할 수 있어, **각 Post마다 별도의 Author 조회 쿼리가 발생하는 N+1 문제를 효과적으로 회피할 수 있습니다.**

### 연관 객체 조회 Code

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
                "post",
                PostProjectionLookup::class.java,
            )
            .mappedResults
    }
}
```

**getPostsLookUp** 메서드는 단일 Aggregation 쿼리를 통해 Post와 Author 데이터를 한 번에 조회합니다. 이 과정에서 `$lookup` 연산자가 두 컬렉션 간의 조인을 수행하고, `$unwind`를 사용해 조인된 Author 데이터를 평탄화합니다. 결과적으로 모든 연관 데이터를 한 번에 가져오기 때문에 각 Post마다 별도의 Author 조회 쿼리가 실행되는 N+1 문제가 발생하지 않으며, 대량의 데이터를 조회하는 상황에서도 성능 저하를 효과적으로 방지할 수 있습니다.

또한, 앞서 소개한 단순 ID 참조 방식(`author_id`만 저장하는 모델링)의 경우에도 동일하게 `$lookup`을 통해 연관 Author 데이터를 조회할 수 있습니다. 이는 반드시 `@DBRef`로 연관관계를 매핑할 필요 없이, 단순 ID 저장 방식만으로도 연관 데이터를 조인할 수 있음을 보여줍니다.

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

실제 동작이 어떻게 나가는지 본격적으로 살펴보겠습니다.

### $lookup 결과

아래 이미지는 `$lookup` 방식을 사용하여 Post와 Author 데이터를 단일 Aggregation 쿼리로 조회한 결과를 보여줍니다. 쿼리 로그를 통해 두 컬렉션 간의 조인과 함께 Author 데이터가 평탄화되어 처리되는 과정을 확인할 수 있으며, **이 방식은 모든 연관 데이터를 한 번에 가져와 N+1 문제를 효과적으로 회피함을 증명합니다.**

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-4.png)

또한, Post와 Author 데이터를 한 번에 조회하는 `db.post.aggregate` 쿼리가 약 76ms 만에 완료된 것을 확인할 수 있으며, 이를 통해 성능이 매우 빨라졌음을 확인할 수 있습니다.

## 성능 측정

아래 이미지는 각 조건을 10회씩 테스트한 후 산출된 평균 응답 시간을 비교한 벤치마크 결과를 보여줍니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-5.png)

| rows  | $lookup | DBRef lazy false | DBRef lazy true(author 접근) | DBRef lazy true(author 미접근) |
|-------|---------|------------------|----------------------------|-----------------------------|
| 1     | 9.2ms   | 9.6ms            | 9.3ms                      | 8.5ms                       |
| 50    | 11.6ms  | 69.7ms           | 69.4ms                     | 8.9ms                       |
| 100   | 16.2ms  | 130.1ms          | 133.5ms                    | 11.5ms                      |
| 500   | 42.2ms  | 574.2ms          | 575.9ms                    | 23.5ms                      |
| 1,000 | 69.5ms  | 1167.4ms         | 1178.3ms                   | 41.9ms                      |
| 5,000 | 257.2ms | 6043.1ms         | 6181.5ms                   | 129.6ms                     |

테스트 결과를 요약하면, 단일 문서 조회에서는 모든 방식이 거의 동일한 응답 속도를 보입니다. **그러나 조회 대상 문서 수가 증가할수록 각 방식 간의 성능 차이가 뚜렷하게 나타납니다.**

특히, Author 필드에 접근하는 경우, **lazy 설정이 true든 false든 상관없이 각 Post마다 추가 쿼리가 실행되어 N+1 문제가 발생합니다.** 이로 인해, 예를 들어 1,000건의 Post를 조회하면 약 1,000ms 정도의 응답 속도가 소요되어, 실제 서비스에 적용하기에는 다소 부적합할 수 있습니다.

반면, lazy=true 설정에서 Author 필드에 접근하지 않는 경우에는 단순 find 쿼리만 실행되므로 가장 빠른 응답 속도를 기록합니다. 만약 Author 정보에 접근해야 하는 상황이라면, `$lookup` 연산자를 활용해 `db.post.aggregate` 쿼리를 통해 Post와 Author 데이터를 한 번에 조회함으로써 N+1 문제를 효과적으로 회피할 수 있습니다. 이러한 방식은 Author 접근이 필요한 경우에 가장 효율적인 대안으로 평가됩니다.

## $lookup 방식의 리턴 타입 문제와 개선 방안

기존 구현에서는 Lookup 결과의 리턴 타입을 `PostProjectionLookup`과 같이 별도의 Projection 객체로 지정했습니다. 그러나 이 방식은 Post 도큐먼트 내에 정의된 비즈니스 로직(DDD 관점에서 핵심 도메인 기능)을 그대로 활용할 수 없다는 단점이 있습니다. 즉, Post 객체가 갖는 도메인 메서드를 이용하여 비즈니스 규칙을 적용할 수 없게 되어, 객체지향 설계 원칙에 위배되는 문제가 발생합니다.

또한, 상속을 통해 메서드를 재사용하는 방법은 재사용을 위해 인위적으로 상속 관계를 도입하는 방식이므로, 이는 최적의 해결책이라고 보기 어렵습니다. 이러한 문제를 해결하기 위해 가장 좋은 방법은 Aggregation 쿼리의 결과를 바로 `Post` 객체로 리턴하는 것입니다. 이렇게 하면 Post 도큐먼트에 정의된 비즈니스 로직을 그대로 유지하면서도, `$lookup` 방식의 장점을 활용하여 N+1 문제를 회피할 수 있습니다.

```kotlin
class PostCustomRepositoryImpl(mongoTemplate: MongoTemplate) : PostCustomRepository, MongoCustomRepositorySupport<Post>(
    Post::class.java,
    mongoTemplate
) {
    override fun findLookUp(limit: Int): List<Post> {
        // ...
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

이 방식이 동작하는 이유는 Spring Data MongoDB의 매핑 동작에 있습니다. `MappingMongoConverter`는 `@DBRef` 필드를 읽을 때 값이 DBRef(`{"$ref", "$id"}`) 형태이면 별도 조회로 참조를 해석하지만, `$lookup` + `$unwind`를 거쳐 이미 완성된 Author 도큐먼트가 채워져 있으면 추가 조회 없이 그 값을 그대로 매핑합니다. 덕분에 Post 객체에 내장된 도메인 로직을 그대로 활용할 수 있으며, 불필요한 상속 관계를 도입하지 않고도 `$lookup` 방식을 통해 연관 데이터를 한 번에 조회할 수 있습니다.

**중요한 포인트는 `Post` 클래스 내에서 author 필드를 다음과 같이 lazy로 설정하는 점입니다.**

```kotlin
@Document(collection = "post")
class Post(
    // ...
    @DBRef(lazy = true)
    val author: Author,
)
```

이렇게 하면, 일반적인 find 조회에서는 author 필드에 접근하기 전까지 추가 쿼리가 발생하지 않아 N+1 문제를 어느 정도 회피할 수 있고, 필요할 때는 `$lookup`을 통해 author 정보도 함께 조회할 수 있습니다.

단, lazy로 설정되어 있기 때문에 find 쿼리 이후 author 객체에 접근하면 추가적인 N+1 문제가 발생할 수 있으므로, 이 방식은 **N+1 문제를 완전히 해결하는 것은 아니며** 잠재적인 문제가 남아 있음을 유념해야 합니다.

## ID 참조 방식: N+1 문제의 원천 차단

이러한 잠재적 문제를 근본적으로 해결하려면, **객체 간의 연관관계를 맺지 않고 단순한 값인 `author_id`를 보유하는 ID 참조 방식의 모델링을 고려해야 합니다.**

```kotlin
@Document(collection = "post")
class Post(
    // ...
    @Field(name = "author_id")
    val authorId: ObjectId
)
```

이 경우 Post는 Author 객체를 전혀 알지 못하고 단순한 값인 `author_id`만을 저장하므로, 객체 그래프 탐색에 의한 자동 로딩이라는 개념 자체가 존재하지 않습니다. 즉, 어떤 조회 경로에서도 의도치 않은 추가 쿼리가 발생할 수 없으므로 **N+1 문제가 원천적으로 차단됩니다.** lazy 옵션 설정, 프록시 동작, JSON 시리얼라이즈 시점의 의도치 않은 접근 같은 문제를 고민할 필요가 없어집니다.

다만 이 방식에도 트레이드오프는 존재합니다. Author 데이터가 필요한 조회에서는 **개발자가 명시적으로 `$lookup` 기반의 Aggregation 쿼리를 작성해야 합니다.** @DBRef가 제공하던 자동 로딩의 편의는 포기하는 것입니다. 하지만 앞서 살펴본 것처럼 그 편의는 N+1이라는 큰 비용을 수반하며, `$lookup` 쿼리는 어차피 N+1 회피를 위해 작성하게 되는 코드이므로, 실무 관점에서 잃는 것보다 얻는 것이 더 많다고 볼 수 있습니다.

정리하면 두 방식의 트레이드오프는 다음과 같습니다.

| 구분        | @DBRef 객체 참조                   | 단순 ID 참조                       |
|-----------|--------------------------------|--------------------------------|
| 연관 객체 접근  | 객체 그래프 탐색으로 직관적                | ID만 보유, 직접 조회 필요               |
| N+1 문제    | 잠재적으로 항상 존재 (lazy 여부와 무관)      | 원천적으로 발생 불가                    |
| 조회 쿼리 작성  | 자동 로딩 (find 기반)                | `$lookup` Aggregation 명시적 작성 필요 |
| 결합도       | Post가 Author 객체에 직접 의존         | ID 값으로만 느슨하게 연결                |

## 어떤 방식을 선택해야 할까: DDD 관점에서

성능 관점에서는 ID 참조 방식이 유리하다는 것을 확인했습니다. 하지만 객체 간 연관관계를 단순히 성능 문제만으로 판단하기보다는, DDD에서 제시하는 **바운디드 컨텍스트와 애그리거트 경계**를 기준으로 도메인 모델을 구성하는 것이 더 본질적인 접근입니다. 다시 DDD로 돌아가 보겠습니다.

### 애그리거트 경계가 선택의 기준

DDD에서 애그리거트는 함께 생성되고, 함께 변경되며, 하나의 트랜잭션 안에서 일관성(불변식)을 지켜야 하는 객체들의 묶음입니다. 그리고 애그리거트 루트는 이 경계 안의 객체들의 생명주기를 책임집니다. 이 기준으로 보면 참조 방식의 선택은 다음과 같이 정리할 수 있습니다.

- **같은 애그리거트 경계 안에 있다면**: 루트가 하위 객체의 생명주기를 관리하고 불변식을 함께 지켜야 하므로, 객체 참조(@DBRef 방식)도 논리적으로 적절할 수 있습니다. 루트를 통해 하위 객체에 접근하는 것이 자연스럽기 때문입니다.
- **바운디드 컨텍스트가 다르거나 다른 애그리거트라면**: ID 참조가 원칙입니다. Vaughn Vernon이 "다른 애그리거트는 ID로만 참조하라(Reference Other Aggregates by Identity)"라고 정리한 원칙이기도 합니다.

다른 애그리거트를 ID로만 참조해야 하는 이유는 다음과 같습니다.

1. **결합도 최소화**: 객체 참조는 두 애그리거트를 컴파일 타임에 강하게 결합시킵니다. Author 모델의 변경이 Post 모델의 변경으로 전파되며, 이는 컨텍스트 간의 독립적인 발전을 방해합니다.
2. **일관성 경계의 분리**: 애그리거트는 곧 트랜잭션 일관성의 경계입니다. 객체 참조로 다른 애그리거트를 품으면 하나의 트랜잭션에서 여러 애그리거트를 수정하려는 유혹이 생기고, 경계가 흐려집니다. ID 참조는 "이 둘은 서로 다른 일관성 경계다"라는 사실을 코드 수준에서 강제합니다.
3. **확장과 분리에 유리**: ID로만 연결되어 있으면 추후 컬렉션을 분리하거나, 컨텍스트를 별도 서비스(MSA)로 떼어낼 때 도메인 모델을 수정할 필요가 없습니다. 객체 참조 기반 모델은 이 시점에 큰 리팩토링 비용을 치르게 됩니다.
4. **불필요한 로딩 방지**: 객체 참조는 필요 여부와 무관하게 연관 객체 로딩을 유도합니다. ID 참조는 정말 필요한 시점에만 명시적으로 조회하게 만들어, 앞서 살펴본 N+1 같은 성능 문제까지 자연스럽게 예방합니다.

### Post와 Author는 왜 ID 참조가 적절한가

이 기준을 본문의 예제에 적용해 보겠습니다. Post와 Author는 같은 컨텍스트 경계에 있지 않기 때문에 ID 참조가 더 적절합니다. 구체적인 이유는 다음과 같습니다.

- **서로 다른 컨텍스트, 서로 다른 생명주기**: Author는 회원/계정 컨텍스트에 속하는 개념이고, Post는 게시글 컨텍스트에 속합니다. Author는 Post와 무관하게 가입/탈퇴/정보 수정이 일어나고, Post 역시 Author의 상태 변화와 무관하게 작성/수정/삭제됩니다. 생명주기를 함께하지 않는 객체를 하나의 객체 그래프로 묶을 이유가 없습니다.
- **불변식을 공유하지 않음**: Post의 비즈니스 규칙(제목/내용의 유효성, 게시 상태 전이 등)을 지키는 데 Author의 내부 상태는 필요하지 않습니다. Post 입장에서 필요한 것은 "누가 썼는가"라는 식별 정보뿐이며, 이는 `authorId`만으로 충분합니다.
- **변경 전파 차단**: Author 컨텍스트에서 필드가 추가되거나 모델이 재구성되어도, ID 참조라면 Post 도큐먼트와 저장 모델은 아무 영향을 받지 않습니다. @DBRef로 묶여 있다면 Author의 변경이 Post 조회/시리얼라이즈 경로에 그대로 전파됩니다.
- **화면 조합은 조회 계층의 책임**: "게시글 목록에 작성자 이름을 함께 보여주는" 요구사항은 도메인 모델의 연관관계가 아니라 조회(Query) 계층에서 `$lookup`으로 해결하면 됩니다. 도메인 모델이 화면 요구사항 때문에 다른 컨텍스트를 끌어안을 필요는 없습니다.

### 같은 컨텍스트라면 @DBRef를 사용해도 될까?

그렇다면 같은 컨텍스트, 같은 애그리거트 경계 안이라면 @DBRef를 사용하는 것이 좋을까요? 개인적으로 **RDBMS(JPA)라면 객체 참조가 적절할 수 있다고 보지만, MongoDB에서는 같은 경계 안이라도 @DBRef를 권장하지 않습니다.**

JPA의 경우 객체 참조로 인한 N+1 위험이 동일하게 존재하지만, fetch join, `@EntityGraph`, `default_batch_fetch_size` 등 프레임워크 차원의 성숙한 대응 수단이 마련되어 있습니다. 즉 잠재 위험을 통제할 수 있는 도구가 있습니다. 반면 Spring Data MongoDB의 @DBRef에는 이에 대응하는 수단이 사실상 없어, 잠재 위험이 훨씬 높은 편입니다. 구체적인 이유는 다음과 같습니다.

1. **find 기반 조회에서 N+1이 구조적으로 불가피**: MongoDB에서 조인은 aggregate 파이프라인의 `$lookup`으로만 가능합니다. @DBRef의 자동 로딩은 find 쿼리 기반으로 동작하므로, 연관 문서를 조인해 오지 못하고 건별 추가 쿼리를 실행할 수밖에 없습니다. JPA처럼 "조인으로 바꿔서 한 번에 가져오는" 프레임워크 수준의 해결책이 없습니다.
2. **$lookup을 쓰더라도 DBRef 저장 구조가 발목을 잡음**: DBRef는 `{"$ref": "author", "$id": ...}` 형태로 저장되기 때문에, `$lookup` 작성 시 `author.$id` 같은 내부 필드를 다뤄야 해서 쿼리가 번거롭고 직관성이 떨어집니다. 단순 `author_id` 필드였다면 훨씬 깔끔했을 쿼리입니다.
3. **참조 무결성이 보장되지 않음**: MongoDB에는 FK 제약이 없으므로, @DBRef를 쓰더라도 Author 문서가 삭제되면 dangling reference가 그대로 남습니다. 즉, 객체 참조가 주는 "연관관계가 관리되고 있다"는 인상과 달리 실제 정합성 보장은 ID 참조와 다를 바 없습니다.
4. **프록시 기반 Lazy 로딩의 부수 복잡도**: 앞서 살펴본 것처럼 Lazy 로딩은 CGLIB 프록시에 의존하므로, Kotlin final 클래스 문제(all-open 설정), JSON 시리얼라이즈 시점의 의도치 않은 쿼리 등 운영상 함정이 많습니다.
5. **같은 애그리거트라면 MongoDB에서는 내장 도큐먼트가 정석**: 정말 생명주기를 함께하는 데이터라면, MongoDB의 관용적인 모델링은 참조가 아니라 **내장 도큐먼트(Embedded Document)**입니다. 함께 조회되고 함께 변경되는 데이터를 한 도큐먼트 안에 담으면 조인 자체가 필요 없어집니다. 즉, 같은 경계 안이라면 내장이 우선이고, 경계 밖이라면 ID 참조가 우선이므로 @DBRef가 설 자리는 애매합니다.
6. **MongoDB 공식 문서의 권고**: MongoDB 공식 문서 역시 특별한 이유(여러 컬렉션을 동적으로 참조해야 하는 경우 등)가 없다면 DBRef보다 manual reference(ID 참조)를 우선 사용하도록 안내하고 있습니다.

### 이미 @DBRef를 사용하고 있다면

이미 @DBRef 기반으로 운영 중인 시스템이라면, 모델을 당장 ID 참조로 전환하기 어려울 수 있습니다. 이 경우에는 커스텀 리포지토리 프래그먼트에 `MongoRepository`의 기본 메서드와 동일한 시그니처의 메서드를 정의하여, `$lookup` 기반 구현이 기본 구현보다 우선하도록 만드는 것이 더 적절할 수 있습니다. 예를 들어, `findByIdOrNull`(Spring Data Kotlin이 제공하는 `CrudRepository.findByIdOrNull` 확장 함수)로 조회하면 내부적으로 `findById`가 동작하고, 이후 Lazy 로딩된 Author 데이터에 접근할 때 추가 쿼리가 발생할 수 있습니다. 따라서 조회 로직 자체를 `$lookup` 기반으로 제공하여 한 번의 Aggregation 쿼리로 연관 데이터까지 가져오도록 구현하는 것도 좋은 선택입니다.

```kotlin
class PostCustomRepositoryImpl(mongoTemplate: MongoTemplate) : PostCustomRepository, MongoCustomRepositorySupport<Post>(
    Post::class.java,
    mongoTemplate
) {

    override fun findByIdOrNull(id: ObjectId): Post? {
        val match = Aggregation.match(Criteria.where("_id").`is`(id))
        val lookupStage = Aggregation.lookup(...)
        val unwindStage = Aggregation.unwind("author", true)
        val projection = Aggregation.project(...)
        val aggregation = Aggregation.newAggregation(match, lookupStage, unwindStage, projection)
        return mongoTemplate
            .aggregate(
                aggregation,
                "post",
                Post::class.java,
            )
            .uniqueMappedResult
    }
}
```

이와 같이 구현하면, `@DBRef(lazy = true)`를 사용하는 경우에도 기본 메서드를 `$lookup` 기반으로 재정의하여 한 번의 Aggregation 쿼리로 연관 데이터를 조회할 수 있습니다. 자주 사용되는 조회 경로를 이런 식으로 Repository 계층에서 $lookup 기반 메서드로 제공하면, 도메인 모델을 크게 손대지 않고도 N+1 문제를 효과적으로 회피할 수 있습니다.

## 마치며

이번 글에서는 Spring Data MongoDB에서 연관관계를 맺는 두 가지 방식과 그로 인한 N+1 문제, 그리고 DDD 관점의 선택 기준까지 살펴보았습니다. 핵심 요약은 다음과 같습니다.

- **두 가지 연관관계 방식:**  
  MongoDB에서 연관관계를 맺는 방법은 `@DBRef` 기반 객체 참조 방식과 단순 ID 참조 방식이 있습니다. @DBRef는 직관적이지만, MongoDB의 조인은 find가 아닌 aggregate의 `$lookup`으로만 가능하기 때문에 find 기반 자동 로딩은 구조적으로 N+1 문제를 피할 수 없습니다.
- **@DBRef 방식의 한계:**  
  `@DBRef`를 사용하면 연관 문서를 자동으로 로딩할 수 있지만, Post를 조회할 때마다 별도의 Author 조회 쿼리가 발생하여 N+1 문제가 발생할 위험이 있습니다. `lazy = true`를 적용해도 실제 Author 데이터에 접근하는 시점에는 추가 쿼리가 실행되므로 N+1 문제를 완전히 회피하기는 어렵습니다.
- **효과적인 대안:**  
  MongoDB의 `$lookup` 연산자를 활용해 단일 Aggregation 쿼리로 Post와 Author 데이터를 한 번에 조회하면 N+1 문제를 효과적으로 회피할 수 있으며, 벤치마크 결과 대량 조회에서 큰 성능 차이를 확인했습니다.
- **ID 참조 방식:**  
  연관 객체 대신 `author_id`만 보유하면 자동 로딩 자체가 없으므로 N+1 문제가 원천 차단됩니다. 대신 연관 데이터가 필요한 조회에서는 `$lookup` 쿼리를 명시적으로 작성해야 하는 트레이드오프가 있습니다.
- **DDD 관점의 선택 기준:**  
  바운디드 컨텍스트와 애그리거트 경계가 기준입니다. 같은 애그리거트라면 객체 참조도 논리적으로 적절할 수 있지만, 경계가 다르다면 결합도 최소화, 일관성 경계 분리, 독립적 확장을 위해 ID 참조가 원칙입니다. Post와 Author는 같은 컨텍스트 경계에 있지 않으므로 ID 참조가 더 적절합니다. 나아가 MongoDB에서는 같은 경계 안이라도 find 기반 N+1의 구조적 위험, DBRef 저장 구조의 번거로움, 내장 도큐먼트라는 더 나은 대안이 있으므로 @DBRef를 권장하지 않습니다.
- **이미 @DBRef를 사용 중이라면:**  
  `findByIdOrNull` 같은 기본 Repository 메서드를 `$lookup` 기반 Aggregation으로 재정의하여 제공하면, 도메인 모델을 크게 수정하지 않고도 N+1 문제를 회피할 수 있습니다.

이와 같이 성능 측정 결과와 DDD의 설계 기준을 함께 놓고 보면, MongoDB에서의 연관관계는 단순 편의가 아니라 컨텍스트 경계와 조회 특성을 기준으로 선택해야 하며, 그 기준에서 대부분의 경우 ID 참조 방식이 더 견고한 선택임을 확인할 수 있습니다.
