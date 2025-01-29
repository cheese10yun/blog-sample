### Spring Data MongoDB Repository 확장 - Projection과 Pagination을 고려한 설계

기존 [Spring Data MongoDB Repository 확장](https://cheese10yun.github.io/spring-data-mongo-repository/) 포스팅에서는 복잡한 쿼리 로직을 분리하여 상위 레벨에서는 구현 디테일을 신경 쓰지 않고, 데이터 접근 로직을 단순화할 수 있는 방법을 다루었습니다.

특히, **Slice 기반 및 Page 기반의 페이징 처리**를 적용하여 다음과 같은 장점을 얻을 수 있었습니다:

- **Page 기반 페이징 처리**:
   - 카운트 쿼리와 컨텐츠 쿼리를 **병렬로 실행**하여 성능 최적화
- **Slice 기반 페이징 처리**:
   - `hasNext` 처리를 위임하여 반복적인 코드 없이 페이징을 처리

그러나 이러한 방식은 **단순히 도큐먼트 객체(`T`) 타입**을 기준으로 설계되어, **복잡한 데이터 변환**, **조인**, **그룹화** 등 다양한 데이터 처리 작업을 다루는 데 한계가 있었습니다. 특히, **프로젝션(Projection)**을 활용한 **데이터 조회**나 **복잡한 쿼리**는 `Aggregation`을 사용하여 해결해야 하기 때문에, **`Aggregation`을 기반으로 하는 페이징 처리**가 필요합니다.  
따라서, 이번 포스팅에서는 **`MongoTemplate`을 기반으로 `Aggregation`을 활용한 페이징 처리** 방법을 확장하여 이 문제를 해결하는 방법을 다루겠습니다.

## 기존 Query 기반 Pagination과 Slice 처리

기존 `applyPagination`과 `applySlice`는 `MongoTemplate`을 활용하여 `Query` 기반으로 페이징을 처리하는 방식이었습니다.

### Query 기반 Pagination 및 Slice 추상화

```kotlin
protected fun <S : T> applyPagination(
    pageable: Pageable,
    contentQuery: (Query) -> List<S>,
    countQuery: (Query) -> Long
) = runBlocking {
    val content = async { contentQuery(Query().with(pageable)) }
    val totalCount = async { countQuery(Query()) }
    PageImpl(content.await(), pageable, totalCount.await())
}

protected fun <S : T> applySlice(
    pageable: Pageable,
    contentQuery: (Query) -> List<S>
): Slice<S> {
    val content = contentQuery(Query().with(pageable))
    val hasNext = content.size >= pageable.pageSize
    return SliceImpl(content, pageable, hasNext)
}
```

### 사용 예시

기존 `applyPagination`과 `applySlice`를 활용하면 `MongoTemplate`을 사용하여 데이터를 간단하게 조회할 수 있습니다.

#### Slice 조회

```kotlin
override fun findSlice(
    pageable: Pageable,
    name: String?,
    email: String?,
    memberId: String?
): Slice<Member> {
    val criteria = Criteria()
        .apply {
            name?.let { this.and("name").`is`(it) }
            email?.let { this.and("email").`is`(it) }
            memberId?.let { this.and("member_id").`is`(it) }
        }

    return applySlice(
        pageable = pageable,
        contentQuery = {
            mongoTemplate.find<Member>(it.addCriteria(criteria))
        }
    )
}
```

#### Page 조회

```kotlin
override fun findPage(
    pageable: Pageable,
    name: String?,
    email: String?,
    memberId: String?
): Page<Member> {
    val criteria = Criteria().apply {
        name?.let { this.and("name").`is`(it) }
        email?.let { this.and("email").`is`(it) }
        memberId?.let { this.and("member_id").`is`(it) }
    }

    return applyPagination(
        pageable = pageable,
        contentQuery = { mongoTemplate.find<Member>(it.addCriteria(criteria)) },
        countQuery = { mongoTemplate.count(it.addCriteria(criteria), documentClass) }
    )
}
```

이 방식은 기본적인 도큐먼트(`T`) 조회에는 적합 하지만, 프로젝션을 활용한 데이터 조회에는 적용할 수 없는 한계가 있습니다.

## Aggregation을 활용한 Projection 및 Pagination 확장

MongoDB에서는 **Aggregation**을 활용하여 특정 필드만 선택하거나, 데이터를 변환하는 **프로젝션** 외에도, **조인(`$lookup`)**, **그룹화(`$group`)**, **집계(`$count`)**, **정렬(`$sort`)** 등 다양한 작업을 수행할 수 있습니다. 이러한 복잡한 데이터 처리 작업을 효율적으로 다루기 위해서는 **Aggregation** 기반의 페이징을 활용하는 것이 필요합니다. 이 방식은 기존 `Query` 기반 페이징 처리 방식에 비해 더 유연하고 강력한 쿼리 작성이 가능하며, 복잡한 데이터 변환 및 집계도 손쉽게 처리할 수 있습니다.

### Aggregation 기반 Pagination 및 Slice 추상화 코드 설명

이 두 메서드는 MongoDB에서 **Aggregation**을 사용하여 페이징 처리 및 Slice 또는 Page 결과를 반환하는 기능을 제공합니다. 각 메서드는 **Aggregation의 파이프라인**을 동적으로 수정하고, `skip`과 `limit`을 추가하여 페이지네이션을 처리합니다.

### applyPaginationAggregation 메서드

```kotlin
protected fun <S> applyPaginationAggregation(
    pageable: Pageable,
    contentAggregation: Aggregation,
    countAggregation: Aggregation,
    contentQuery: (Aggregation) -> AggregationResults<S>,
    countQuery: (Aggregation) -> AggregationResults<MongoCount>
): PageImpl<S> = runBlocking {
    val skip = pageable.pageNumber * pageable.pageSize
    val limit = pageable.pageSize

    contentAggregation.pipeline.apply {
        this.add(Aggregation.skip(skip.toLong()))
        this.add(Aggregation.limit(limit.toLong()))
    }

    countAggregation.pipeline.apply {
        this.add(Aggregation.count().`as`("count"))
    }

    // Perform queries asynchronously
    val contentDeferred = async { contentQuery(contentAggregation) }
    val countDeferred = async { countQuery(countAggregation) }

    val content = contentDeferred.await().mappedResults
    val totalCount = countDeferred.await().uniqueMappedResult?.count ?: 0L

    PageImpl(content, pageable, totalCount)
}
```

### 설명

1. **`contentAggregation`**:
   - 페이지네이션을 적용하기 위해 `skip`과 `limit`을 `contentAggregation`에 추가합니다. `skip`은 현재 페이지의 첫 번째 항목부터 건너뛸 수 있도록 하며, `limit`은 페이지당 보여줄 항목의 개수를 설정합니다.
2. **`countAggregation`**:
   - 카운트 쿼리를 처리하기 위해 `countAggregation`에서 `$count`를 사용하여 총 항목 수를 계산합니다. 이 단계에서는 `skip`과 `limit`을 적용하지 않고, 전체 항목 수만 계산합니다.
3. **`runBlocking`**:
   - 비동기 처리를 위해 `runBlocking`을 사용하여 `contentQuery`와 `countQuery`를 동시에 실행합니다. 이렇게 함으로써 **페이징 처리 쿼리**와 **카운트 쿼리**를 병렬로 실행하여 성능을 최적화합니다.
4. **쿼리 실행**:
    - **`contentQuery(contentAggregation)`**: `contentAggregation`을 기반으로 데이터를 조회합니다.
    - **`countQuery(countAggregation)`**: `countAggregation`을 기반으로 총 개수를 조회합니다.
5. **응답 생성**:
    - **`content`**: 페이징된 결과 목록.
    - **`totalCount`**: 전체 항목 수.
    - `PageImpl` 객체를 생성하여 결과를 반환합니다.

### 사용된 MongoDB 쿼리

#### 페이징 쿼리

```
db.members.aggregate([
  {
    "$match": {
      "member_id": "memberId"
    }
  },
  {
    "$project": {
      "name": 1.0,
      "email": 1.0
    }
  },
  {
    "$skip": 0.0
  },
  {
    "$limit": 10.0
  }
])
```

#### 카운트 쿼리

```
db.members.aggregate([
  {
    "$match": {
      "member_id": "memberId"
    }
  },
  {
    "$count": "count"
  }
])
```

이 두 쿼리는 각각 페이징 처리된 결과와 전체 항목 수를 계산하는 쿼리입니다. `contentAggregation`에서는 `skip`과 `limit`을 적용하여 데이터를 제한하고, `countAggregation`에서는 총 항목 수를 계산합니다.

### applySliceAggregation 메서드

```kotlin
protected fun <S> applySliceAggregation(
    pageable: Pageable,
    contentAggregation: Aggregation,
    contentQuery: (Aggregation) -> AggregationResults<S>
): Slice<S> {
    val skip = pageable.pageNumber * pageable.pageSize
    val limit = pageable.pageSize
   contentAggregation.pipeline.apply {
        this.add(Aggregation.skip(skip.toLong()))
        this.add(Aggregation.limit(limit.toLong()))
    }
   val results = contentQuery(contentAggregation)
    val content = results.mappedResults
    val hasNext = content.size >= pageable.pageSize
    return SliceImpl(content, pageable, hasNext)
}
```

### **설명**:

1. **`contentAggregation`**:
   - **`contentAggregation`** 는 사용자가 제공한 Aggregation 객체입니다. 이 객체에는 `$match`, `$project`와 같은 데이터 변환 및 필터링 로직이 포함됩니다.
   - `contentAggregation`에 **`skip`** 과 **`limit`** 을 추가하여 페이징을 처리합니다. 이를 통해 주어진 `pageable`에 맞게 데이터를 조회할 수 있습니다.
2. **`contentQuery`**:
   - `contentAggregation`을 기반으로 데이터를 조회하는 `contentQuery` 함수입니다. 이 함수는 `Aggregation`을 받아서 `mongoTemplate.aggregate`를 사용해 데이터를 가져옵니다.
3. **쿼리 실행**:
   - `contentQuery(contentAggregation)`를 실행하여 페이징된 결과를 가져옵니다.
   - `skip`과 `limit`을 포함한 `contentAggregation`을 전달하여 데이터를 필터링합니다.
4. **응답 생성**:
    - **`content`**: 페이징된 결과.
    - **`hasNext`**: `content`의 크기가 `pageable.pageSize`보다 크거나 같으면, 더 많은 데이터가 있다는 뜻으로 `hasNext`를 설정합니다.
    - `SliceImpl` 객체를 생성하여 결과를 반환합니다.

### 사용된 MongoDB 쿼리

```
db.members.aggregate([
  {
    "$match": {
      "name": "11-name",
      "email": "11-asd@asd.com",
      "member_id": "memberId"
    }
  },
  {
    "$project": {
      "name": 1.0,
      "email": 1.0
    }
  },
  {
    "$skip": 0.0
  },
  {
    "$limit": 10.0
  }
])
```

### Aggregation 기반 `Page` 및 `Slice` 조회 예시

#### Slice 조회

```kotlin
override fun findSliceAggregation(
    pageable: Pageable,
    name: String?,
    email: String?,
    memberId: String?
): Slice<MemberProjection> {
    val match = Aggregation.match(
        Criteria().apply {
            name?.let { this.and("name").`is`(it) }
            email?.let { this.and("email").`is`(it) }
            memberId?.let { this.and("member_id").`is`(it) }
        }
    )
    val projection = Aggregation.project()
        .andInclude("name")
        .andInclude("email")

    return this.applySliceAggregation(
        pageable = pageable,
       contentAggregation = Aggregation.newAggregation(match, projection),
        contentQuery = { mongoTemplate.aggregate(it, Member.DOCUMENT_NAME, MemberProjection::class.java) }
    )
}
```

#### Page 조회

```kotlin
override fun findPageAggregation(
    pageable: Pageable,
    name: String?,
    email: String?,
    memberId: String?
): Page<MemberProjection> {

    val match = Aggregation.match(
        Criteria().apply {
            name?.let { this.and("name").`is`(it) }
            email?.let { this.and("email").`is`(it) }
            memberId?.let { this.and("member_id").`is`(it) }
        }
    )
    val projection = Aggregation.project()
        .andInclude("name")
        .andInclude("email")

    return applyPaginationAggregation(
        pageable = pageable,
        contentAggregation = Aggregation.newAggregation(match, projection),
        countAggregation = Aggregation.newAggregation(match),
        contentQuery = { mongoTemplate.aggregate(it, Member.DOCUMENT_NAME, MemberProjection::class.java) },
        countQuery = { mongoTemplate.aggregate(it, Member.DOCUMENT_NAME, MongoCount::class.java) }
    )
}
```

## **결론**

- 기존 `Query` 기반 페이징에서 `T` 타입 한계를 벗어나 **Projection을 지원**하도록 확장
- **`Aggregation`을 활용한 페이징 처리**로 다양한 데이터 변환 및 성능 최적화 가능
- **비동기(`async`) 처리로 성능 최적화**하며, 코드 재사용성을 높임

이제 Spring Data MongoDB에서 **확장 가능한 Projection 기반 Pagination을 활용한 Repository를 구축**할 수 있습니다.