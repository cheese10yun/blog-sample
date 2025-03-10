# Spring Data MongoDB에서 N+1 문제 다루기: 성능 이슈와 최적화 전략

MongoDB에서 문서 간 연관관계를 처리할 때, **N+1 문제**가 발생할 수 있습니다. 이는 주로 **다대일(One-to-Many) 구조**에서, 한 번에 여러 문서를 조회한 뒤 각 문서마다 또 다른 문서를 개별 쿼리로 가져올 때 발생합니다. 예컨대, `Post` 문서 N개를 조회한 후, 각 `Post`마다 연관된 `Author` 문서를 별도 쿼리로 로딩한다면 **총 N+1개의 쿼리**가 실행되어 성능 저하가 발생하게 됩니다.

본 글에서는 아래와 같은 내용을 다룹니다.

1. **DBRef vs. ObjectId**
    - MongoDB에서 연관관계를 표현하는 대표적인 두 방식(DBRef, ObjectId 직접 참조)과 그 차이점
    - DBRef의 Lazy/Eager 로딩이 N+1 문제에 어떤 영향을 미치는지
2. **N+1 문제 발생 원리**
    - DBRef(Eager) 사용 시, 여러 문서를 한 번에 불러오면 각 문서마다 참조 문서를 추가 조회
    - DBRef(Lazy) 사용 시, 참조 필드에 실제 접근하는 시점마다 쿼리가 발생하여 예측이 어려움
3. **실제 성능 측정 결과**
    - DBRef(Eager), DBRef(Lazy), `$lookup` 등을 비교한 벤치마크
    - 대량의 문서를 조회할 때 어떤 방식이 유리한지, 실제 숫자로 확인

이를 통해, Spring Data MongoDB 환경에서 **N+1 문제**를 어떻게 측정하고, 어떤 방식으로 최적화할 수 있는지 구체적인 예시와 함께 살펴보겠습니다. 이후 본문에서는 DBRef와 ObjectId 방식을 비교하고, 실제로 N+1 문제를 유발하는 시나리오와 성능 테스트 결과, 그리고 이를 개선하기 위한 다양한 방법들을 단계별로 소개합니다.

## DBRef vs. ObjectId: 왜, 어떻게 쓰는가?

MongoDB는 RDBMS와 달리 테이블 간의 조인(join) 개념이 제한적으로 제공됩니다. 대신,

- **DBRef**를 통해 문서가 다른 컬렉션을 참조할 수 있으며,
- **ObjectId** 직접 참조 방식으로 단순히 ID만 저장한 뒤, 필요할 때 `$lookup`이나 애플리케이션 로직에서 조합해 가져오는 패턴이 흔히 사용됩니다.

두 방식 모두 장단점이 있으므로, **데이터 액세스 패턴**과 **성능 요구사항**에 따라 적절한 설계가 중요합니다.

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

**추가 설명:**

- **`@DBRef` 애노테이션**은 MongoDB에서 다른 컬렉션의 문서를 참조할 때 사용됩니다. 위 예제에서는 `Post` 클래스에 포함된 `author` 필드가 **DBRef**로 선언되어, Post를 조회할 때 자동으로 관련 Author 문서를 로딩할 수 있습니다.
- `lazy = false`로 설정하면, Post를 조회하는 순간 즉시 Author 문서를 함께 로딩합니다. 이는 코드 작성 시 편리하지만, 한 번에 많은 Post를 불러올 때 **N+1 문제**를 야기할 수 있습니다.

### 문서 구조

MongoDB에 저장된 **Post** 문서는 다음과 같은 형태를 갖습니다.

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

**추가 설명:**

- `"$ref"` 필드에는 참조할 컬렉션의 이름(예: "author")이 명시되며, `"$id"`에는 실제 참조 대상 문서의 ID가 저장됩니다.
- 이 구조는 DBRef가 참조 대상 컬렉션과 연결된 메타 정보를 포함한다는 점에서, 단순한 ObjectId 참조와 차별화됩니다.

### 업데이트 쿼리 예시

DBRef 필드 값을 업데이트하려면, `$ref`와 `$id`를 명시해야 합니다.

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

**추가 설명:**

- 이 쿼리는 특정 Post 문서의 `author` 필드를 업데이트합니다.
- DBRef의 경우, 단순히 ObjectId만 변경하는 것이 아니라 참조 정보(`$ref` 값)까지 업데이트해야 하므로 쿼리가 조금 더 복잡해질 수 있습니다.

### 연관 객체 조회 쿼리 예시

```JavaScript
db.post.find().limit(1)

db.author.find(
    {
        _id: ObjectId("67cd82c3aec68267745dcf85")
    }
)
    .limit(1)
```

**추가 설명:**

- Post를 조회한 후, 별도의 쿼리로 Author 문서를 조회하는 과정을 보여줍니다.
- 만약 Post 여러 건을 조회한다면 각 Post마다 별도의 Author 조회 쿼리가 발생하게 되어, N+1 문제의 원인이 됩니다.

### 연관 객체 조회 방법: Lazy 로딩 vs. Eager 로딩

- **`@DBRef(lazy = true)`**
    - **Lazy 로딩**은 Post를 가져올 때 `author` 필드를 즉시 로딩하지 않습니다.
    - 실제로 `author` 필드에 접근하는 시점에서 CGLIB 프록시가 동작하여, 별도의 쿼리가 실행됩니다.
    - **장점:** 초기 Post 조회 시 불필요한 데이터를 로딩하지 않아 빠른 응답을 기대할 수 있음
    - **단점:** 실제 필드 접근 시마다 추가 쿼리가 발생하여, 예상치 못한 시점에 성능 저하가 발생할 수 있음

- **`@DBRef(lazy = false)`**
    - **Eager 로딩**은 Post를 조회할 때 Author 문서도 함께 즉시 로딩합니다.
    - **장점:** 관련 데이터가 한 번의 작업으로 모두 로딩되어 후속 쿼리 발생이 없음
    - **단점:** Post가 많을 경우, 각 Post마다 Author를 로딩하여 전체 쿼리 수가 많아질 수 있음 (즉, N+1 문제)

#### 코드 예시

아래 코드는 Spring MVC 컨트롤러에서 Post 하나를 조회한 뒤, Projection을 통해 응답을 내려주는 예시입니다.

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

**추가 설명:**

- **Projection 패턴**을 활용하여, 응답 시 실제 Author 정보는 사용하지 않고 Post의 핵심 데이터만 반환하는 경우와, 필요한 경우 Author 정보를 포함하는 방식(추후 PostProjectionLookup 등)으로 나누어 처리할 수 있습니다.
- 이를 통해 실제로 `author` 필드에 접근하는 경우와 접근하지 않는 경우의 쿼리 발생 차이를 쉽게 비교할 수 있습니다.

#### Eager 로딩, @DBRef(lazy = false)

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-3.png)

**이미지 설명:**

- 해당 이미지는 Eager 로딩 방식으로 Post를 조회할 때, Author 문서까지 함께 로딩되는 쿼리 흐름을 보여줍니다.
- 쿼리 로그 상에서 Post와 Author에 대해 별도의 쿼리가 발생하는 것을 확인할 수 있으며, 여러 Post 조회 시 N+1 문제가 명확하게 드러납니다.

#### Lazy 로딩, @DBRef(lazy = true)

##### author lazy로 접근

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-1.png)

**이미지 설명:**

- 이 이미지는 Lazy 로딩 방식에서 Post 조회 후, Author 필드에 접근할 때 발생하는 추가 쿼리를 보여줍니다.
- Post를 먼저 조회한 후, 실제 Author 정보가 필요한 순간에 별도의 쿼리가 실행되는 과정을 확인할 수 있습니다.

##### author lazy로 미접근

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-2.png)

**이미지 설명:**

- 이 이미지는 Post 조회 시 Author 필드에 접근하지 않았을 때의 쿼리 실행 로그를 나타냅니다.
- Lazy 로딩 덕분에 불필요한 Author 조회 쿼리가 발생하지 않아, 훨씬 짧은 응답 시간을 기록하는 것을 확인할 수 있습니다.

##### 프록시(CGLIB)로 인한 all-open 설정 (Kotlin)

```kotlin
plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.6.21"
    // 또는 id("org.jetbrains.kotlin.plugin.allopen") ...
}

allOpen {
    annotation("org.springframework.data.mongodb.core.mapping.Document")
}
```

**추가 설명:**

- Kotlin은 기본적으로 클래스가 `final`로 선언되기 때문에, Lazy 로딩을 위한 **CGLIB 프록시 생성**이 어렵습니다.
- 위와 같이 `all-open` 또는 `kotlin-spring` 플러그인을 적용하면, `@Document` 애노테이션이 붙은 클래스들이 자동으로 `open` 처리되어 프록시 생성을 원활하게 할 수 있습니다.
- 이 설정은 Lazy 로딩을 사용할 때 반드시 고려해야 할 중요한 부분입니다.

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

**추가 설명:**

- 여기서는 단순히 **ObjectId**만 저장하여, Post와 Author 간의 연관관계를 직접 관리합니다.
- DBRef 방식에 비해 구조가 단순하여, 컬렉션 이름 등의 메타 정보를 관리할 필요 없이 빠르게 조회할 수 있는 장점이 있습니다.

### 문서 구조

```json
{
  "_id": "67c49519bb7bbd62011d7b13",
  "author_id": "67c49518bb7bbd62011d7b0e",
  "content": "content-1",
  "title": "title-1"
}
```

**추가 설명:**

- `author_id` 필드에는 단순히 참조할 Author 문서의 ObjectId 값만 저장됩니다.
- 이후 애플리케이션 로직이나 `$lookup`을 활용하여 Author 컬렉션과 조인할 수 있습니다.

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

**추가 설명:**

- 업데이트 쿼리가 단순하여, 복잡한 메타 정보를 처리할 필요 없이 ObjectId 값만 변경하면 됩니다.
- 이로 인해 쿼리의 복잡성이 낮아지고 인덱스 활용도 더 직관적으로 관리할 수 있습니다.

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

**추가 설명:**

- `$lookup`을 사용하여 Post와 Author 컬렉션을 조인하는 예제입니다.
- 이 방식은 단일 Aggregation 파이프라인으로 모든 연관 데이터를 한 번에 가져올 수 있으므로, N+1 문제를 효과적으로 회피할 수 있습니다.

### 연관 객체 조회 방법: lookup

#### 코드 예시

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

**추가 설명:**

- 이 코드는 `$lookup`을 통해 Post와 Author 데이터를 조인하여, 단일 Aggregation 쿼리로 필요한 데이터를 한 번에 조회하는 예시입니다.
- 특히, 대량의 데이터를 조회할 때 DBRef 방식에서 발생하는 N+1 문제를 해결하는 데 유리합니다.

#### lookup 응답

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-4.png)

## 구조적 차이 요약

| 관점            | **DBRef**                                                | **ObjectId 직접 참조**                    |
|---------------|----------------------------------------------------------|---------------------------------------|
| **저장 구조**     | `{"author": {"$ref": "author", "$id": ObjectId("...")}}` | `"author_id": ObjectId("...")`        |
| **메타 정보**     | 참조 컬렉션명(`$ref`), DB명(`$db`) 포함                           | 단순히 ObjectId만 저장                      |
| **쿼리/업데이트**   | DBRef 구조 고려 필요<br>자동 참조 해제 시 여러 쿼리가 발생할 수 있음             | 단순 필드 값이므로 쿼리/인덱스 작성이 직관적             |
| **스키마 변경 영향** | 컬렉션 이름 변경 시 DBRef의 `$ref` 수정 필요                          | 컬렉션명 변경과 무관<br>로직에서만 참조 해결 가능         |
| **성능**        | Lazy/Eager 모두 자동 참조 해제 시 N+1 문제<br>대규모 환경 비효율 가능         | 필요 시 `$lookup` 또는 추가 쿼리로 조인<br>성능상 유연 |

## 성능 테스트 (Performance Test)

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-5.png)

**이미지 설명:**

- 위 이미지는 여러 조회 조건(행 수: 1, 50, 100, 500, 1,000, 5,000)에 대해 각각의 방법(Lookup, DBRef lazy false, DBRef lazy true (author 접근/미접근))의 평균 응답 시간을 시각적으로 비교한 벤치마크 결과를 나타냅니다.
- 특히, DBRef를 사용한 경우 Author 문서의 로딩 방식에 따라 응답 시간이 크게 차이가 나는 것을 확인할 수 있으며, ObjectId 직접 참조 시 `$lookup` 방식이 훨씬 낮은 응답 시간을 보여줍니다.

| rows  | LookUp  | DBRef lazy false | DBRef lazy true(author 접근) | DBRef lazy true(author 미접근) |
|-------|---------|:-----------------|:---------------------------|:----------------------------|
| 1     | 9.2ms   | 9.6ms            | 9.3ms                      | 8.5ms                       |
| 50    | 11.6ms  | 69.7ms           | 69.4ms                     | 8.9ms                       |
| 100   | 16.2ms  | 130.1ms          | 133.5ms                    | 11.5ms                      |
| 500   | 42.2ms  | 574.2ms          | 575.9ms                    | 23.5ms                      |
| 1,000 | 69.5ms  | 1167.4ms         | 1178.3ms                   | 41.9ms                      |
| 5,000 | 257.2ms | 6043.1ms         | 6181.5ms                   | 129.6ms                     |

**추가 설명:**

- 위 표는 각 방식별로 조회한 행 수가 증가할 때 응답 시간이 어떻게 변화하는지를 보여줍니다.
- 특히, DBRef의 eager/lazy 방식에서는 Author 접근 여부에 따라 성능 차이가 극명하게 나타나며, DBRef lazy true 상태에서 Author 필드를 접근하지 않으면 거의 ObjectId 방식과 유사한 성능을 기록합니다.
- 이 데이터를 통해, 대량의 데이터 조회 시에는 **ObjectId 직접 참조** 혹은 `$lookup` 방식을 고려하는 것이 바람직함을 알 수 있습니다.

## 결론

- **DBRef**
    - **장점:** 참조 컬렉션/DB 정보가 명시적으로 포함되어 있어, Spring Data MongoDB에서 객체를 자동으로 로딩할 때 편리합니다.
    - **단점:** Eager/Lazy 방식 모두 대규모 조회 시 N+1 문제를 유발할 가능성이 있으며, 스키마 변경 시 `$ref` 정보를 함께 수정해야 하는 단점이 있습니다.

- **ObjectId 직접 참조**
    - **장점:** 구조가 단순하여 쿼리 작성과 인덱스 설정이 직관적이며, 필요 시 `$lookup`을 통해 한 번의 쿼리로 조인이 가능하여 성능 최적화에 유리합니다.
    - **단점:** 참조 대상 컬렉션 정보를 애플리케이션 로직에서 별도로 관리해야 하며, 객체 변환을 위해 추가적인 코드가 필요할 수 있습니다.

**요약하자면**, 소규모 프로젝트나 간단한 PoC에서는 DBRef가 편리할 수 있지만, 대규모 트래픽이나 복잡한 조회가 필요한 환경에서는 **ObjectId 직접 참조** 방식이 더 유연하고 성능상 이점을 가질 수 있습니다. 또한, `$lookup`을 통한 한 번의 조인 처리로 N+1 문제를 효과적으로 회피할 수 있으므로, 실제 운영 환경에서는 사전에 충분한 벤치마크와 테스트를 통해 요구사항에 부합하는 방식을 선택하는 것이 중요합니다.
