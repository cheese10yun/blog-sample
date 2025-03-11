# Spring Data MongoDB에서 N+1 문제 다루기: 성능 이슈와 최적화 전략

MongoDB에서 문서 간 연관관계를 처리할 때 N+1 문제가 발생할 수 있습니다. 이는 주로 다대일(One-to-Many) 구조에서 여러 문서를 한 번에 조회한 뒤, 각 문서마다 또 다른 문서를 개별 쿼리로 가져올 때 나타납니다. 예를 들어, Post 문서 N개를 조회한 후 각 Post마다 연관된 Author 문서를 별도로 로딩하면 총 N+1개의 쿼리가 실행되어 성능 저하를 유발합니다.

본 글에서는 DBRef와 ObjectId 직접 참조 방식의 차이, 각 방식이 N+1 문제에 미치는 영향, 그리고 실제 성능 테스트 결과를 통해 어느 방식이 대규모 데이터 조회에 유리한지 살펴봅니다. 또한, DBRef와 ObjectId 방식을 실제 상황에서 어떻게 최적화할 수 있는지 구체적인 예시와 함께 소개합니다.

## MongoDB의 연관관계 처리 방식 개요

MongoDB는 전통적인 RDBMS와 달리 조인(join) 개념이 제한적으로 제공되지만, 두 가지 방식으로 문서 간의 연관관계를 표현할 수 있습니다. 첫 번째는 DBRef 방식으로, 문서 내에 참조 대상 컬렉션명과 ID를 함께 저장하여 필요 시 자동으로 참조 문서를 로딩합니다. Spring Data MongoDB에서는 `@DBRef` 애노테이션을 사용하여 이를 구현합니다. 두 번째는 ObjectId 직접 참조 방식으로, 단순히 참조 대상 문서의 ObjectId만 저장하고 필요할 때 `$lookup`과 같은 Aggregation 파이프라인이나 애플리케이션 로직을 통해 별도로 조회하는 방식입니다. 두 방식은 각각의 장단점이 있으므로 데이터 액세스 패턴과 성능 요구사항에 따라 적절하게 선택해야 합니다.

## @DBRef 방식

### 기본 예시 코드

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

위 코드에서는 `@DBRef` 애노테이션을 사용하여 Post 클래스의 author 필드를 DBRef로 선언했습니다. Post를 조회할 때 관련 Author 문서를 자동으로 로딩할 수 있지만, `lazy = false`로 설정하면 Post 조회와 동시에 Author 문서도 함께 로딩되므로, 많은 Post를 조회할 경우 N+1 문제가 발생할 수 있습니다.

### 문서 구조

MongoDB에 저장된 Post 문서는 다음과 같은 형태를 갖습니다.

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

여기서는 `$ref` 필드에 참조할 컬렉션의 이름(예: "author")이 명시되고, `$id` 필드에는 실제 참조 대상 문서의 ID가 저장됩니다. DBRef 방식은 참조 대상 컬렉션과 관련된 메타 정보를 함께 관리하므로 단순 ObjectId 참조와 구분됩니다.

### 업데이트 쿼리 예시

```javascript
db.post.update(
    {_id: ObjectId("post_id")},
    {
        $set: {
            author: {
                $ref: "author",
                $id: ObjectId("new_author_id")
            }
        }
    }
)
```

이 쿼리는 특정 Post 문서의 author 필드를 업데이트합니다. DBRef 방식에서는 단순히 ObjectId만 변경하는 것이 아니라 참조 정보인 `$ref` 값까지 함께 업데이트해야 하므로 쿼리 작성이 다소 복잡해질 수 있습니다.

### 연관 객체 조회 쿼리 예시

```javascript
db.post.find().limit(500)

// limit 번만큼 조회
db.author.find(
    {
        _id: ObjectId("67cd82c3aec68267745dcf85")
    }
)
    .limit(1)
```

Post를 조회한 후 별도의 쿼리로 Author 문서를 조회하는 과정입니다. 만약 여러 개의 Post를 조회할 경우, 각 Post마다 Author 조회 쿼리가 발생하여 N+1 문제가 발생합니다.

### 연관 객체 조회 방법: Lazy 로딩 vs. Eager 로딩

`@DBRef(lazy = true)`를 사용하면 Post를 조회할 때 author 필드를 즉시 로딩하지 않고, 실제로 해당 필드에 접근하는 시점에서 별도의 쿼리가 실행됩니다. 이 방식은 초기 Post 조회 시 불필요한 데이터를 로딩하지 않아 응답 속도가 빠를 수 있으나, 실제로 author 필드에 접근할 때마다 추가 쿼리가 발생하여 예기치 못한 성능 저하를 유발할 수 있습니다.

반면, `@DBRef(lazy = false)`를 사용하면 Post를 조회할 때 Author 문서도 함께 즉시 로딩됩니다. 이 경우 관련 데이터를 한 번에 가져오므로 후속 쿼리가 발생하지 않지만, Post가 많은 상황에서는 각 Post마다 Author를 로딩하여 전체 쿼리 수가 증가하게 됩니다.

아래 코드는 Spring MVC 컨트롤러에서 Post 하나를 조회한 후 Projection을 통해 응답을 내려주는 예시입니다.

```kotlin
@RestController
@RequestMapping("/posts")
class PostController(
    private val postRepository: PostRepository,
) {

    @GetMapping("/post-with-author")
    fun getPostWithAuthor(@RequestParam(name = "limit") limit: Int) = postRepository.find(limit)

    @GetMapping("/post-only")
    fun getPostOnly(@RequestParam(name = "limit") limit: Int) = postRepository.find(limit).map { PostProjection(it) }
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
```

Projection 패턴을 활용하여, 응답 시 실제 Author 정보를 포함하지 않고 Post의 핵심 데이터만 반환하거나 필요한 경우 Author 정보를 포함하는 방식으로 나누어 처리할 수 있습니다. 이를 통해 실제로 author 필드에 접근하는 경우와 접근하지 않는 경우의 쿼리 발생 차이를 쉽게 비교할 수 있습니다.

#### Eager 로딩, @DBRef(lazy = false)

아래 이미지는 Eager 로딩 방식으로 Post를 조회할 때 Author 문서까지 함께 로딩되는 쿼리 흐름을 보여줍니다. 쿼리 로그에서 Post와 Author에 대해 별도의 쿼리가 발생하는 것을 확인할 수 있으며, 여러 Post를 조회할 경우 N+1 문제가 명확하게 드러납니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-3.png)

#### Lazy 로딩, @DBRef(lazy = true)

먼저, author 필드에 접근하는 경우를 보면 Post 조회 후 실제 Author 정보가 필요한 순간에 별도의 쿼리가 실행되는 과정을 아래 이미지에서 확인할 수 있습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-1.png)

반면, Post 조회 시 author 필드에 전혀 접근하지 않으면 불필요한 Author 조회 쿼리가 발생하지 않아 짧은 응답 시간을 기록하는 것을 아래 이미지에서 확인할 수 있습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-2.png)

Kotlin에서는 기본적으로 클래스가 final로 선언되어 Lazy 로딩을 위한 CGLIB 프록시 생성이 어려우므로, 아래와 같이 `all-open` 또는 `kotlin-spring` 플러그인을 적용하여 `@Document` 애노테이션이 붙은 클래스들이 자동으로 open 처리되도록 설정해야 합니다.

```kotlin
plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.6.21"
    // 또는 id("org.jetbrains.kotlin.plugin.allopen") ...
}

allOpen {
    annotation("org.springframework.data.mongodb.core.mapping.Document")
}
```

이 설정을 통해 Lazy 로딩이 원활하게 동작할 수 있습니다.

## ObjectId 참조 방식

### 기본 예시 코드

```kotlin
@Document(collection = "post")
class Post(
    @Field(name = "title")
    val title: String,
    @Field(name = "content")
    val content: String,
    @Field(name = "author_id")
    val authorId: ObjectId
)
```

이 방식은 단순히 ObjectId만 저장하여 Post와 Author 간의 연관관계를 직접 관리합니다. DBRef 방식에 비해 구조가 단순해 컬렉션 이름과 같은 메타 정보를 별도로 관리할 필요 없이 빠르게 조회할 수 있는 장점이 있습니다.

### 문서 구조

ObjectId 직접 참조 방식으로 저장된 Post 문서는 다음과 같이 구성됩니다.

```json
{
  "_id": "67c49519bb7bbd62011d7b13",
  "author_id": "67c49518bb7bbd62011d7b0e",
  "content": "content-1",
  "title": "title-1"
}
```

여기서는 author_id 필드에 단순히 참조할 Author 문서의 ObjectId 값만 저장되며, 이후 애플리케이션 로직이나 `$lookup`을 활용하여 Author 컬렉션과 조인할 수 있습니다.

### 업데이트 쿼리 예시

```javascript
db.post.update(
    {_id: ObjectId("post_id")},
    {
        $set: {
            author_id: ObjectId("new_author_id")
        }
    }
)
```

업데이트 쿼리는 단순하여 복잡한 메타 정보를 처리할 필요 없이 ObjectId 값만 변경하면 됩니다. 이로 인해 쿼리 작성이 간단해지고 인덱스 활용도 더 직관적으로 관리할 수 있습니다.

### 연관 객체 조회 쿼리 예시

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

### 연관 객체 조회 방법: lookup

아래 코드는 `$lookup`을 통해 Post와 Author 데이터를 조인하여 단일 Aggregation 쿼리로 필요한 데이터를 한 번에 조회하는 예시입니다. 특히 대량의 데이터를 조회할 때 DBRef 방식에서 발생하는 N+1 문제를 해결하는 데 유리합니다.

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
    fun find(limit: Int): List<Post>
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

#### lookup 응답

아래 이미지는 `$lookup` 방식으로 조회한 결과를 보여줍니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-4.png)

## 구조적 차이 요약

| 관점            | **DBRef**                                                 | **ObjectId 직접 참조**                        |
|---------------|-----------------------------------------------------------|-------------------------------------------|
| **저장 구조**     | `{"author": {"$ref": "author", "$id": ObjectId("...")}}`  | `"author_id": ObjectId("...")`            |
| **메타 정보**     | 참조 컬렉션명(`$ref`), DB명(`$db`) 포함                            | 단순히 ObjectId만 저장                          |
| **쿼리/업데이트**   | DBRef 구조 고려 필요하며 자동 참조 해제 시 여러 쿼리가 발생할 수 있음               | 단순 필드 값이므로 쿼리 및 인덱스 작성이 직관적임              |
| **스키마 변경 영향** | 컬렉션 이름 변경 시 DBRef의 `$ref` 정보도 함께 수정해야 함                   | 컬렉션명 변경과 무관하며 로직에서만 참조 해결 가능함             |
| **성능**        | Lazy/Eager 모두 자동 참조 해제 시 N+1 문제가 발생할 수 있어 대규모 환경에서는 비효율적임 | 필요 시 `$lookup` 또는 추가 쿼리로 조인하여 성능 최적화가 용이함 |

## 성능 테스트 (Performance Test)

아래 이미지는 여러 조회 조건(행 수: 1, 50, 100, 500, 1,000, 5,000)에 대해 각 방식(Lookup, DBRef lazy false, DBRef lazy true(author 접근/미접근))의 평균 응답 시간을 시각적으로 비교한 벤치마크 결과를 나타냅니다. 이 결과를 보면 DBRef 방식은 Author 문서의 로딩 방식에 따라 응답 시간이 크게 달라지며, DBRef lazy true 상태에서 실제 Author 필드에 접근하지 않을 경우 ObjectId 직접 참조 방식과 유사한 성능을 기록함을 알 수 있습니다.

| rows  | LookUp  | DBRef lazy false | DBRef lazy true(author 접근) | DBRef lazy true(author 미접근) |
|-------|---------|------------------|----------------------------|-----------------------------|
| 1     | 9.2ms   | 9.6ms            | 9.3ms                      | 8.5ms                       |
| 50    | 11.6ms  | 69.7ms           | 69.4ms                     | 8.9ms                       |
| 100   | 16.2ms  | 130.1ms          | 133.5ms                    | 11.5ms                      |
| 500   | 42.2ms  | 574.2ms          | 575.9ms                    | 23.5ms                      |
| 1,000 | 69.5ms  | 1167.4ms         | 1178.3ms                   | 41.9ms                      |
| 5,000 | 257.2ms | 6043.1ms         | 6181.5ms                   | 129.6ms                     |

이 벤치마크 데이터를 통해 대량 데이터를 조회할 때는 ObjectId 직접 참조 방식 또는 `$lookup` 방식을 고려하는 것이 바람직함을 확인할 수 있습니다.

## 결론

DBRef 방식은 참조 컬렉션과 DB 정보를 명시적으로 포함하여 객체를 자동으로 로딩하는 데 편리하지만, Eager와 Lazy 방식 모두 대규모 조회 시 N+1 문제를 유발할 수 있으며, 스키마 변경 시 `$ref` 정보도 함께 수정해야 하는 단점이 있습니다. 반면, ObjectId 직접 참조 방식은 구조가 단순해 쿼리 작성과 인덱스 설정이 직관적이며, 필요할 경우 `$lookup`을 통해 한 번의 쿼리로 연관 데이터를 조인할 수 있어 성능 최적화에 유리합니다. 소규모 프로젝트나 간단한 PoC에서는 DBRef 방식이 편리할 수 있지만, 대규모 트래픽이나 복잡한 조회가 필요한 환경에서는 ObjectId 직접 참조와 `$lookup` 방식을 통한 접근이 더욱 유연하고 효율적일 수 있습니다.

이처럼 각 방식의 특징과 벤치마크 결과를 충분히 검토한 후, 실제 운영 환경에 적합한 최적의 방법을 선택하는 것이 중요합니다.