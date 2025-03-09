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

- `@DBRef(lazy = false)`는 Post를 조회할 때 Author를 즉시(eager) 로딩합니다.
- `@Document(collection = "post")` / `@Document(collection = "author")`로 컬렉션을 지정했습니다.

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

- `"$ref"` 필드에 참조할 컬렉션 이름(`author`),
- `"$id"` 필드에 참조 대상 문서의 `_id`를 저장합니다.

### 업데이트 쿼리 예시

DBRef 필드 값을 업데이트하려면, `$ref`와 `$id`를 지정해 줍니다.

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

- `"$ref"`: 참조할 컬렉션 이름(예: `"author"`)
- `"$id"`: 새 Author의 ObjectId

### Lazy 로딩 vs. Eager 로딩

- **`@DBRef(lazy = true)`**
    - Post 문서를 가져와도 `author` 필드는 즉시 조회되지 않습니다.
    - 실제로 `author` 필드에 **접근**하는 순간, CGLIB 프록시가 동작해 별도의 쿼리를 실행합니다.
    - 초기 응답은 빠를 수 있으나, **접근 시점마다 추가 쿼리**가 발생하여 예측이 어렵습니다.

- **`@DBRef(lazy = false)`**
    - Post 문서를 조회할 때, Author 문서도 **즉시 로딩**(eager loading)합니다.
    - 여러 Post를 한 번에 가져오면, 각각의 Author를 자동으로 해제하므로 **N+1 문제**가 발생할 가능성이 높습니다.

#### 코드 예시

아래 코드는 Spring MVC 컨트롤러에서 Post 하나를 조회한 뒤, Projection을 통해 응답을 내려주는 예시입니다.

```kotlin
@RestController
@RequestMapping("/posts")
class PostController(
    private val postRepository: PostRepository,
) {
    @GetMapping("/lookup")
    fun getPostsLookUp(
        @RequestParam(name = "limit") limit: Int,
    ) = postRepository.findLookUp(limit)

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

data class AuthorProjection(
    val id: ObjectId,
    val name: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    constructor(author: Author) : this(
        id = author.id!!,
        name = author.name,
        createdAt = author.createdAt,
        updatedAt = author.updatedAt,
    )
}

data class PostProjectionLookup(
    val id: ObjectId,
    val title: String,
    val content: String,
    val author: AuthorProjection,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
```

위 코드에서 `Post` 클래스의 `@DBRef(lazy = true) val author: Author` 부분을 `lazy = false`로 바꾸어 보면서, `PostProjection`(author 필드 미참조)과 `PostProjectionLookup`(author 필드 참조)을 각각 호출해 보면, 실제 쿼리가 발생하는 시점과 방식이 어떻게 달라지는지를 확인할 수 있으며, 이를 통해 Lazy 로딩과 Eager 로딩의 차이점을 직관적으로 살펴볼 수 있습니다.

#### Eager 로딩, @DBRef(lazy = false)

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-3.png)

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-4.png)

#### Lazy 로딩, @DBRef(lazy = true)

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-1.png)

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-2.png)

##### 프록시(CGLIB)로 인한 all-open 설정 (Kotlin)

`@DBRef(lazy = true)`를 사용하면, Spring Data MongoDB가 **CGLIB 프록시**를 생성해 지연 로딩을 구현합니다. 하지만 Kotlin에서는 클래스가 기본적으로 `final`이라, 프록시 생성이 불가능할 수 있습니다. (예: `Cannot subclass final class ...` 오류)

이를 해결하려면 **all-open** 또는 **kotlin-spring** 플러그인을 사용해, `@Document` 클래스들을 자동으로 `open` 처리해야 합니다.

##### 예시: build.gradle.kts

```kotlin
plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.6.21"
    // 또는 id("org.jetbrains.kotlin.plugin.allopen") ...
}

allOpen {
    annotation("org.springframework.data.mongodb.core.mapping.Document")
}
```

- `org.jetbrains.kotlin.plugin.spring`: Spring 관련 애노테이션(`@Component`, `@Configuration` 등)에 대해 자동으로 `open`을 적용해 줍니다.
- `allOpen` 블록에서 **`@Document`** 애노테이션을 추가로 지정하면, MongoDB 엔티티 클래스가 **final**이 아닌 **open** 상태가 되어 CGLIB 프록시 생성이 가능합니다.

이처럼 Projection을 어떻게 구성하느냐에 따라, Lazy 로딩과 Eager 로딩이 **쿼리를 실행하는 시점**이 달라집니다. Lazy 로딩은 필드를 실제로 참조하기 전까지 쿼리가 없지만, 예상치 못한 시점에 쿼리가 발생할 수 있습니다. Eager 로딩은 Post를 가져올 때 Author까지 즉시 조회하여 N+1 문제가 쉽게 드러날 수 있다는 차이가 있습니다.

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

- `authorId` 필드에 **ObjectId**만 저장합니다.
- 필요한 경우, 별도의 쿼리나 `$lookup` Aggregation을 통해 Author 문서를 가져올 수 있습니다.

### 문서 구조

MongoDB에 저장된 **Post** 문서는 다음과 같습니다:

```json
{
  "_id": "67c49519bb7bbd62011d7b13",
  "author_id": "67c49518bb7bbd62011d7b0e",
  "content": "content-1",
  "title": "title-1"
}
```

- `author_id`는 단순히 **ObjectId** 값을 담고 있습니다.
- 어떤 컬렉션을 참조하는지 메타정보(`$ref`)는 없으므로, 애플리케이션 로직에서 `"authors"` 컬렉션을 참조해야 합니다.

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

- DBRef보다 쿼리가 단순합니다.
- 인덱싱, 조회, `$lookup` 활용 등이 모두 ObjectId 필드 기준으로 이뤄집니다.

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

| rows  | LookUp | DBRef lazy false | DBRef lazy true(author 접근) | DBRef lazy true(author 미접근) |
|-------|--------|:-----------------|:---------------------------|:----------------------------|
| 1     | 9.2ms  | 9.6ms            | 9.3ms                      | 8.5ms                       |
| 50    | 11.6ms | 69.7ms           | 69.4ms                     | 8.9ms                       |
| 100   | 16.2ms | 130.1ms          | 133.5ms                    | 11.5ms                      |
| 500   | 42.2ms | 574.2ms          | 575.9ms                    | 23.5ms                      |
| 1,000 | 69.5ms | 1167.4ms         | 1178.3ms                   | 41.9ms                      |

아래 표는 각 **rows** 값(1, 50, 100, 500, 1,000)에 대해 **10번씩 호출**하여 **평균 응답 시간**을 측정한 결과입니다. **LookUp**은 MongoDB의 `\$lookup` 단계를 사용하여 **Post**와 **Author**를 한 번의 쿼리로 조인한 방식이며, **DBRef lazy false**는 `@DBRef(lazy = false)` 설정을 통해 Post 조회 시 즉시 Author 문서를 로딩합니다.

한편, **DBRef lazy true(author 접근)는** `@DBRef(lazy = true)` 상태에서 Author 필드에 실제 접근할 때마다 추가 쿼리가 실행되는 구조이고, **DBRef lazy true(author 미접근)는** 같은 `@DBRef(lazy = true)`지만 Author 필드를 전혀 사용하지 않아 추가 쿼리가 발생하지 않는 상황을 의미합니다.

## 결론

- **DBRef**
    - **장점**: 참조 컬렉션/DB 정보가 명시적으로 포함되며, Spring Data MongoDB에서 자동으로 객체를 가져오기 편리
    - **단점**: Lazy/Eager 모두 대규모 조회 시 N+1 문제, 스키마 변경 시 `$ref` 수정 필요

- **ObjectId 직접 참조**
    - **장점**: 구조가 단순, 인덱싱과 쿼리가 직관적, `$lookup` 등으로 성능 최적화 가능
    - **단점**: 참조할 컬렉션 정보를 로직에서 관리해야 하며, 객체 변환에 추가 코드 필요

**요약하자면**, 소규모 프로젝트나 간단한 PoC라면 DBRef가 편리할 수 있지만, 대규모 트래픽이나 복잡한 조회가 필요한 환경에서는 **ObjectId 직접 참조**가 더 유연하고 성능상 유리할 수 있습니다. 필요 시 `$lookup`을 통해 한 번에 조인하거나, **N+1 문제**를 방지하기 위해 애플리케이션 레벨에서 적절히 쿼리를 조합하면 됩니다.

실제 운영 환경에서는 **테스트(위 섹션 5 참고)**를 통해, 어떤 방식이 요구사항에 부합하는지 확인해보시길 권장합니다.