# Spring Data MongoDB 개발 경험에서 얻은 성찰과 적절한 사용 방법

Spring Data MongoDB를 활용하여 애플리케이션을 개발하면서 다양한 고민과 성찰을 통해 "더 나은 설계와 활용"을 위한 몇 가지 인사이트를 정리해보았습니다. 이 글에서는 실제 개발 경험에서 마주했던 문제와 이를 해결하기 위해 도입한 방법들을 공유하며, Spring Data MongoDB를 효과적으로 사용하는 방법에 대해 논의합니다.

## Update Guide

Spring Data MongoDB에서 `mongoRepository.save`, `mongoTemplate.save`, `mongoTemplate.updateOne`의 동작 방식을 상세히 비교하면 다음과 같습니다. 각 메서드는 문서를 업데이트하거나 저장하는 방식에서 차이를 보이며, 특정 시나리오에 적합합니다.

### mongoTemplate.save

문서 전체 교체(Replace)를 수행합니다.

#### 동작 방식

- `_id` 필드를 기준으로 MongoDB에서 문서를 검색.
- 문서가 존재하면 **전체 문서를 교체**합니다.
- 문서가 존재하지 않으면 새로 삽입합니다.
- 저장 객체에 없는 필드는 기존 문서에서 삭제됩니다.

#### 예제

```kotlin
val user = User(id = "123", name = "John Doe", age = 30)
mongoTemplate.save(user)
```

#### 결과

- 기존 문서: `{ "_id": "123", "name": "Alice", "age": 25, "email": "alice@example.com" }`
- 업데이트 후: `{ "_id": "123", "name": "John Doe", "age": 30 }`
- 변경 사항: `email` 필드가 삭제됨.

### mongoRepository.save

문서의 일부 필드만 업데이트(Partial Update)를 수행합니다.

#### 동작 방식

- `_id` 필드를 기준으로 MongoDB에서 문서를 검색.
- 문서가 존재하면 **변경된 필드만 업데이트**하고, 나머지 필드는 유지됩니다.
- 문서가 존재하지 않으면 새로 삽입합니다.

#### 예제

```kotlin
val user = User(id = "123", name = "John Doe")
userRepository.save(user)
```

#### 결과

- 기존 문서: `{ "_id": "123", "name": "Alice", "age": 25, "email": "alice@example.com" }`
- 업데이트 후: `{ "_id": "123", "name": "John Doe", "age": 25, "email": "alice@example.com" }`
- 변경 사항: `name` 필드만 업데이트, 나머지 필드는 유지됨.

### mongoTemplate.updateOne

MongoDB의 **`updateOne`** 명령어를 실행하여 **단일 문서를 부분 업데이트**합니다.

#### 동작 방식

- 조건을 지정하여 MongoDB에서 문서를 검색.
- 첫 번째로 매칭된 문서의 **일부 필드만 업데이트**합니다.
- 문서가 존재하지 않으면 기본적으로 아무 작업도 수행하지 않습니다(삽입하지 않음).
- `$set`과 같은 MongoDB 연산자를 사용하여 지정된 필드만 업데이트합니다.

#### 예제

```kotlin
val query = Query(Criteria.where("name").`is`("Alice"))
val update = Update().set("age", 30)
mongoTemplate.updateOne(query, update, User::class.java)
```

#### 결과

- 기존 문서: `{ "_id": "123", "name": "Alice", "age": 25, "email": "alice@example.com" }`
- 업데이트 후: `{ "_id": "123", "name": "Alice", "age": 30, "email": "alice@example.com" }`
- 변경 사항: `age` 필드만 업데이트, 나머지 필드는 유지됨.

### 주요 차이점 비교

| **특징**             | **mongoRepository.save** | **mongoTemplate.save** | **mongoTemplate.updateOne** |
|--------------------|--------------------------|------------------------|-----------------------------|
| **작업 대상**          | 단일 문서                    | 단일 문서                  | 단일 문서                       |
| **저장 방식**          | 변경된 필드만 업데이트             | 전체 문서 교체               | 변경된 필드만 업데이트                |
| **문서가 없을 경우**      | 새로 삽입                    | 새로 삽입                  | 기본적으로 아무 작업도 수행하지 않음        |
| **업데이트 범위**        | 필드 단위                    | 전체 문서                  | 필드 단위                       |
| **조건 지정**          | `_id` 기준                 | `_id` 기준               | 사용자 정의 쿼리                   |
| **Spring Data 통합** | 페이징, 정렬 등 지원             | 미지원                    | 미지원                         |
| **적합한 상황**         | 간단한 CRUD 작업              | 전체 문서 교체 또는 삽입         | 조건에 맞는 단일 문서 필드 수정          |

## 적절한 방법 선택

