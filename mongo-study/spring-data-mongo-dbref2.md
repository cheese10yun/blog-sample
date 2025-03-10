다음은 추천한 목차를 바탕으로 작성한 블로그 포스트 초안입니다. 실제 포스트에 적용 시, 코드 예제와 이미지, 벤치마크 결과 등은 직접 테스트 환경에 맞춰 업데이트하시기 바랍니다.

---

# Spring Data MongoDB에서 N+1 문제 다루기: 성능 이슈와 최적화 전략

## 1. 서론

현대 애플리케이션에서는 데이터베이스 연관관계를 효율적으로 처리하는 것이 매우 중요합니다. 특히 MongoDB와 같은 NoSQL 환경에서는 RDBMS처럼 자동 조인이 제공되지 않기 때문에, 데이터 모델링 방식에 따라 **N+1 문제**가 발생할 수 있습니다.  
예를 들어, 여러 개의 `Post` 문서를 조회한 후 각 Post마다 연관된 `Author` 문서를 개별 쿼리로 가져오면 총 N+1개의 쿼리가 실행되어 성능 저하를 유발합니다. 본 포스트에서는 Spring Data MongoDB에서 DBRef와 ObjectId 직접 참조 방식을 사용하여 N+1 문제를 어떻게 발생시키고, 이를 어떻게 해결할 수 있는지 자세히 살펴보겠습니다.

---

## 2. MongoDB의 연관관계 처리 방식 개요

MongoDB는 RDBMS와 달리 **조인**(join) 개념이 제한적으로 제공됩니다. 대신, 두 가지 방식으로 문서 간의 연관관계를 표현할 수 있습니다.

- **DBRef 방식**:
  - 문서 내에 참조 대상 컬렉션명과 ID를 함께 저장하여, 필요 시 자동으로 참조 문서를 로딩합니다.
  - Spring Data MongoDB에서는 `@DBRef` 애노테이션을 사용해 구현할 수 있습니다.

- **ObjectId 직접 참조 방식**:
  - 단순히 참조 대상 문서의 ObjectId만 저장합니다.
  - 필요 시 `$lookup`과 같은 Aggregation 파이프라인을 통해 조인하거나, 애플리케이션 레벨에서 별도로 조회합니다.

두 방식은 각각 장단점이 있으므로, 데이터 액세스 패턴과 성능 요구사항에 따라 적절하게 선택해야 합니다.

---

## 3. DBRef 방식

### 3.1 DBRef 기본 개념 및 구조

DBRef 방식은 문서 내부에 참조할 대상의 컬렉션명(`$ref`)과 해당 문서의 ID(`$id`)를 함께 저장합니다. 예를 들어, 아래와 같이 `Post` 클래스의 `author` 필드를 DBRef로 지정할 수 있습니다.

```kotlin
@Document(collection = "post")
class Post(
    @Field(name = "title")
    val title: String,
    @Field(name = "content")
    val content: String,
    @DBRef(lazy = false) // eager 로딩
    val author: Author
) : Auditable()

@Document(collection = "author")
class Author(
    @Field(name = "name")
    val name: String
) : Auditable()
```

이 경우 MongoDB에 저장되는 문서는 아래와 같은 형태를 띕니다:

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

### 3.2 Lazy 로딩 vs. Eager 로딩

- **Eager 로딩 (`lazy = false`)**:
  - Post를 조회하는 순간, 연관된 Author 문서도 함께 로딩됩니다.
  - 장점: 별도의 추가 쿼리 없이 모든 데이터를 즉시 사용할 수 있음
  - 단점: Post가 여러 건이면, 각각의 Author를 함께 로딩하여 N+1 문제가 발생할 수 있음

- **Lazy 로딩 (`lazy = true`)**:
  - Post 조회 시 Author 문서를 즉시 로딩하지 않고, 해당 필드에 접근하는 순간에만 쿼리가 발생합니다.
  - 장점: 초기 조회 속도는 빠르며, 불필요한 데이터 로딩을 줄일 수 있음
  - 단점: 실제 Author 필드에 접근할 때마다 추가 쿼리가 발생하여, 성능을 예측하기 어려움

#### 코드 예시 – Projection을 활용한 조회

```kotlin
@RestController
@RequestMapping("/posts")
class PostController(
    private val postRepository: PostRepository,
) {

  // Author 정보를 포함하여 조회하는 API
    @GetMapping("/post-with-author")
    fun getPostWithAuthor(@RequestParam(name = "limit") limit: Int) = postRepository.find(limit)

  // Author 정보를 사용하지 않고 Post 정보만 Projection하여 조회하는 API
    @GetMapping("/post-only")
  fun getPostOnly(@RequestParam(name = "limit") limit: Int) =
    postRepository.find(limit).map { PostProjection(it) }
}
```

**주의할 점:**  
Kotlin의 클래스는 기본적으로 `final`이기 때문에, Lazy 로딩을 사용하려면 CGLIB 프록시 생성을 위해 `all-open` 혹은 `kotlin-spring` 플러그인을 적용해야 합니다.

```kotlin
plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.6.21"
    // 또는 id("org.jetbrains.kotlin.plugin.allopen") ...
}

allOpen {
    annotation("org.springframework.data.mongodb.core.mapping.Document")
}
```

### 3.3 이미지로 보는 DBRef 방식

- **Eager 로딩 (lazy = false)**  
  ![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-3.png)  
  _이미지 설명:_ Post 조회 시 Author 문서도 즉시 로딩되어, 각 Post마다 추가 쿼리가 발생하는 구조를 확인할 수 있습니다.

- **Lazy 로딩 (lazy = true)**
  - **post-with-author**  
    ![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-1.png)  
    _이미지 설명:_ Post 조회 후 Author 필드에 접근할 때마다 별도의 쿼리가 실행되는 과정을 보여줍니다.

  - **post-only**  
    ![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-2.png)  
    _이미지 설명:_ Author 필드에 접근하지 않아 추가 쿼리가 발생하지 않는 상황을 확인할 수 있습니다.

---

## 4. ObjectId 직접 참조 방식

### 4.1 ObjectId 참조 기본 개념

ObjectId 직접 참조 방식은 단순히 참조 대상 문서의 ObjectId만 저장합니다. 예를 들어, 아래와 같이 `Post` 클래스에 author의 ObjectId만 저장하는 방식을 사용할 수 있습니다.

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

이 경우 MongoDB에 저장된 문서는 아래와 같이 간단한 구조를 갖습니다:

```json
{
  "_id": "67c49519bb7bbd62011d7b13",
  "author_id": "67c49518bb7bbd62011d7b0e",
  "content": "content-1",
  "title": "title-1"
}
```

### 4.2 $lookup을 활용한 연관 객체 조회

ObjectId 방식은 DBRef에 비해 구조가 단순하지만, 연관된 Author 데이터를 함께 조회하기 위해서는 `$lookup` Aggregation을 사용합니다.

#### 업데이트 및 조회 쿼리 예시

**업데이트 쿼리:**

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

**Aggregation을 활용한 조회:**

```javascript
db.post.aggregate(
    [
        {
            "$lookup": {
                "from": "author",
              "localField": "author_id", // ObjectId 필드를 사용
                "foreignField": "_id",
                "as": "author"
            }
        },
      {"$unwind": {"path": "$author", "preserveNullAndEmptyArrays": true}},
        {
            "$project": {
              "title": 1,
              "content": 1,
              "author": 1,
              "updated_at": 1,
              "created_at": 1
            }
        },
      {"$limit": 500}
    ]
)
```

#### 코드 예시 – Aggregation을 통한 조회

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

class PostCustomRepositoryImpl(mongoTemplate: MongoTemplate) : PostCustomRepository,
  MongoCustomRepositorySupport<Post>(Post::class.java, mongoTemplate) {

    override fun findLookUp(limit: Int): List<PostProjectionLookup> {
        val lookupStage = Aggregation.lookup(
            "author",        // from: 실제 컬렉션 이름
          "author_id",     // localField: 저장된 ObjectId
          "_id",           // foreignField: Author 컬렉션의 _id
          "author"         // as: 결과 필드 이름
        )
        val unwindStage = Aggregation.unwind("author", true)
        val projection = Aggregation.project()
          .andInclude("title", "content", "author", "updated_at", "created_at")
        val limitStage = Aggregation.limit(limit.toLong())
        val aggregation = Aggregation.newAggregation(lookupStage, unwindStage, projection, limitStage)
        return mongoTemplate
          .aggregate(aggregation, Post.DOCUMENT_NAME, PostProjectionLookup::class.java)
            .mappedResults
    }
}
```

**추가 설명:**

- `$lookup`을 통해 Post와 Author 데이터를 한 번의 쿼리로 조인할 수 있어, N+1 문제를 효과적으로 회피할 수 있습니다.
- 구조가 단순하기 때문에 인덱싱과 쿼리 작성이 직관적이며, 필요한 경우 애플리케이션 레벨에서 추가 가공이 용이합니다.

---

## 5. 성능 비교 및 벤치마크 분석

아래 벤치마크 결과는 각 방식(LOOKUP, DBRef eager, DBRef lazy – Author 접근 여부)에 따라 동일한 행(row) 수에 대해 평균 응답 시간을 비교한 결과입니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-5.png)

| rows  | LookUp  | DBRef lazy false | DBRef lazy true (author 접근) | DBRef lazy true (author 미접근) |
|-------|---------|------------------|-----------------------------|------------------------------|
| 1     | 9.2ms   | 9.6ms            | 9.3ms                       | 8.5ms                        |
| 50    | 11.6ms  | 69.7ms           | 69.4ms                      | 8.9ms                        |
| 100   | 16.2ms  | 130.1ms          | 133.5ms                     | 11.5ms                       |
| 500   | 42.2ms  | 574.2ms          | 575.9ms                     | 23.5ms                       |
| 1,000 | 69.5ms  | 1167.4ms         | 1178.3ms                    | 41.9ms                       |
| 5,000 | 257.2ms | 6043.1ms         | 6181.5ms                    | 129.6ms                      |

**분석:**

- **DBRef 방식**은 Author 로딩 방식에 따라 성능이 크게 달라집니다. 특히 eager 로딩 또는 Lazy 방식에서 Author 필드에 접근할 경우, 조회 쿼리가 많이 발생하여 응답 시간이 급격하게 증가합니다.
- 반면, **ObjectId 직접 참조** 방식은 `$lookup`을 사용함으로써 단일 Aggregation으로 조인을 수행, 동일한 데이터 양에 대해 훨씬 낮은 응답 시간을 기록합니다.
- 실제 운영 환경에서는 데이터 양과 조회 빈도에 따라 적절한 방식을 선택해야 하며, 벤치마크 테스트를 통해 최적의 설계를 결정하는 것이 중요합니다.

---

## 6. 최적화 전략 및 권장 사항

N+1 문제를 해결하고 최적의 성능을 달성하기 위한 몇 가지 전략은 다음과 같습니다.

- **DBRef 사용 시**
  - 조회 시 필요한 연관 객체만 Eager 혹은 Lazy로 선택하여 로딩.
  - 불필요한 연관 객체 접근을 피하기 위해 Projection 패턴을 적극 활용.
  - 스키마 변경에 따른 `$ref` 수정 문제를 고려하여 관리 전략 수립.

- **ObjectId 직접 참조 사용 시**
  - `$lookup` Aggregation을 통해 한 번에 연관 데이터를 조회.
  - 인덱싱과 쿼리 최적화를 통해 성능 향상 도모.
  - 애플리케이션 로직에서 연관관계를 명시적으로 관리하여, 쿼리 발생을 제어.

- **Kotlin 사용 시**
  - Lazy 로딩을 위해 CGLIB 프록시가 필요한 경우, `all-open` 플러그인 설정을 반드시 적용하여 클래스가 `open` 상태가 되도록 처리.

- **일반적인 권장 사항**
  - 데이터 양과 조회 패턴에 맞는 테스트를 수행하고, 실제 운영 환경에서 벤치마크 결과를 토대로 설계 결정.
  - 각 방식의 장단점을 명확히 이해한 후, 개발 초기 단계에서 적절한 데이터 모델링을 선택할 것.

---

## 7. 결론 및 향후 전망

본 포스트에서는 Spring Data MongoDB 환경에서 DBRef 방식과 ObjectId 직접 참조 방식을 비교하여 N+1 문제를 어떻게 발생시키고, 이를 어떻게 해결할 수 있는지 살펴보았습니다.

- **DBRef 방식**은 코드 작성 및 자동 객체 매핑에서 편리하지만, 대규모 조회 시 N+1 문제가 발생할 수 있으므로 사용에 주의해야 합니다.
- **ObjectId 직접 참조** 방식은 구조가 단순하며, `$lookup`을 활용한 조인으로 성능 최적화가 가능하지만, 연관관계 관리에 추가 코드가 필요합니다.

**최종 권장:**  
애플리케이션의 규모, 트래픽, 데이터 조회 패턴 등을 고려하여 적절한 방식을 선택하고, 실제 벤치마크를 통해 최적화 전략을 도출하는 것이 중요합니다. 이러한 접근 방식을 통해 MongoDB 기반의 애플리케이션에서도 N+1 문제를 효과적으로 해결할 수 있을 것입니다.

향후에는 MongoDB의 새로운 기능이나 Spring Data MongoDB의 업데이트에 따라, 보다 효율적인 연관관계 처리 방식이 등장할 가능성이 있으므로 지속적인 학습과 테스트가 필요합니다.

---

이상으로, [https://cheese10yun.github.io/](https://cheese10yun.github.io/) 기술 블로그에서 활용할 수 있는 Spring Data MongoDB의 N+1 문제 해결 전략에 대해 소개해 보았습니다. 각 섹션의 예제 코드와 벤치마크 결과를 직접 확인하고, 실제 환경에 맞는 최적화 전략을 도입하시길 바랍니다.