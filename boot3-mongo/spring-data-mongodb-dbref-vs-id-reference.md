# Spring Data MongoDB 가이드: 연관관계 설계와 업데이트 전략

## 시작하며

MongoDB에서는 비정규화를 지향하기 때문에 RDBMS처럼 조인을 빈번하게 사용하지는 않지만, 도큐먼트를 조인해서 조회하고 싶은 상황은 종종 발생합니다. 이때 Spring Data MongoDB 환경에서 연관관계를 맺는 방법은 크게 두 가지입니다. `@DBRef`를 이용해 객체를 직접 참조하는 방식과, 단순히 연관 도큐먼트의 ID 값만 보유하는 방식입니다. 어떤 방식을 선택하느냐에 따라 N+1 문제가 발생할 수 있으며, 이는 서비스 응답 속도에 큰 영향을 미칩니다.

그리고 참조 방식을 정해 도큐먼트 설계를 마쳤다면, 그다음에는 그 도큐먼트를 어떻게 변경할 것인가라는 질문이 이어집니다. 설계와 변경은 동전의 양면이기에, 본 글에서는 이 두 축을 하나의 흐름으로 다룹니다.

1. **연관관계 설계와 조회** — @DBRef를 통한 객체 연관관계 매핑에서 발생하는 N+1 문제와 그 한계를 구체적으로 살펴보고, lazy 옵션 활용, Aggregation의 `$lookup` 연산자 기반 조회, 단순 ID 참조 모델링 등 다양한 최적화 전략을 벤치마크 결과와 함께 소개합니다. 나아가 **DDD의 바운디드 컨텍스트와 애그리거트 경계를 기준으로 어떤 참조 방식을 선택해야 하는지**에 대한 설계 관점의 가이드를 제시합니다.
2. **업데이트 전략** — 그렇게 설계한 도큐먼트를 변경하는 단계에서는 또 다른 문제가 기다립니다. `repository.save`의 구조적 위험을 짚어보고, 특정 필드만 명시적으로 업데이트하는 전략과 `bulkOps` 기반의 대량 업데이트 성능 최적화를 다룹니다. 업데이트 쪽이 급하신 분은 후반부의 [설계에서 변경으로: 애그리거트 경계 안의 업데이트 전략](#설계에서-변경으로-애그리거트-경계-안의-업데이트-전략) 장부터 읽으셔도 좋습니다.

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

아래 코드는 Spring MVC 컨트롤러에서 Post 도큐먼트를 조회한 후, 두 가지 방식의 응답 처리를 비교하는 예시입니다. `getPostWithAuthor` 메서드는 전체 Post 객체를 리턴하여 JSON 시리얼라이즈 과정에서 Author 필드에 접근하게 됩니다. 반면, `getPostOnly` 메서드는 Projection 패턴을 활용하여 핵심 데이터만을 담은 PostProjection 객체를 리턴하므로, Author 필드에 접근하지 않아 추가 쿼리가 발생하지 않습니다. 코드에서 리포지토리가 상속하는 `MongoCustomRepositorySupport`는 뒤의 업데이트 전략 장에서 구현을 상세히 다룰 추상 클래스이므로, 지금은 MongoTemplate 기반 커스텀 리포지토리를 구성하는 지원 클래스 정도로 봐두면 충분합니다.

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

## 조회 성능 측정

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

## 설계에서 변경으로: 애그리거트 경계 안의 업데이트 전략

지금까지의 결론을 정리하면, 애그리거트 경계 밖의 다른 애그리거트는 ID 참조로 느슨하게 연결하고, 정말 생명주기를 함께하는 데이터라면 참조가 아니라 내장 도큐먼트로 한 도큐먼트 안에 담는 것이 MongoDB의 정석적인 모델링입니다. 그런데 이 처방을 따르는 순간 도큐먼트의 모습이 달라집니다. RDB라면 여러 테이블로 나뉘었을 데이터가 하나의 도큐먼트에 담기므로, 애그리거트의 데이터를 온전히 소유한 도큐먼트는 필드가 많아질 수밖에 없습니다.

문제는 바로 이 지점에서 시작됩니다. 앞서 애그리거트는 곧 트랜잭션 일관성의 경계라고 했습니다. 필드가 많아진 도큐먼트를 "조회 → 객체 수정 → 저장" 방식으로 통째로 덮어쓰면, 그 일관성 경계가 코드 수준에서 깨집니다. 서로 다른 필드를 수정한 두 작업이 서로의 변경을 덮어쓰는 Lost Update가 대표적인 사례이며, 잠시 후 타임라인으로 직접 확인하겠습니다.

한 가지 짚어둘 것은 지금부터 말하는 "컨텍스트"의 의미입니다. 경계를 나눠 도큐먼트를 좁혔더라도, 하나의 애그리거트 안에는 여전히 성격이 다른 필드군이 남습니다. 별도 애그리거트로 쪼갤 만큼 생명주기가 다르지는 않지만 변경 엄격성은 서로 다른 필드들 — 예컨대 운영자가 자유롭게 바꾸는 가맹점 이름과 엄격한 승인 절차를 거쳐야 하는 수수료율 같은 관계입니다. 지금부터의 "컨텍스트"는 이런 **같은 애그리거트 내부의 변경 정책 단위**를 가리킵니다.

이를 다루기에는 Post/Author처럼 필드가 단출한 예제가 적합하지 않으므로, 지금부터는 하나의 도큐먼트에 기본 정보·정산·상태 관리 등 여러 컨텍스트가 혼재된 **가맹점(Merchant)** 예제로 전환하여, 이런 도큐먼트를 안전하고 빠르게 업데이트하는 전략을 살펴보겠습니다.

MongoDB의 업데이트 작업은 단순해 보이지만, **무엇을(어떤 필드를)** 업데이트하고 **어떻게(어떤 방식으로)** 업데이트하느냐에 따라 데이터 정합성과 성능이 크게 갈립니다. Spring Data MongoDB는 `repository.save`부터 `mongoTemplate.updateFirst`, `bulkOps`까지 다양한 업데이트 수단을 제공하는데, 각 방식의 특성을 이해하지 못한 채 가장 손에 익은 `save`만 사용하다 보면 의도치 않은 데이터 덮어쓰기나 심각한 성능 저하를 만나게 됩니다.

지금부터는 Spring Data MongoDB의 업데이트 작업을 두 가지 축으로 나누어 정리합니다.

1. **특정 필드만 업데이트하는 전략** — `repository.save`의 구조적 위험을 짚어보고, 변경 대상 필드를 명시적으로 관리하는 방법을 소개합니다. (정확성·안전성)
2. **업데이트 성능 가이드** — `saveAll`, `updateFirst`, `updateMulti`, `bulkOps`의 성능을 측정·비교하고, Repository 확장 패턴과 단계적 성능 최적화 접근법을 소개합니다. (효율성)

## 왜 repository.save가 위험한가

### NoSQL 도큐먼트는 필드가 많아질 수밖에 없다

MongoDB 같은 NoSQL은 스키마가 매우 유연하고, 조인 대신 **역정규화(denormalization)** 로 데이터를 중복 저장하는 모델링이 일반적입니다. RDB라면 여러 테이블로 나뉘었을 데이터가 하나의 도큐먼트에 담기기 때문에, 컬렉션의 필드 수는 구조적으로 늘어날 수밖에 없습니다.

여기서 중요한 것은, 필드가 많다는 것이 단순히 저장할 데이터가 많다는 의미가 아니라는 점입니다. **필드가 많다는 것은 해당 컬렉션이 다루는 컨텍스트와 비즈니스 항목이 많다는 의미입니다.** 예를 들어 가맹점(merchant) 컬렉션을 생각해보겠습니다.

```kotlin
@Document(collection = "merchants")
class Merchant(
    // 가맹점 기본 정보 컨텍스트
    @Field(name = "name")
    val name: String,

    @Field(name = "business_number")
    val businessNumber: String,

    @Field(name = "address")
    val address: Address,

    // 정산 컨텍스트
    @Field(name = "commission_rate")
    val commissionRate: BigDecimal,

    @Field(name = "settlement_cycle")
    val settlementCycle: SettlementCycle,

    @Field(name = "bank_account")
    val bankAccount: BankAccount,

    // 상태 관리 컨텍스트
    @Field(name = "status")
    val status: MerchantStatus,

    @Field(name = "suspended_at")
    val suspendedAt: LocalDateTime?,

    // ... 그 외 다수의 필드
) : Auditable()
```

하나의 도큐먼트 안에 가맹점 기본 정보, 정산·수수료 정보, 상태 관리라는 서로 다른 컨텍스트가 함께 존재합니다. 각 컨텍스트는 담당하는 팀도, 변경이 일어나는 업무 흐름도, 요구되는 변경 엄격성도 다릅니다. 가맹점 이름은 운영자가 자유롭게 수정할 수 있지만, 수수료율은 정산 도메인에서 엄격한 승인 절차를 거쳐야만 변경되어야 하는 필드입니다.

### save는 모든 필드가 모든 곳에서 변경 가능한 구조를 만든다

이렇게 다양한 컨텍스트가 혼재된 도큐먼트를 모두 `repository.save`로 업데이트한다면 어떻게 될까요. `save`는 객체의 전체 상태를 저장하는 메서드이기 때문에, **`save`를 호출하는 모든 코드 구간에서 모든 필드가 업데이트될 수 있는 구조**가 됩니다.

가맹점 이름을 변경하는 로직이든, 정산 수수료를 변경하는 로직이든, 상태를 변경하는 로직이든 결국 같은 `save`를 호출합니다. 코드만 보고 "이 지점에서는 어떤 필드가 변경되는가"를 파악할 수 없고, 반대로 "수수료 필드는 어디에서 변경될 수 있는가"를 추적하려면 `save` 호출부 전체를 열어 객체가 어떻게 조작되었는지 일일이 따라가야 합니다. 필드와 컨텍스트가 늘어날수록 이 추적 비용은 기하급수적으로 커지고, 복잡한 컨텍스트 관리를 더욱 악화시킵니다.

수수료처럼 데이터 변경에 엄격해야 하는 도메인이라면 이는 치명적인 단점입니다. 정산 금액에 직접 영향을 주는 필드가 "이론상 어디서든 변경 가능한" 상태로 열려 있는 것이기 때문입니다.

### 동시성 시나리오: 의도치 않은 덮어쓰기

`save` 기반 업데이트의 문제는 코드 추적의 어려움에서 끝나지 않습니다. 동시성 환경에서는 실제 데이터 유실로 이어집니다. 동일한 가맹점 도큐먼트에 대해 두 사용자가 서로 다른 필드를 수정하는 상황을 가정해보겠습니다.

- 사용자 A: 가맹점 이름을 `A상점 → B상점`으로 변경
- 사용자 B: 수수료율을 `2% → 3%`로 변경

두 작업 모두 "조회 → 객체 수정 → save" 흐름으로 처리된다면 다음과 같은 타임라인이 발생할 수 있습니다.

| 시점 | 사용자 A                              | 사용자 B                          | DB 상태                    |
|----|------------------------------------|--------------------------------|--------------------------|
| t1 | 도큐먼트 조회 (이름: A상점, 수수료: 2%)         |                                | 이름: A상점, 수수료: 2%         |
| t2 |                                    | 도큐먼트 조회 (이름: A상점, 수수료: 2%)     | 이름: A상점, 수수료: 2%         |
| t3 |                                    | 수수료 3%로 변경 후 `save`            | 이름: A상점, **수수료: 3%**     |
| t4 | 이름 B상점으로 변경 후 `save`               |                                | **이름: B상점, 수수료: 2%** ⚠️  |

사용자 A는 이름만 바꿀 의도였지만, `save`는 t1 시점에 조회한 객체의 **전체 상태**를 저장합니다. A가 들고 있던 객체의 수수료는 여전히 2%이므로, t4의 `save`가 실행되는 순간 B가 변경한 수수료 3%는 조회 시점의 낡은 값(2%)으로 덮어써집니다. 전형적인 Lost Update이며, 서로 다른 필드를 수정했음에도 데이터가 유실된다는 점에서 더 위험합니다. 에러도 발생하지 않기 때문에 정산 금액이 틀어진 뒤에야 문제를 인지하게 됩니다.

만약 각 작업이 **변경하려는 필드만** 업데이트했다면 어떨까요. A는 `name` 필드만, B는 `commission_rate` 필드만 `$set`으로 업데이트하면 두 변경은 서로 다른 필드를 건드리므로 실행 순서와 무관하게 둘 다 안전하게 반영됩니다. 이 문제는 락이나 버전 관리 같은 추가 장치 없이, **업데이트 범위를 명시하는 것만으로 구조적으로 사라집니다.**

이것이 본 포스팅에서 `repository.save` 대신 특정 필드만 업데이트하는 방식을 권장하는 이유입니다.

## 특정 필드만 업데이트하는 전략

### 업데이트 메서드 동작 방식 비교

Spring Data MongoDB에서 사용되는 주요 업데이트 메서드들은 아래와 같이 동작 방식과 적합한 시나리오에서 차이가 있습니다.

| **특징**             | **mongoRepository.save** | **mongoTemplate.save** | **mongoTemplate.updateFirst** |
|--------------------|--------------------------|------------------------|-------------------------------|
| **작업 대상**          | 단일 문서                    | 단일 문서                  | 단일 문서                         |
| **저장 방식**          | 전체 문서 교체                 | 전체 문서 교체               | 변경된 필드만 업데이트                  |
| **문서가 없을 경우**      | 새로 삽입                    | 새로 삽입                  | 기본적으로 아무 작업도 수행하지 않음          |
| **업데이트 범위**        | 전체 문서                    | 전체 문서                  | 필드 단위                         |
| **조건 지정**          | `_id` 기준                 | `_id` 기준               | 사용자 정의 쿼리                     |
| **쿼리 정의 방식**       | 도메인 인터페이스(파생 쿼리)         | 수동 쿼리(`Query`)         | 수동 쿼리(`Query`)                |
| **적합한 상황**         | 간단한 CRUD 작업              | 전체 문서 교체 또는 삽입         | 조건에 맞는 단일 문서 필드 수정            |

#### mongoTemplate.save — 문서 전체 교체(Replace)

`_id`를 기준으로 문서를 찾아 **전체 문서를 교체**합니다. 저장 객체에 없는 필드는 기존 문서에서 삭제됩니다.

```kotlin
val user = User(id = "123", name = "John Doe", age = 30)
mongoTemplate.save(user)
```

- 기존 문서: `{ "_id": "123", "name": "Alice", "age": 25, "email": "alice@example.com" }`
- 업데이트 후: `{ "_id": "123", "name": "John Doe", "age": 30 }`
- 변경 사항: **`email` 필드가 삭제됨**

전체 교체라는 특성상 의도치 않은 필드 삭제로 이어지기 쉬워, 일반적인 업데이트 용도로는 거의 사용되지 않습니다.

#### mongoRepository.save — 전체 문서 교체(Replace)

`mongoRepository.save`는 이름과 달리 **부분 업데이트가 아닙니다.** 내부적으로 `MongoTemplate.save`를 그대로 호출하기 때문에, `mongoTemplate.save`와 **완전히 동일하게 전체 문서를 교체**합니다.

```kotlin
// SimpleMongoRepository 내부 구현 (요약)
override fun <S : T> save(entity: S): S {
    return if (entityInformation.isNew(entity)) {
        mongoOperations.insert(entity, ...)   // 신규면 insert
    } else {
        mongoOperations.save(entity, ...)     // 기존이면 결국 MongoTemplate.save() = replaceOne
    }
}
```

JPA의 dirty checking처럼 "변경된 필드만 추려서" 반영하는 메커니즘은 존재하지 않습니다. 저장 객체에 담기지 않은 필드는 기존 문서에서 삭제됩니다.

```kotlin
val user = User(id = "123", name = "John Doe") // age, email은 설정하지 않음
userRepository.save(user)
```

- 기존 문서: `{ "_id": "123", "name": "Alice", "age": 25, "email": "alice@example.com" }`
- 업데이트 후: `{ "_id": "123", "name": "John Doe" }`
- 변경 사항: `name`은 교체되고, **설정하지 않은 `age`·`email`은 삭제됨** (앞의 `mongoTemplate.save`와 동일한 결과)

그렇다면 "조회 → 수정 → save" 흐름에서는 왜 나머지 필드가 유지되는 것처럼 보일까요. 그것은 MongoDB가 필드를 병합해주기 때문이 **아니라**, DB에서 로드한 객체가 이미 모든 필드를 들고 있어 save 시점에 그 값들이 그대로 다시 쓰이기 때문입니다. 즉 "필드가 유지된다"가 아니라 "낡은 값으로 다시 덮어쓴다"에 가깝습니다. 바로 이 성질이 앞 장에서 본 Lost Update의 원인입니다.

Spring Data JPA 경험이 있는 개발자에게 가장 익숙하고 직관적인 방식이지만, 이처럼 "객체 전체 상태 기준"으로 동작하기 때문에 변경 범위를 코드 레벨에서 통제할 수 없다는 구조적 한계가 있습니다.

#### mongoTemplate.updateFirst — 명시적 필드 업데이트

조건에 매칭된 첫 번째 문서의 **지정한 필드만** 업데이트합니다. 문서가 없으면 기본적으로 아무 작업도 수행하지 않습니다.

```kotlin
val query = Query(Criteria.where("name").`is`("Alice"))
val update = Update().set("age", 30)
mongoTemplate.updateFirst(query, update, User::class.java)
```

- 기존 문서: `{ "_id": "123", "name": "Alice", "age": 25, "email": "alice@example.com" }`
- 업데이트 후: `{ "_id": "123", "name": "Alice", "age": 30, "email": "alice@example.com" }`
- 변경 사항: `age` 필드만 업데이트, 나머지 필드는 유지됨

### Update().set() 기반 명시적 필드 업데이트

`updateFirst`의 핵심은 `Update().set("age", 30)`처럼 **변경 대상 필드가 코드에 명시적으로 드러난다**는 점입니다. MongoDB의 `$set` 연산자를 사용해 지정된 필드만 원자적으로 갱신하므로, 이 코드가 `age` 외의 필드를 변경할 가능성은 없습니다.

`save` 방식과 비교하면 관점 자체가 다릅니다. `save`는 "객체의 현재 상태를 저장한다"는 관점이라 변경 범위가 암묵적이지만, `Update().set()`은 "이 필드를 이 값으로 바꾼다"는 관점이라 변경 범위가 명시적입니다. 코드 리뷰 시점에 변경 범위가 그대로 보이고, 앞서 살펴본 동시성 덮어쓰기 문제도 발생하지 않습니다.

### QueryForm(DTO) 패턴: 업데이트 필드를 한곳에서 관리

`Update().set()`으로 변경 필드를 명시하더라도, 업데이트 로직이 여러 서비스에 흩어져 있다면 "어떤 필드가 어디에서 업데이트되는가"라는 질문에는 여전히 코드 전체를 뒤져야 합니다. 그래서 업데이트에 사용할 필드들을 **별도의 DTO 클래스로 정의하고 한곳에서 관리**하는 방식을 함께 사용합니다.

```kotlin
object MerchantQueryForm {
    data class UpdateName(
        val id: ObjectId,
        val name: String
    )

    data class UpdateCommissionRate(
        val id: ObjectId,
        val commissionRate: BigDecimal
    )
}
```

`MerchantQueryForm`은 Merchant 컬렉션에 대한 업데이트 명세서 역할을 합니다. 이 패턴의 효과는 다음과 같습니다.

- **업데이트 카탈로그**: `MerchantQueryForm` 클래스 하나만 보면 이 컬렉션에 어떤 종류의 업데이트가 존재하는지, 각 업데이트가 어떤 필드를 다루는지 한눈에 파악할 수 있습니다.
- **정책의 코드화**: `UpdateName`에는 `name`만 담겨 있으므로, 이름 변경 작업이 수수료율이나 상태 필드를 건드릴 방법이 타입 수준에서 차단됩니다. 반대로 QueryForm에 정의되지 않은 필드는 "현재 업데이트 대상이 아니거나 정책적으로 변경하지 않는 필드"라고 간주할 수 있습니다.
- **추적 용이성**: 특정 필드가 어디에서 변경되는지 알고 싶다면 해당 QueryForm의 사용처만 추적하면 됩니다.

여기에 더해 도큐먼트 필드를 `val`로 선언하면 이 전략이 더 견고해집니다. 앞서 살펴본 `Merchant` 도큐먼트를 다시 보겠습니다.

```kotlin
@Document(collection = "merchants")
class Merchant(
    @Field(name = "name")
    val name: String,

    @Field(name = "business_number")
    val businessNumber: String,

    @Field(name = "commission_rate")
    val commissionRate: BigDecimal,

    @Field(name = "status")
    val status: MerchantStatus,

    // ... 그 외 필드 생략
) : Auditable()
```

필드가 `val`이므로 "조회한 객체의 필드를 수정한 뒤 `save`"하는 경로 자체가 컴파일 수준에서 불가능합니다. 도큐먼트의 불변성이 보장되고, 모든 업데이트는 QueryForm을 거쳐 명시적인 업데이트 쿼리로만 수행되도록 강제됩니다. 참고로 앞서 @DBRef의 Lazy 로딩을 위해 클래스를 open으로 열었던 설정과 혼동할 필요는 없습니다. open은 프록시 생성을 위한 상속 가능성의 문제, `val`은 불변성의 문제로 서로 독립적인 축이며, ID 참조 모델에서는 프록시 기반 Lazy 로딩 자체가 없으므로 all-open 설정을 고민할 필요도 없습니다.

> 엄밀히는 `data class`라면 `copy()`로 일부 필드만 바꾼 새 인스턴스를 만들어 `save`하는 우회 경로가 남습니다. 위 예시처럼 일반 `class`로 선언하면 `copy()`가 없어 이 경로까지 막을 수 있습니다. 즉 `val` 선언은 "기존 인스턴스의 제자리 수정"을 막는 1차 방어선이고, 일반 `class` 사용은 그 방어선을 한층 더 굳히는 선택입니다.

### Repository 구성 예시

이제 QueryForm을 받아 실제 업데이트를 수행하는 Repository를 구성해보겠습니다. Spring Data JPA에서 Custom Repository를 확장하는 것과 동일한 패턴입니다.

```kotlin
interface MerchantRepository : MongoRepository<Merchant, ObjectId>, MerchantCustomRepository

interface MerchantCustomRepository {
    fun updateName(targets: List<MerchantQueryForm.UpdateName>)
}

class MerchantCustomRepositoryImpl(mongoTemplate: MongoTemplate) :
    MerchantCustomRepository,
    MongoCustomRepositorySupport<Merchant>(Merchant::class.java, mongoTemplate) {

    override fun updateName(targets: List<MerchantQueryForm.UpdateName>) {
        bulkUpdate(
            targets.map {
                Pair(
                    { Query(Criteria.where("_id").`is`(it.id)) },
                    { Update().set("name", it.name) }
                )
            }
        )
    }
}
```

`updateName`은 `MerchantQueryForm.UpdateName` 리스트를 받아 각 대상의 `name` 필드만 업데이트합니다. 시그니처만 봐도 "이 메서드는 name 필드를 업데이트한다"는 사실이 명확하게 드러납니다. 내부에서 사용하는 `bulkUpdate`는 `MongoCustomRepositorySupport`가 제공하는 벌크 업데이트 편의 메서드로, 자세한 구현은 다음 장에서 다룹니다.

테스트 코드로 동작을 검증해보겠습니다.

```kotlin
@MongoTestSupport
class MerchantRepositoryTest(
    private val merchantRepository: MerchantRepository
) : MongoStudyApplicationTests() {

    @Test
    fun `updateName test`() {
        // given
        val merchants = (1..20).map {
            Merchant(
                name = "name",
                ...
            )
        }

        val targets = mongoTemplate
            .insertAll(merchants).map {
                MerchantQueryForm.UpdateName(
                    id = it.id!!,
                    name = "newName"
                )
            }

        // when
        merchantRepository.updateName(targets)

        // then
        val results = mongoTemplate.findAll<Merchant>()

        then(results).hasSize(20)
        then(results).allSatisfy {
            then(it.name).isEqualTo("newName")
        }
    }
}
```

`Merchant` 객체를 저장한 뒤 `UpdateName` QueryForm을 만들어 `updateName`을 호출하고, 다시 조회하여 `name` 필드만 의도대로 변경되었는지 확인합니다. 업데이트 대상 필드가 QueryForm으로 명확히 정의되어 있기 때문에, 테스트 역시 검증해야 할 범위가 명확해집니다.

## 업데이트 성능 가이드

지금까지는 "무엇을 업데이트할 것인가"를 다뤘다면, 이제는 "어떻게 빠르게 업데이트할 것인가"를 다룰 차례입니다. 특히 대량의 문서를 업데이트하는 배치성 작업에서는 어떤 메서드를 선택하느냐에 따라 성능이 수십 배 차이 납니다.

### 업데이트 방식별 동작과 특성

#### saveAll

```kotlin
fun updateSaveAll(merchants: List<Merchant>) {
    // name 필드만 UUID.randomUUID().toString() 으로 업데이트
    merchantRepository.saveAll(merchants)
}
```

`saveAll`은 `CrudRepository`가 기본 제공하는 메서드로, 리스트를 순회하며 각 객체의 `id` 존재 여부에 따라 삽입 또는 업데이트를 수행합니다. 별도의 학습 없이 바로 사용할 수 있다는 것이 큰 장점이지만, 내부 동작을 들여다보면 리스트를 순회하면서 **문서 한 건마다 독립적인 DB I/O가 발생**합니다. N건을 업데이트한다면 네트워크 왕복도 N회 일어난다는 뜻입니다. 또한 변경되지 않은 필드까지 문서 전체를 전송하기 때문에 payload 측면에서도 비효율적입니다. "기본 제공이라 무심코 사용했다가" 대량 처리에서 성능 이슈를 만나는 대표적인 사례입니다.

#### updateFirst

```kotlin
fun updateFirst(id: ObjectId): UpdateResult {
    return mongoTemplate.updateFirst(
        Query(Criteria.where("_id").`is`(id)),
        Update().set("name", UUID.randomUUID().toString()),
        Merchant::class.java
    )
}
```

`updateFirst`는 변경할 필드만 명시적으로 지정할 수 있어 payload 면에서는 `saveAll`보다 효율적입니다. 하지만 **DB I/O 메커니즘은 `saveAll`과 동일**합니다. 호출 한 번이 곧 네트워크 왕복 한 번이므로, N건을 업데이트하려면 외부에서 루프를 돌며 N번 호출해야 합니다. 결과적으로 두 방식은 성능 특성상 같은 한계를 공유합니다. 또한 `_id` 외 다른 필드로 조회할 경우 인덱스 상태에 따라 매칭 비용이 추가되어 성능 차이가 더 벌어질 수 있습니다.

#### updateMulti

```kotlin
fun updateStatusBulk(targetIds: List<ObjectId>, status: MerchantStatus): Long {
    return mongoTemplate.updateMulti(
        Query(Criteria.where("_id").`in`(targetIds).and("status").ne(status)),
        Update().set("status", status),
        Merchant::class.java
    ).modifiedCount
}
```

`updateMulti`는 조건에 매칭되는 **여러 문서를 단일 쿼리 한 번으로** 동일한 값으로 업데이트합니다. 가장 빛을 발하는 사례는 **가맹점 상태(영업/정지)** 처럼 업데이트하려는 값의 종류가 한정적인 경우입니다. 100건의 가맹점 상태를 변경해야 한다면, 대상 ID 목록을 영업과 정지 두 그룹으로 나누어 `updateMulti`를 최대 2회 호출하면 됩니다. `saveAll`이나 `updateFirst`로 처리했다면 100회 발생했을 DB I/O가 최대 2회로 줄어듭니다.

단, `updateMulti`는 **모든 대상 문서에 동일한 값을 써야 한다는 제약**이 있습니다. 가맹점별 수수료율 변경처럼 각 문서마다 set해야 하는 값이 다를 경우에는 이 메서드 하나로 처리할 수 없습니다. 이런 케이스가 바로 다음에 소개하는 `bulkOps`가 필요한 이유입니다.

#### bulkOps

```kotlin
fun updateBulk(
    ids: List<ObjectId>,
    bulkMode: BulkOperations.BulkMode = BulkOperations.BulkMode.UNORDERED // or BulkOperations.BulkMode.ORDERED
): BulkWriteResult {
    val bulkOps = mongoTemplate.bulkOps(bulkMode, Merchant::class.java)
    for (id in ids) {
        bulkOps.updateOne(
            Query(Criteria.where("_id").`is`(id)),
            Update().set("name", UUID.randomUUID().toString())
        )
    }
    return bulkOps.execute()
}
```

`bulkOps`의 핵심도 `updateMulti`와 마찬가지로 **DB I/O 횟수를 줄이는 것**이지만, 접근 방식이 다릅니다. 각 문서마다 다른 값을 set해야 하는 경우 `updateMulti` 한 번으로는 묶을 수 없어 결국 N회 I/O가 발생합니다. `bulkOps`는 update 명령들을 **클라이언트 측 버퍼에 누적**하다가 `execute()` 시점에 묶어 전송합니다. 즉, 각기 다른 값을 N건 업데이트하더라도 실제 DB로 나가는 네트워크 왕복은 건별 N회가 아니라 **소수의 배치**로 크게 줄어듭니다.

> MongoDB 드라이버는 하나의 bulk write를 `maxWriteBatchSize`(현대 버전 기준 100,000 ops)와 메시지 크기 한도(약 48MB)에 맞춰 **자동으로 배치 분할**합니다. 따라서 왕복 횟수는 정확히는 `ceil(N / 배치 크기)`이며, 작은 문서를 다루는 대부분의 경우 한 번 또는 소수의 왕복으로 수렴합니다. "N회 → 1회"는 이 한도 안에 들어오는 경우의 근사 표현으로 이해하면 됩니다.

정리하면 두 메서드의 선택 기준은 다음과 같습니다.

- **동일한 값을 다수 문서에 적용** → `updateMulti`: 조건 쿼리만으로 한 번에 처리
- **문서마다 다른 값을 적용** → `bulkOps`: 각 update 명령을 버퍼에 쌓고 `execute()` 시점에 일괄 전송

`bulkOps`는 실행 모드를 선택할 수 있습니다.

- **`BulkMode.UNORDERED`**: 작업들이 순서에 구애받지 않고 처리됩니다. 하나의 작업 실패가 다른 작업에 영향을 미치지 않으며, 대량의 독립적인 작업을 빠르게 처리해야 할 때 유용합니다.
- **`BulkMode.ORDERED`**: 작업들이 추가된 순서대로 처리됩니다. 하나의 작업이 실패하면 그 이후의 작업은 실행되지 않을 수 있으며, 작업 간 순서가 중요한 경우에 적합합니다.

### 업데이트 성능 측정 결과

성능 테스트는 `saveAll`, `updateFirst`, `bulkOps(UNORDERED)`, `bulkOps(ORDERED)`를 대상으로, 문서마다 서로 다른 값을 set하는 워크로드에서 진행했습니다.

`updateMulti`는 이번 벤치마크에서 의도적으로 제외했습니다. 첫째, `updateMulti`는 단일 쿼리 한 번으로 다수 문서를 처리하므로 N 값에 관계없이 사실상 상수에 가까운 처리 시간을 보여, "행 수에 따른 시간 변화"를 측정하는 다른 방식들과 같은 축에 놓으면 비교 자체가 의미를 잃습니다. 둘째, `updateMulti`는 모든 대상에 동일한 값을 적용하는 시나리오에만 사용 가능하므로, 각 문서마다 다른 값을 set하는 이번 워크로드와 동일한 조건으로 테스트를 재현할 수 없습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/MongoDB%20Update%20Performance.svg)


| rows   | saveAll    | updateFirst | bulkOps(UNORDERED) | bulkOps(ORDERED) |
|--------|------------|-------------|--------------------|------------------|
| 100    | 1,052 ms   | 1,176 ms    | 46 ms              | 79 ms            |
| 200    | 2,304 ms   | 2,196 ms    | 103 ms             | 124 ms           |
| 500    | 5,658 ms   | 5,250 ms    | 309 ms             | 257 ms           |
| 1,000  | 11,106 ms  | 10,846 ms   | 418 ms             | 412 ms           |
| 2,000  | 22,592 ms  | 21,427 ms   | 1,060 ms           | 1,004 ms         |
| 5,000  | 54,407 ms  | 52,075 ms   | 2,663 ms           | 2,292 ms         |
| 10,000 | 107,651 ms | 110,884 ms  | 4,514 ms           | 4,496 ms         |

분석 결과는 다음과 같습니다.

**saveAll과 updateFirst**는 유사한 성능을 보이며, 행 수가 증가함에 따라 수행 시간이 선형적으로 증가합니다. 두 방식의 성능 차이는 유의미하지 않으므로, 데이터 양이 적은 경우에는 upsert 기능을 제공하는 `saveAll`로 로직을 단순화하는 것도 선택지가 될 수 있습니다. 단, 이번 테스트는 기본 키(PK) 기반으로 업데이트를 수행했기 때문에, 다른 키로 조회할 경우 인덱스 상태에 따라 성능 차이가 발생할 수 있습니다.

**bulkOps(UNORDERED)와 bulkOps(ORDERED)**는 `saveAll`·`updateFirst` 대비 현저히 빠른 성능을 보입니다. 10,000건 기준 약 107초 vs 4.5초, **약 24배 차이**입니다. 두 모드 간에는 10,000건까지는 큰 성능 차이가 나타나지 않았지만, 데이터가 여러 노드에 분산 저장된 환경에서는 순서 보장 비용으로 인해 두 방식의 차이가 더 명확하게 드러날 수 있습니다.

여기서 얻을 수 있는 가장 중요한 인사이트는, **복잡한 병렬 처리나 멀티스레드 같은 기법 없이 네트워크 I/O를 모아서 보내는 것만으로도** 성능 향상의 폭이 매우 크다는 점입니다. `bulkOps`가 24배 빠른 이유는 알고리즘이 우월해서가 아니라, 단순히 네트워크 왕복 횟수를 건별 N회에서 소수의 배치로 줄였기 때문입니다.

### MongoCustomRepositorySupport: JPA처럼 Repository 확장하기

`bulkOps`를 직접 사용할 때마다 반복되는 패턴을 생각해보면, 매번 `mongoTemplate.bulkOps(bulkMode, documentClass)`로 초기화하고, 루프를 돌며 `updateOne`으로 작업을 추가한 뒤, 마지막에 `execute()`를 호출하는 세 단계가 항상 고정적입니다. JPA에서 `QuerydslRepositorySupport` 같은 추상 클래스로 Repository를 확장하듯, MongoDB에서도 이 보일러플레이트를 추상 클래스에 한 번만 구현해두면 각 Repository 구현체는 핵심 비즈니스 로직인 `Query`와 `Update` 조합만 정의하면 됩니다. 바로 이 클래스가 앞서 연관관계 장의 `$lookup` 리포지토리(`PostCustomRepositoryImpl`)에서 상속했던 `MongoCustomRepositorySupport`입니다.

```kotlin
abstract class MongoCustomRepositorySupport<T>(
    protected val documentClass: Class<T>,
    protected val mongoTemplate: MongoTemplate
) {

    protected fun bulkUpdate(
        operations: List<Pair<() -> Query, () -> Update>>,
        bulkMode: BulkOperations.BulkMode = BulkOperations.BulkMode.UNORDERED
    ): BulkWriteResult {
        val bulkOps = mongoTemplate.bulkOps(bulkMode, documentClass)
        operations.forEach { (queryCreator, updateCreator) ->
            bulkOps.updateOne(queryCreator.invoke(), updateCreator.invoke())
        }
        return bulkOps.execute()
    }

    protected fun updateMany(criteria: Criteria, update: Update): Long {
        val query = Query(criteria)
        return mongoTemplate.updateMulti(query, update, documentClass).modifiedCount
    }
}
```

`bulkUpdate`는 `Pair<() -> Query, () -> Update>` 형태의 람다 리스트를 받습니다. 다만 이 람다 래핑이 실행 시점을 늦추는 등의 성능적 이점을 주지는 않습니다. `bulkUpdate` 내부의 `forEach`에서 곧바로 `invoke()`되므로, 사실상 `Pair<Query, Update>`를 직접 넘기는 것과 동작이 동일합니다. 여기서 람다를 사용한 이유는 호출부에서 `{ Query(...) } to { Update(...) }`처럼 조건과 변경 내용을 선언적으로 나열해 가독성을 높이려는 스타일 선택에 가깝습니다. 단순함을 우선한다면 `Pair<Query, Update>` 형태로 두어도 무방합니다.

`updateMany`는 `updateMulti`를 감싸는 래퍼로, 동일한 값을 다수 문서에 일괄 적용하는 시나리오에서 사용합니다. `bulkUpdate`와 함께 추상 클래스에 포함시켜두면, 어느 Repository든 상속만으로 두 메서드를 모두 활용할 수 있습니다.

구현체에서의 활용 예시입니다.

```kotlin
class MerchantCustomRepositoryImpl(mongoTemplate: MongoTemplate) :
    MerchantCustomRepository,
    MongoCustomRepositorySupport<Merchant>(Merchant::class.java, mongoTemplate) {

    // 가맹점별로 서로 다른 수수료율을 업데이트 — 문서마다 값이 다르므로 bulkUpdate 사용
    override fun updateCommissionRates(targets: List<MerchantQueryForm.UpdateCommissionRate>): BulkWriteResult {
        val operations = targets.map {
            Pair(
                first = { Query(Criteria.where("_id").`is`(it.id)) },
                second = { Update().set("commission_rate", it.commissionRate) }
            )
        }
        return bulkUpdate(operations)
    }

    // 특정 가맹점들을 일괄 정지 — 모두 동일한 값이므로 updateMany 사용
    override fun suspendMerchants(targetIds: List<ObjectId>): Long {
        return updateMany(
            Criteria.where("_id").`in`(targetIds),
            Update().set("status", MerchantStatus.SUSPENDED)
        )
    }
}
```

두 메서드를 나란히 놓고 보면 앞서 설명한 선택 기준이 코드 수준에서도 명확하게 드러납니다. `updateCommissionRates`는 가맹점마다 적용할 수수료율이 다르기 때문에 `bulkUpdate`로 각 update 명령을 버퍼에 쌓아 한 번에 전송하고, `suspendMerchants`는 모든 대상에 동일한 `SUSPENDED` 상태를 적용하므로 `updateMany`로 단일 쿼리 한 번에 처리합니다. 두 경우 모두 `MongoCustomRepositorySupport`가 내부 구현을 캡슐화하고 있어, Repository 구현체에는 비즈니스 의도를 명확하게 표현하는 코드만 남습니다.

이 패턴은 앞 장에서 소개한 QueryForm 전략과 자연스럽게 결합됩니다. QueryForm으로 "무엇을 업데이트하는지"를 명세하고, `MongoCustomRepositorySupport`로 "어떻게 효율적으로 업데이트하는지"를 캡슐화하면, 정확성과 성능을 모두 갖춘 업데이트 계층이 완성됩니다.

### 성능 최적화의 순서: 단순한 것부터

성능 개선이 필요할 때 병렬 처리, 멀티스레드, 비동기 같은 기법부터 떠올리기 쉽습니다. 하지만 위 측정 결과가 보여주듯, **특별한 기법 없이 단순하게 I/O만 몰아서 처리해도 성능적인 이점이 매우 큽니다.** 성능 최적화에는 단계가 있으며, 직관적이고 유지보수가 쉬운 방법부터 순서대로 적용해보고 그래도 부족하다면 다음 단계로 넘어가는 접근이 디버깅·운영 비용을 포함한 총비용을 가장 낮춥니다.

1. **데이터 양이 적고 단순한 경우** → `saveAll` / `updateFirst`로 충분, 코드 단순성을 우선
2. **같은 값을 다수 문서에 일괄 적용하는 경우** → `updateMulti`로 I/O를 "유니크한 값 수"로 수렴
3. **문서마다 다른 값을 적용해야 하는 경우** → `bulkOps`로 I/O를 1회로 수렴
4. **위 방법으로도 성능이 부족한 경우** → 그제야 청크 분할, 비동기/병렬 처리, 샤딩 키 설계 등 다음 단계로 진입

복잡한 기법일수록 코드 가독성이 떨어지고 운영 중 문제 발생 시 추적이 어렵습니다. 처음부터 직관적이지 않은 방법으로 시작하면 성능은 얻더라도 디버깅과 운영에서 더 큰 곤경에 처할 수 있습니다. 동기적이고 직관적인 방향으로 성능 개선의 여지가 있는지 먼저 확인하고, 그 이후에도 더 끌어올려야 한다면 적절한 다음 방법을 찾는 것이 바람직합니다.

## 마치며

이번 글에서는 Spring Data MongoDB의 연관관계 설계부터 업데이트 전략까지를 하나의 흐름으로 살펴보았습니다. 핵심 요약은 다음과 같습니다.

- **두 가지 연관관계 방식:**  
  MongoDB에서 연관관계를 맺는 방법은 `@DBRef` 기반 객체 참조 방식과 단순 ID 참조 방식이 있습니다. @DBRef는 직관적이지만, MongoDB의 조인은 find가 아닌 aggregate의 `$lookup`으로만 가능하기 때문에 find 기반 자동 로딩은 구조적으로 N+1 문제를 피할 수 없습니다. `lazy = true`를 적용해도 실제 데이터에 접근하는 시점에는 추가 쿼리가 실행됩니다.
- **조회 최적화:**  
  MongoDB의 `$lookup` 연산자를 활용해 단일 Aggregation 쿼리로 연관 데이터를 한 번에 조회하면 N+1 문제를 효과적으로 회피할 수 있으며, 벤치마크 결과 대량 조회에서 큰 성능 차이를 확인했습니다. 이미 @DBRef를 사용 중이라면 `findByIdOrNull` 같은 기본 Repository 메서드를 `$lookup` 기반 Aggregation으로 재정의하여 도메인 모델을 크게 수정하지 않고도 N+1 문제를 회피할 수 있습니다.
- **참조 방식의 선택 기준:**  
  바운디드 컨텍스트와 애그리거트 경계가 기준입니다. 경계가 다르다면 결합도 최소화, 일관성 경계 분리, 독립적 확장을 위해 ID 참조가 원칙이고, 정말 생명주기를 함께하는 같은 경계 안의 데이터라면 내장 도큐먼트가 MongoDB의 정석입니다. 이 기준에서 대부분의 경우 @DBRef보다 ID 참조가 더 견고한 선택입니다.
- **업데이트 정확성:**  
  애그리거트의 데이터를 온전히 소유한 도큐먼트에는 변경 엄격성이 서로 다른 여러 컨텍스트가 혼재하기 마련입니다. 이를 `repository.save`로 업데이트하면 변경 범위를 통제할 수 없고, 동시성 환경에서는 의도치 않은 덮어쓰기(Lost Update)로 이어집니다. `Update().set()` 기반의 명시적 필드 업데이트와 QueryForm(DTO) 패턴으로 "어떤 필드가 어디에서 업데이트되는지"를 코드 레벨에서 명확하게 관리할 수 있습니다.
- **업데이트 성능:**  
  대량 업데이트에서는 네트워크 I/O 횟수가 성능을 지배합니다. 동일한 값 적용은 `updateMulti`, 문서마다 다른 값 적용은 `bulkOps`로 I/O를 수렴시키고, 이러한 반복 패턴은 `MongoCustomRepositorySupport` 같은 추상 클래스로 캡슐화하여 조회와 업데이트 양쪽에서 재사용할 수 있습니다.

제가 담당하는 도메인은 특정 필드마다 업데이트 권한이 다르게 설정되어 있어 업데이트 필드를 명확하고 엄격하게 관리해야 하고, 동시에 대량의 데이터를 빠르게 처리해야 하는 요구사항도 있어 이러한 전략을 선택했습니다. 모든 프로젝트에 정답인 방식은 아니며, 컨텍스트 경계와 조회·변경 특성을 기준으로 각자의 도메인에 맞는 적절한 방법을 선택하는 것이 가장 중요합니다.
