# Spring Data MongoDB @DBRef vs. ObjectId 직접 참조

MongoDB에서 **문서 간의 연관관계(relationship)를** 표현하는 방법에는 크게 두 가지가 있습니다.

1. **@DBRef**를 이용해 객체 간의 연관관계를 직접 맺는 방식
2. **ObjectId**를 필드로 저장해, 필요 시 해당 ID를 기준으로 다른 문서를 조회하는 방식

이 글에서는 Spring Data MongoDB 환경에서 두 방식의 장단점과, 실제 데이터 구조 및 업데이트 쿼리 차이를 살펴보겠습니다.

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

## Lazy 로딩 vs. Eager 로딩

- **`@DBRef(lazy = true)`**
  - Post 문서를 가져와도 `author` 필드는 즉시 조회되지 않습니다.
  - 실제로 `author` 필드에 **접근**하는 순간, CGLIB 프록시가 동작해 별도의 쿼리를 실행합니다.
  - 초기 응답은 빠를 수 있으나, **접근 시점마다 추가 쿼리**가 발생하여 예측이 어렵습니다.

- **`@DBRef(lazy = false)`**
  - Post 문서를 조회할 때, Author 문서도 **즉시 로딩**(eager loading)합니다.
  - 여러 Post를 한 번에 가져오면, 각각의 Author를 자동으로 해제하므로 **N+1 문제**가 발생할 가능성이 높습니다.

### 코드 예시

아래 코드는 Spring MVC 컨트롤러에서 Post 하나를 조회한 뒤, Projection을 통해 응답을 내려주는 예시입니다.

```kotlin
@RestController
@RequestMapping("/posts")
class PostController(
  private val postRepository: PostRepository,
) {
  @GetMapping("/post")
  fun getPost() = PostProjection(postRepository.findOne())

  @GetMapping("/post-with-author")
  fun getPostWithAuthor() = PostProjectionLookup(postRepository.findOne())
}

data class PostProjection(
  val title: String,
  val content: String,
) {
  constructor(post: Post) : this(
    title = post.title,
    content = post.content,
  )
}

data class PostProjectionLookup(
  val title: String,
  val content: String,
  val author: AuthorProjection,
) {
  constructor(post: Post) : this(
    title = post.title,
    content = post.content,
    author = AuthorProjection(post.author),
  )
}

data class AuthorProjection(
  val name: String,
) {
  constructor(author: Author) : this(
    name = author.name,
  )
}
```

위 코드에서 `Post` 클래스의 `@DBRef(lazy = true) val author: Author` 부분을 `lazy = false`로 바꾸어 보면서, `PostProjection`(author 필드 미참조)과 `PostProjectionLookup`(author 필드 참조)을 각각 호출해 보면, 실제 쿼리가 발생하는 시점과 방식이 어떻게 달라지는지를 확인할 수 있으며, 이를 통해 Lazy 로딩과 Eager 로딩의 차이점을 직관적으로 살펴볼 수 있습니다.

### Eager 로딩, @DBRef(lazy = false)

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-3.png)

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-4.png)

### Lazy 로딩, @DBRef(lazy = true)

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-1.png)

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/mongo-study/images/m-mong-2.png)

#### 프록시(CGLIB)로 인한 all-open 설정 (Kotlin)

**`@DBRef(lazy = true)`**를 사용하면, Spring Data MongoDB가 **CGLIB 프록시**를 생성해 지연 로딩을 구현합니다. 하지만 Kotlin에서는 클래스가 기본적으로 `final`이라, 프록시 생성이 불가능할 수 있습니다. (예: `Cannot subclass final class ...` 오류)

이를 해결하려면 **all-open** 또는 **kotlin-spring** 플러그인을 사용해, `@Document` 클래스들을 자동으로 `open` 처리해야 합니다.

#### 예시: build.gradle.kts

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

## ObjectId 직접 참조 방식

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

**테스트 시나리오**

- 예: 다량의 `Post` 문서를 조회하며 `Author` 정보도 함께 로딩하는 상황
- 각각 DBRef(Lazy/Eager)와 ObjectId(별도 `$lookup` 또는 애플리케이션 레벨 조인) 방식으로 테스트
  **테스트 환경/도구**
- 예: Locust, JMeter, Gatling 등 부하 테스트 도구
- 테스트 시 사용자 수, RPS, 타임라인 등 설정
  **결과 분석**
- DBRef(Eager) 시 N+1 문제 발생 여부, DBRef(Lazy) 시 지연 로딩 쿼리 횟수, ObjectId 직접 참조 시 `$lookup` 쿼리 한 번에 가져오는지
- 응답 시간, CPU/메모리 사용량, MongoDB 쿼리 로그 등
  **요약**
- 어떤 방식이 트래픽이 많을 때 더 유리했는지, DBRef가 성능적으로 부담이 되지는 않았는지 결과 공유

## 결론

- **DBRef**
    - **장점**: 참조 컬렉션/DB 정보가 명시적으로 포함되며, Spring Data MongoDB에서 자동으로 객체를 가져오기 편리
    - **단점**: Lazy/Eager 모두 대규모 조회 시 N+1 문제, 스키마 변경 시 `$ref` 수정 필요

- **ObjectId 직접 참조**
    - **장점**: 구조가 단순, 인덱싱과 쿼리가 직관적, `$lookup` 등으로 성능 최적화 가능
    - **단점**: 참조할 컬렉션 정보를 로직에서 관리해야 하며, 객체 변환에 추가 코드 필요

**요약하자면**, 소규모 프로젝트나 간단한 PoC라면 DBRef가 편리할 수 있지만, 대규모 트래픽이나 복잡한 조회가 필요한 환경에서는 **ObjectId 직접 참조**가 더 유연하고 성능상 유리할 수 있습니다. 필요 시 `$lookup`을 통해 한 번에 조인하거나, **N+1 문제**를 방지하기 위해 애플리케이션 레벨에서 적절히 쿼리를 조합하면 됩니다.

실제 운영 환경에서는 **테스트(위 섹션 5 참고)**를 통해, 어떤 방식이 요구사항에 부합하는지 확인해보시길 권장합니다.