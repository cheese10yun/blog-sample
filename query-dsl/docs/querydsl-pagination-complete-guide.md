# QueryDSL 페이징 처리 완전 정리: Count 병목부터 커서 기반 페이지네이션까지

일반적으로 어드민 페이지처럼 데이터를 테이블 뷰 형식으로 제공할 때는 페이징 기법으로 현재 페이지의 내용과 페이지 정보를 표시합니다. JPA와 Querydsl을 활용하면 이런 반복적인 코드를 비교적 쉽게 작성할 수 있습니다.

데이터 모수가 적고 단순한 구조라면 문제가 없지만, 데이터가 많아지고 여러 테이블을 조인해서 조회해야 하는 구조라면 이야기가 달라집니다. 이 글은 Querydsl로 페이징을 처리하는 가장 단순한 형태에서 출발해, Count 쿼리 병목을 마주치고, 이를 Slice와 코루틴 병렬 처리로 걷어내고, 그럼에도 남는 offset의 구조적 한계를 발견한 뒤, 최종적으로 커서 기반 페이지네이션에 도달하기까지의 과정을 하나의 흐름으로 정리합니다.


## 1. 출발점: Querydsl과 Support 클래스

개발을 하다 보면 다양한 조회 쿼리를 만들게 됩니다. JPA를 사용할 때 Querydsl Support를 활용하면 세부 구현체를 숨기고 Repository를 통해 조회 쿼리를 제공할 수 있다는 장점이 있습니다.

### 1.1 `QuerydslRepositorySupport`는 `from`으로 시작한다

`QuerydslRepositorySupport`는 `JPQLQuery`를 이용해서 JPQL 작업을 진행합니다. 그래서 약간의 단점이 있습니다.

```kotlin
import com.example.querydsl.domain.QPayment.payment as qPayment

class PaymentCustomRepositoryImpl : Querydsl4RepositorySupport(Payment::class.java), PaymentCustomRepository {

    override fun findUseFrom(targetAmount: BigDecimal): List<Payment> {
        return from(qPayment)
            .select(qPayment)
            .where(qPayment.amount.gt(targetAmount))
            .fetch()
    }
}
```

해당 코드를 보면 `from`으로 시작해야 합니다. `QuerydslRepositorySupport` 구현체의 `from` 메서드는 아래와 같습니다.

```java
protected <T> JPQLQuery<T> from(EntityPath<T> path) {
    return getRequiredQuerydsl().createQuery(path).select(path);
}
```

`JPQLQuery`를 사용해서 쿼리 작업을 진행해야 하기 때문에 `from`으로 시작할 수밖에 없는 구조입니다.

별거 아닌 것 같지만, 우리는 일반적으로 쿼리를 시작할 때 `select`로 시작합니다. `queryFactory`를 이용하면 `select`, `selectFrom`으로 쿼리를 시작할 수 있습니다.

```kotlin
override fun findUseSelectFrom(targetAmount: BigDecimal): List<Payment> {
    return selectFrom(qPayment)
        .where(qPayment.amount.gt(targetAmount))
        .fetch()
}

override fun findUseSelect(targetAmount: BigDecimal): List<Long> {
    return select(qPayment.id)
        .from(qPayment)
        .where(qPayment.amount.gt(targetAmount))
        .fetch()
}
```

`selectFrom`은 조회하는 타입이 일치하는 경우 사용할 수 있고, `select`는 반환받을 타입을 지정할 수 있습니다. 우리가 흔히 쓰는 SQL 문과 아주 유사한 구조입니다.

### 1.2 `select`/`selectFrom`을 제공하는 Support

`QuerydslRepositorySupport`를 상속하는 클래스를 직접 정의해서 `select`, `selectFrom`을 제공하면 됩니다.

```kotlin
abstract class Querydsl4RepositorySupport(domainClass: Class<*>) : QuerydslRepositorySupport(domainClass) {

    protected var queryFactory: JPAQueryFactory by Delegates.notNull()

    @PersistenceContext
    override fun setEntityManager(entityManager: EntityManager) {
        this.queryFactory = JPAQueryFactory(entityManager)
        super.setEntityManager(entityManager)
    }

    protected fun <T> select(expr: Expression<T>): JPAQuery<T> {
        return queryFactory.select(expr)
    }

    protected fun <T> selectFrom(from: EntityPath<T>): JPAQuery<T> {
        return queryFactory.selectFrom(from)
    }
}
```

`EntityManager`는 상위 클래스에서 전달받고, `JPAQuery`가 제공하는 `select`, `selectFrom`을 구현하면 됩니다. 이 두 메서드 외에도 다양한 기능을 계속 추가해 나갈 수 있습니다 — 이 글에서 다루는 페이징 로직도 이 Support 클래스에 차례로 얹힐 예정입니다.

테스트로 정상 동작을 확인합니다.

```kotlin
@Test
internal fun `findUseSelectForm`() {
    //given
    val targetAmount = 200.toBigDecimal()

    //when
    val payments = paymentRepository.findUseSelectFrom(targetAmount)

    //then
    then(payments).allSatisfy(
        Consumer {
            then(it.amount).isGreaterThan(targetAmount)
        }
    )
}
```

`select`, `selectFrom`으로 시작한다고 해서 성능상 큰 이득이 생기는 것은 아니지만, 최대한 SQL에 가깝게 코드를 작성할 수 있다는 점에서 실무에서 선호할 만한 방식입니다.

이제 이 Support 위에 실제 서비스에서 가장 많이 쓰이는 기능, 페이징을 얹어보겠습니다. 데이터가 늘어날수록 가장 먼저 발목을 잡는 것은 Count 쿼리입니다.


## 2. 첫 번째 병목: Count 쿼리

데이터 모수가 적고 조회 구조가 단순할 때는 Querydsl의 `applyPagination` 메서드를 활용하면 페이징 로직을 쉽게 작성할 수 있습니다.

### 2.1 기본 페이징 구현

```kotlin
class OrderCustomRepositoryImpl : QuerydslRepositorySupport(Order::class.java), OrderCustomRepository {

    override fun findPagingBy(
        pageable: Pageable,
        address: String
    ): Page<Order> {
        val query: JPAQuery<Order> = from(order).select(order).where(order.address.eq(address))
        val content: List<Order> = querydsl.applyPagination(pageable, query).fetch()
        val totalCount: Long = query.fetchCount()
        return PageImpl(content, pageable, totalCount)
    }
}
```

앞서 1장에서 살펴본 `QuerydslRepositorySupport`를 기반으로 `JpaRepository`를 확장해 페이징 로직을 구현했습니다.

세부 구현체의 조회 로직을 보면, Querydsl을 기반으로 `JPAQuery`를 생성하며 필요한 조회 조건을 작성합니다. 이 쿼리 객체를 그대로 이용해 Content 조회와 전체 레코드 수 조회를 차례로 수행합니다. 실제 실행되는 쿼리를 확인해보겠습니다.

```sql
select order0_.id           as id1_4_,
       ...
       order0_.address      as address4_4_,
       order0_.created_at   as created_2_4_,
       order0_.updated_at   as updated_3_4_,
from orders order0_
where order0_.address = ?
limit ?, ?

select count(order0_.id) as col_0_0_
from orders order0_
where order0_.address = ?
```

Content 조회에 필요한 쿼리와 전체 레코드 조회에 필요한 쿼리를 `JPAQuery` 하나로 동일하게 사용할 수 있고, `applyPagination` 메서드로 offset·limit 관련 페이징 로직을 간단히 구현할 수 있다는 게 큰 장점입니다.

`applyPagination`을 활용하면 개발 생산성 측면에서 분명한 이점이 있습니다. 그러나 모든 개발 결정에는 트레이드오프가 따릅니다. 편리한 기능을 즉시 쓸 수 있는 대신, 나중에 이자를 포함한 비용을 치러야 할 수도 있습니다. 어떤 문제가 발생하는지 살펴보겠습니다.

### 2.2 Count는 왜 병목인가

Count 쿼리는 특정 조건에 해당하는 전체 레코드 수를 조회하는 구조라, 데이터 총량이 늘어날수록 성능 저하가 발생할 수 있습니다. Content를 조회하는 limit·offset 쿼리는(offset이 비교적 크지 않은 초반 구간에서는) 빠르게 처리되는 반면, **Count 쿼리는 시간이 오래 걸려 병목이 될 수 있습니다.** 여러 테이블을 조인해서 조회하는 경우에는 조회 조건이 복잡해져 정확한 인덱스를 타기 어려워지는 문제도 함께 발생합니다. 이는 조회 조건에 부합하는 전체 레코드를 Count 하는 구조에서 필연적으로 발생하는 문제입니다.

### 2.3 Content 쿼리를 Count에 재사용하면 손해다

Content 조회 쿼리와 레코드 Count 조회 쿼리를 `JPAQuery` 하나로 동일하게 처리하면 성능적인 손해가 발생할 수 있습니다. 특히 여러 테이블을 조인해서 데이터를 조회하는 경우에 이 문제가 더 두드러집니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/query-dsl/docs/images/001.png)

주문을 조회할 때 사용자 및 쿠폰 정보를 함께 내려줘야 하는 상황이라도, 조회 필터에 주문 정보만 있다면 Count 쿼리는 다른 테이블과의 조인 없이 주문 테이블만으로 작성하는 것이 효율적입니다.

```sql
-- Content 조회 쿼리
select o.*,
       u.*,
       c.*
from orders o
         left join coupon c on o.coupon_id = c.id
         inner join user u on o.user_id = u.id
where o.address = ? limit ?, ?
;

-- Content 조회 쿼리를 그대로 사용하는 경우
select count(o.id)
from orders o
         left join coupon c on o.coupon_id = c.id
         inner join user u on o.user_id = u.id
where o.address = ?
;

-- Content 쿼리를 사용하지 않고 별도의 Count 조회 쿼리
select count(o.id) as count
from orders o
where o.address = ?
;
```

주문 조회에서 `address` 필드만 조회 조건에 해당한다면, 사용자·쿠폰 테이블과의 조인은 필요하지 않습니다. 이 경우 Count 쿼리는 주문 테이블만을 대상으로 간단하게 작성하는 것이 효율적입니다. 조회 조건이 복잡해질수록, Count 쿼리를 별도로 작성하는 편이 성능적으로 유리합니다.


## 3. Count를 걷어내는 두 가지 방법

Count 쿼리로 인한 병목은 크게 두 갈래로 풀 수 있습니다. Count 쿼리 자체를 아예 실행하지 않거나(Slice), 앞서 살펴본 것처럼 Count 쿼리를 Content 쿼리와 분리해 조건에 맞게 최적화하는 것입니다.

### 3.1 Slice: Count를 아예 실행하지 않기

그렇다면 Count 쿼리가 반드시 필요할까요? 실제로 페이지 네비게이션이 있는 화면이라도, 사용자가 26페이지에 원하는 데이터가 있을 것이라고 예상하고 바로 넘어가는 경우는 거의 없습니다. 대부분 다음 페이지로 넘어가는 방식으로 탐색합니다. 이런 경우라면 무거운 Count 쿼리 없이, 다음 데이터가 있는지 여부만 내려주는 Slice 방식이 더 효율적입니다.

JPA Slice 방식은 Page 방식과 달리 Total Count를 조회하는 쿼리를 실행하지 않습니다. 대용량 데이터의 페이징 처리에 특히 유용하며, Total Count가 꼭 필요한 데이터인지 비즈니스적으로 확인해보고 필요하지 않다면 사용하지 않는 것을 권장합니다.

Spring Data는 Slice를 통해 Total Count를 조회하지 않는 페이징 처리를 지원합니다. 처음 떠올리기 쉬운 구현은 이런 모습입니다.

```kotlin
class OrderCustomRepositoryImpl : QuerydslRepositorySupport(Order::class.java), OrderCustomRepository {
    override fun findSliceBy(pageable: Pageable, address: String): Slice<Order> {
        val query: JPAQuery<Order> = from(order).select(order).where(order.address.eq(address))
        val content: List<Order> = querydsl.applyPagination(pageable, query).fetch()
        val hasNext: Boolean = content.size >= pageable.pageSize
        return SliceImpl(content, pageable, hasNext)
    }
}
```

Total Count가 없으니 남은 문제는 하나입니다. **다음 페이지가 있는지 없는지를, Count 없이 어떻게 알 수 있을까요?** 위 구현은 요청한 `pageSize`만큼 조회한 뒤 `content.size >= pageable.pageSize`로 `hasNext`를 판단합니다. 그런데 이 판단은 함정이 있습니다.

Order 데이터가 총 22건 있고, size를 22로 요청했다고 가정해보겠습니다.

| 구현 | 총 22건, 요청 size | 반환 Content | `hasNext` | 판정 |
|------|-------------------|--------------|-----------|------|
| `content.size >= pageSize` | 22 | 22 | **true** | ✗ 다음 페이지가 없는데 있다고 답함 |

요청한 22건이 정확히 22건 반환됐다는 이유만으로 `hasNext`가 `true`가 되어버립니다. 실제로는 더 읽을 데이터가 없는데도 "다음 페이지가 있다"고 잘못 응답하는 것입니다. **Count 없이 다음 페이지 존재 여부를 판단하려면, 딱 그만큼만 읽어서는 안 됩니다.**

해결책은 간단합니다. **한 건 더 읽어보는 것**입니다. `pageSize + 1`개를 조회해서, 실제로 `pageSize`를 초과하는 데이터가 있는지로 `hasNext`를 판단합니다.

```kotlin
protected fun <T> applySlicePagination(
    pageable: Pageable,
    query: Function<JPAQueryFactory, JPAQuery<T>>
): Slice<T> {
    val jpaContentQuery = query.apply(queryFactory)
    val content = querydsl!!.applyPagination(pageable, jpaContentQuery)
        .limit((pageable.pageSize + 1).toLong()) // applyPagination이 건 limit을 덮어씀
        .fetch()
    val hasNext = content.size > pageable.pageSize
    return SliceImpl(content.take(pageable.pageSize), pageable, hasNext)
}
```

`applyPagination`이 이미 설정한 `limit`을 `pageSize + 1`로 다시 덮어쓰고, 판정 기준도 `>=`에서 `>`로 바꿉니다. 대신 호출자에게는 초과분 1건이 새어 나가면 안 되므로 `take(pageSize)`로 실제 반환 분량을 다시 잘라냅니다.

같은 22건, size 5 기준으로 다시 확인해보겠습니다.

| Page | 요청 size | 실제 조회 | 반환 Content | `hasNext` |
|------|-----------|-----------|--------------|-----------|
| 0~3  | 5         | 6         | 5            | true      |
| 4    | 5         | 2         | 2            | false     |

Page 4에서는 남은 데이터가 2건뿐이라 `pageSize + 1`(6건)을 요청해도 2건만 돌아오고, `2 > 5`가 거짓이므로 `hasNext`는 정확히 `false`가 됩니다. Count 쿼리를 실행하지 않고도, 그리고 끝까지 읽어보지 않고도 — **한 건만 더 읽어보면** 다음 페이지 존재 여부를 정확히 판단할 수 있습니다.

> 이 `+1` 조회 기법은 뒤에서 다룰 커서 기반 페이지네이션(`applyCursorPagination`)에서도 동일하게 등장합니다.

Total Count가 반드시 필요하지 않은 화면이라면 대부분 Slice 방식이 효율적입니다. 예를 들어 최근 주문 정보를 기반으로 회원 등급을 업데이트하는 배치 기능이라면 Count 쿼리를 쓸 이유가 없습니다. 단순히 필요한 데이터를 offset과 limit으로 읽고 처리하면 되기 때문입니다. Count 쿼리는 데이터양에 비례해 시간이 걸리므로, 데이터양이 많을수록 계속 사용하는 것은 성능상 부담이 됩니다. [Spring Batch HTTP Page Item Reader](https://cheese10yun.github.io/spring-batch-http-page-item-reader/)처럼 대량의 데이터를 처리하는 배치 애플리케이션에 API를 제공할 때는 Slice 기반으로 제공하는 것이 성능적으로 유리합니다.

### 3.2 Count 쿼리를 따로 최적화하기

Total Count가 반드시 필요한 경우에는 Slice 방식을 사용할 수 없으므로 Page 방식을 사용해야 합니다. 앞서 살펴봤듯 여러 테이블을 조인해서 복잡한 데이터를 조회하는 경우에는 Count 쿼리를 별도로 구현하는 것이 성능적인 이점을 가져올 수 있습니다.

**이는 `AbstractJPAQuery`의 `fetchCount()`가 Deprecated된 이유 중 하나입니다.** 조인이 많거나 복잡한 쿼리에서 `fetchCount`를 사용하면 성능 저하가 발생할 수 있기 때문에, 다른 방식으로 count 쿼리를 실행하도록 권장하고 있습니다.

Count 쿼리를 별도로 구현하면 다음과 같이 작성할 수 있습니다.

```kotlin
class OrderCustomRepositoryImpl : QuerydslRepositorySupport(Order::class.java), OrderCustomRepository {
    override fun findPagingBy(pageable: Pageable, address: String): Page<Order> {
        val content: List<Order> = from(order)
            .select(order)
            .innerJoin(user).on(order.userId.eq(user.id))
            .leftJoin(coupon).on(order.couponId.eq(coupon.id))
            .where(order.address.eq(address))
            .run {
                querydsl.applyPagination(pageable, this).fetch()
            }
        val totalCount: Long = from(order)
            .select(order.count())
            .where(order.address.eq(address))
            .fetchFirst()

        return PageImpl(content, pageable, totalCount)
    }
}
```

`PageImpl`로 Page 객체를 생성할 때, `totalCount`를 Content 쿼리와 별도로 구현해 작성합니다. `totalCount`를 구할 때는 `SimpleExpression`의 `count()`를 사용해 질의합니다. 최종적으로 실행되는 쿼리를 살펴보겠습니다.

```sql
-- Content 쿼리
select order0_.id           as id1_4_,
       order0_.created_at   as created_2_4_,
       order0_.updated_at   as updated_3_4_,
       order0_.address      as address4_4_,
       order0_.coupon_id    as coupon_i5_4_,
       order0_.order_number as order_nu6_4_,
       order0_.user_id      as user_id7_4_
from orders order0_
         inner join user user1_ on (order0_.user_id = user1_.id)
         left outer join coupon coupon2_ on (order0_.coupon_id = coupon2_.id)
where order0_.address = ? limit ?, ?
;

-- Count 쿼리
select count(order0_.id) as col_0_0_
from orders order0_
where order0_.address = ? limit ?
;
```

Content 쿼리는 여러 테이블의 조인을 통해 필요한 정보를 가져오고, Count 쿼리는 조회 조건에 필요한 정보만 가져옵니다. `fetchCount()`가 Deprecated 되었기 때문에 `fetchFirst()`로 대체합니다. 이렇게 Count 쿼리를 따로 구현하면, Count 조건에 맞는 방식으로 최적화하여 성능적인 이점을 얻을 수 있습니다.


## 4. Count와 Content를 동시에: 코루틴 병렬

### 4.1 왜 병렬이 가능한가

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/query-dsl/docs/images/002.png)

Count 쿼리가 1,000ms, 이어지는 Content 쿼리가 500ms 걸린다고 가정하면 순차 실행 시 총 1,500ms가 소요됩니다. 이 작업이 조회할 때마다 반복되면 성능에 문제가 생길 수 있습니다. **그러나 이 두 작업은 서로 의존성이 없기 때문에 병렬로 처리할 수 있습니다.**

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/query-dsl/docs/images/003.png)

Count 쿼리와 Content 쿼리를 병렬로 처리하면, Count 쿼리의 소요 시간이 더 길더라도 전체 작업을 1,000ms에 끝낼 수 있습니다. 코루틴으로 병렬 처리를 구현해보겠습니다.

### 4.2 코루틴 구현

`findPagingBy` 메서드 내에서 코루틴의 `async`를 사용해 Content 조회 쿼리와 Count 쿼리를 동시에 실행합니다. 병렬 처리를 위해 두 작업 모두 `Dispatchers.IO`에서, 별도의 I/O 전용 스레드로 실행됩니다.

```kotlin
override fun findPagingBy(pageable: Pageable, address: String): Page<Order> = runBlocking {
    log.info("findPagingBy thread : ${Thread.currentThread()}")
    val content: Deferred<List<Order>> = async(Dispatchers.IO) {
        log.info("content thread : ${Thread.currentThread()}")
        from(order)
            .select(order)
            .innerJoin(user).on(order.userId.eq(user.id))
            .leftJoin(coupon).on(order.couponId.eq(coupon.id))
            .where(order.address.eq(address))
            .run {
                querydsl.applyPagination(pageable, this).fetch()
            }
    }
    val totalCount: Deferred<Long> = async(Dispatchers.IO) {
        log.info("count thread : ${Thread.currentThread()}")
        from(order)
            .select(order.count())
            .where(order.address.eq(address))
            .fetchFirst()
    }
    PageImpl(content.await(), pageable, totalCount.await())
}
```

### 4.3 JDBC는 블로킹이다

실행 로그를 보면 요청 스레드와 코루틴 스레드가 어떻게 분리되는지 확인할 수 있습니다.

```
INFO [nio-8080-exec-2] OrderApi: thread api : Thread[http-nio-8080-exec-2,5,main]
INFO [-2 @coroutine#4] OrderCustomRepositoryImpl: findPagingBy thread : Thread[http-nio-8080-exec-2 @coroutine#4,5,main]
INFO [-1 @coroutine#5] OrderCustomRepositoryImpl: content thread : Thread[DefaultDispatcher-worker-1 @coroutine#5,5,main]
```

- **요청 스레드**: `OrderApi`는 HTTP 요청을 처리하는 `http-nio-8080-exec-2` 스레드에서 실행되고, `findPagingBy`도 이 스레드에서 시작해 코루틴을 생성합니다.
- **병렬 실행 스레드**: `async(Dispatchers.IO)`로 실행되는 content·count 쿼리는 각각 별도의 I/O 전용 워커 스레드에서 동시에 처리됩니다.

이 병렬 처리가 중요한 이유는 **JDBC 드라이버가 기본적으로 동기·블로킹 방식으로 동작하기 때문입니다.** JDBC 드라이버는 쿼리를 실행하면 호출한 스레드를 블로킹한 채로 결과를 기다립니다. 만약 `runBlocking`의 컨텍스트(주로 HTTP 요청 스레드)에서 그대로 실행하면, 한 작업이 끝날 때까지 스레드가 점유되어 결국 순차 처리와 다를 바 없어집니다. `Dispatchers.IO`를 사용해 블로킹 작업을 별도의 I/O 전용 스레드 풀로 옮겨야, 서로 다른 스레드에서 동시에 실행되어 실제로 병렬성을 확보할 수 있습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/query-dsl/docs/images/004.png)

VM 옵션에 `-Dkotlinx.coroutines.debug`를 추가하면 실행 중인 코루틴이 어떤 스레드에서 도는지 직접 확인할 수 있습니다.

### 4.4 테스트로 검증

Count 쿼리에 1,000ms, Content 쿼리에 500ms의 인위적 지연을 걸어 병렬 처리 효과를 검증합니다. 두 작업이 병렬로 실행되면 전체 소요 시간은 순차 합산(1,500ms)이 아니라 오버헤드를 포함해 약 1,000ms 내외(1,037ms)로 측정됩니다.

```kotlin
@Test
fun `count 1,000ms, content 500ms Thread sleep test`() = runBlocking {
    val time = measureTimeMillis {
        orderRepository.findPaging3By(
            pageable = PageRequest.of(0, 10),
            address = "address"
        )
    }
    println("${time}ms") // 1037ms
}
```

`findPaging3By`는 내부적으로 두 블로킹 작업(Count 쿼리, Content 쿼리)을 `async(Dispatchers.IO)`로 별도의 I/O 스레드에서 병렬 실행합니다. 순차 실행이라면 1,000ms + 500ms = 1,500ms가 걸려야 하지만, 병렬 실행 덕분에 전체 소요 시간이 약 1,000ms대로 줄어듭니다. 이 결과는 JDBC처럼 블로킹 I/O를 사용하는 환경에서도 코루틴 기반 병렬 처리가 유효한 동시성 확보 수단이라는 것을 보여줍니다.


## 5. 반복 걷어내기: Support로 위임

지금까지는 각 Repository 구현체마다 Count·Slice·병렬 처리 로직을 직접 작성했습니다. 이제 이 반복을 걷어내어, 1장에서 만든 `Querydsl4RepositorySupport`에 메서드로 편입시키겠습니다.

### 5.1 Support에 페이징 메서드 추가

Slice, Page 등 페이징 처리에서 반복되는 로직을 피하고 편리하게 사용하기 위해, 지금까지 다뤄온 로직을 Support 클래스에 위임합니다. `select`, `selectFrom`만 제공하던 1장의 `Querydsl4RepositorySupport`에 `applyPagination`과 `applySlicePagination`을 추가합니다.

```kotlin
protected fun <T> applyPagination(
    pageable: Pageable,
    contentQuery: Function<JPAQueryFactory, JPAQuery<T>>,
    countQuery: Function<JPAQueryFactory, JPAQuery<Long>>
): Page<T> = runBlocking {
    val jpaContentQuery = contentQuery.apply(queryFactory)
    val content = async(Dispatchers.IO) { querydsl!!.applyPagination(pageable, jpaContentQuery).fetch() as List<T> }
    val count = async(Dispatchers.IO) { countQuery.apply(queryFactory).fetchFirst() }

    PageImpl(content.await(), pageable, count.await())
}

protected fun <T> applySlicePagination(
    pageable: Pageable,
    query: Function<JPAQueryFactory, JPAQuery<T>>
): Slice<T> {
    val jpaContentQuery = query.apply(queryFactory)
    val content = querydsl!!.applyPagination(pageable, jpaContentQuery)
        .limit((pageable.pageSize + 1).toLong()) // applyPagination이 건 limit을 덮어씀
        .fetch()
    val hasNext = content.size > pageable.pageSize
    return SliceImpl(content.take(pageable.pageSize), pageable, hasNext)
}
```

- `applyPagination`은 `Pageable`과 Content 쿼리(`contentQuery`), Count 쿼리(`countQuery`)를 입력받아 4장에서 다룬 코루틴 병렬 처리를 그대로 수행합니다.
- `applySlicePagination`은 Content 쿼리만 입력받고 `hasNext` 판별까지 캡슐화합니다 — 이 구현은 3장에서 바로잡은 `+1` 조회 방식을 반영한 최종 형태입니다.

### 5.2 AS-IS / TO-BE

```kotlin
class OrderCustomRepositoryImpl : Querydsl4RepositorySupport(Order::class.java), OrderCustomRepository {
    // Slice 로직 AS-IS
    override fun findSliceBy(
        pageable: Pageable,
        address: String
    ): Slice<Order> {
        val query: JPAQuery<Order> = from(order).select(order).where(order.address.eq(address))
        val content: List<Order> = querydsl!!.applyPagination(pageable, query).fetch()
        val hasNext: Boolean = content.size >= pageable.pageSize
        return SliceImpl(content, pageable, hasNext)
    }

    // Slice 로직 TO-BE
    override fun findSliceBy2(
        pageable: Pageable,
        address: String
    ): Slice<Order> {
        return applySlicePagination(
            pageable = pageable,
            query = {
                selectFrom(order).where(order.address.eq(address))
            }
        )
    }

    // Page 로직 AS-IS
    override fun findPagingBy(
        pageable: Pageable,
        address: String
    ): Page<Order> = runBlocking {
        val content: Deferred<List<Order>> = async(Dispatchers.IO) {
            from(order)
                .select(order)
                .innerJoin(user).on(order.userId.eq(user.id))
                .leftJoin(coupon).on(order.couponId.eq(coupon.id))
                .where(order.address.eq(address))
                .run {
                    querydsl!!.applyPagination(pageable, this).fetch()
                }
        }
        val totalCount: Deferred<Long> = async(Dispatchers.IO) {
            from(order)
                .select(order.count())
                .where(order.address.eq(address))
                .fetchFirst()
        }

        PageImpl(content.await(), pageable, totalCount.await())
    }

    // Page 로직 TO-BE
    override fun findPaging1(
        pageable: Pageable
    ): Page<Order> {
        return applyPagination(
            pageable = pageable,
            contentQuery = { selectFrom(order).where(order.userId.isNotNull) },
            countQuery = { select(order.count()).from(order).where(order.userId.isNotNull) },
        )
    }

}
```

`Querydsl4RepositorySupport`를 상속받는 것만으로 `applyPagination`과 `applySlicePagination`을 바로 사용할 수 있습니다. 페이징 로직의 반복 구현은 모두 Support 클래스로 위임되고, 각 Repository는 조회 쿼리만 작성하면 되는 구조로 코드가 한결 간결해졌습니다.

여기까지 오면서 Count 쿼리로 인한 병목은 Slice로 걷어내거나 코루틴으로 병렬 처리해 완화했습니다. 그런데 두 해법 모두 여전히 `limit`, `offset`을 사용합니다. offset 기반 조회에는 아직 손대지 않은 근본적인 한계가 남아 있습니다.


## 6. 그래도 남는 한계: offset

### 6.1 offset은 커질수록 느려진다

두 방식 모두 `limit`, `offset` 기반이라는 공통점이 있습니다. **offset이 커질수록 조회 성능이 선형적으로 저하됩니다.**

> 보다 자세한 내용은 [Spring Batch Reader 성능 분석](https://cheese10yun.github.io/spring-batch-reader-performance/)을 참고해 주세요.

`JpaPagingItemReader`(offset 방식)와 `QueryDslNoOffsetPagingReader`(No Offset 방식)의 실측 성능을 비교하면 offset의 한계가 명확하게 드러납니다.

| Reader                       | rows      | 소요 시간        |
|------------------------------|-----------|--------------|
| JpaPagingItemReader          | 10,000    | 778 ms       |
| JpaPagingItemReader          | 100,000   | 8,912 ms     |
| JpaPagingItemReader          | 500,000   | 205,469 ms   |
| JpaPagingItemReader          | 1,000,000 | 1,048,979 ms |
| QueryDslNoOffsetPagingReader | 10,000    | 658 ms       |
| QueryDslNoOffsetPagingReader | 100,000   | 3,523 ms     |
| QueryDslNoOffsetPagingReader | 500,000   | 15,501 ms    |
| QueryDslNoOffsetPagingReader | 1,000,000 | 28,732 ms    |

`JpaPagingItemReader` 기준으로 10만 건과 50만 건의 차이는 단순히 5배가 아니라 23배 이상입니다. 반면 `QueryDslNoOffsetPagingReader`는 rows가 늘어나도 선형에 가까운 증가를 보입니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/batch-study/docs/img/reader-performance-1.png)

그래프에서 `JpaPagingItemReader`(파란색)의 수치가 워낙 커서 `QueryDslNoOffsetPagingReader`(빨간색)의 선이 거의 보이지 않을 정도입니다. `JpaPagingItemReader`를 제외하고 두 리더만 비교해보면 `QueryDslNoOffsetPagingReader`가 얼마나 안정적인지 알 수 있습니다.

원인을 실행 계획으로 확인해보겠습니다.

### 6.2 실행 계획으로 확인

**JpaPagingItemReader - 첫 번째 청크 (offset 0)**

```sql
SELECT *
FROM payment
WHERE created_at >= ?
ORDER BY created_at DESC
LIMIT 1000;
```

| type  | key            | Extra                 |
|-------|----------------|-----------------------|
| range | IDX_created_at | Using index condition |

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/batch-study/docs/img/explain_1.png)

`type: range`로 인덱스가 정상적으로 동작합니다.

**JpaPagingItemReader - 마지막 청크 (offset 4,999,000)**

```sql
SELECT *
FROM payment
WHERE created_at >= ?
ORDER BY created_at DESC
LIMIT 4999000, 1000;
```

| type | key  | Extra                       |
|------|------|-----------------------------|
| ALL  | NULL | Using where; Using filesort |

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/batch-study/docs/img/explan_2.png)

`type: ALL`, 즉 **풀 테이블 스캔**이 발생합니다. 인덱스도 사용하지 않습니다.

### 6.3 왜 풀 스캔이 발생하는가

`offset`의 본질적인 문제를 이해하려면 DB가 offset을 처리하는 방식을 알아야 합니다. `LIMIT 4999000, 1000`은 "4,999,000번째 행부터 1,000건을 가져와라"라는 의미인데, 데이터베이스는 4,999,000번째 행이 어디 있는지 바로 알 수 없습니다. 버스에서 10번째 줄에 누가 앉아 있는지 확인하려면 앞에서부터 9줄을 직접 세어가며 확인해야 하는 것처럼, offset도 그 위치까지 도달하려면 앞의 데이터를 모두 읽고 버려야 합니다.

즉 `LIMIT 4999000, 1000` 쿼리는 4,999,000건을 읽고 전부 버린 뒤, 그다음 1,000건만 돌려주는 동작을 합니다. 실제로 반환되는 데이터는 1,000건이지만 내부적으로는 500만 건 가까운 데이터를 스캔하는 셈입니다.

여기서 옵티마이저의 비용 판단이 개입합니다. 인덱스를 통해 레코드 1건을 읽는 것은 테이블에서 직접 1건을 읽는 것보다 4~5배 비용이 듭니다. 데이터 모수가 적을 때는 인덱스를 타는 편이 훨씬 효율적이지만, 읽어야 할 범위가 테이블 전체의 20~25%를 넘어서면 옵티마이저는 인덱스로 한 건씩 찾아가는 것보다 테이블을 통째로 풀 스캔하는 편이 낫다고 판단합니다. 그 결과 후반 청크에서는 인덱스가 사라지고 풀 스캔이 선택됩니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/batch-study/docs/img/limit_3.png)

### 6.4 No Offset은 왜 일정한가

No Offset 방식은 `WHERE id < 마지막 조회 ID` 조건으로, offset 없이 다음 데이터를 찾습니다.

```sql
-- 첫 번째 청크
SELECT *
FROM payment
WHERE created_at >= ?
  AND id >= 1
  AND id <= 1000
ORDER BY id ASC
LIMIT 1000;

-- 마지막 청크
SELECT *
FROM payment
WHERE created_at >= ?
  AND id >= 4999001
  AND id <= 5000000
ORDER BY id ASC
LIMIT 1000;
```

| type  | key     | Extra       |
|-------|---------|-------------|
| range | PRIMARY | Using where |

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/batch-study/docs/img/explain_3.png)

**첫 번째 청크와 마지막 청크의 실행 계획이 동일합니다.** PK 인덱스(`PRIMARY`)를 기준으로 범위 조회하기 때문에, offset이 누적되어도 스캔 비용이 일정하게 유지됩니다.

정리하면, offset/limit 방식은 offset이 커질수록 그 위치까지 도달하기 위한 스캔 비용이 누적되어 결국 풀 스캔으로 전환되는 구조적 한계를 가집니다. No Offset 방식은 이 문제를 커서 조건으로 원천 차단합니다.


## 7. 커서 기반 페이지네이션

### 7.1 배치 Reader와 API 탐색은 요구가 다르다

6장에서 살펴본 `QueryDslNoOffsetPagingReader`도 커서 기반 발상을 활용하지만, 그 용도는 **배치 Item Reader**에 특화되어 있습니다. 데이터를 처음부터 끝까지 한 방향으로 순차 처리하는 것이 목적이기 때문에 "다음 청크"만 지원하면 충분합니다.

반면 **REST API로 페이지 탐색 기능을 제공**할 때는 요구사항이 다릅니다. 사용자 또는 어드민이 화면에서 다음과 같은 탐색을 요청할 수 있습니다.

- **첫 번째 페이지로 이동** (FIRST)
- **마지막 페이지로 이동** (LAST)
- **현재 페이지에서 다음 페이지로** (NEXT)
- **현재 페이지에서 이전 페이지로** (PREV)

`applyCursorPagination`은 이 네 가지 방향을 모두 지원하는 **양방향 커서 탐색 API**를 제공하기 위해 설계되었습니다.

### 7.2 커서란 무엇인가

커서 기반 페이지네이션의 핵심 아이디어는 `offset`을 없애는 것입니다. `offset` 대신 **마지막으로 조회한 데이터의 ID를 커서(cursor)로 삼아 `WHERE id < :cursor` 조건으로 다음 데이터를 가져오는 방식**입니다.

```sql
-- offset 방식 (뒤로 갈수록 느려짐)
SELECT *
FROM payment
ORDER BY id DESC
LIMIT 10 OFFSET 4990;

-- cursor 방식 (항상 동일한 실행 계획)
SELECT *
FROM payment
WHERE id < :lastId
ORDER BY id DESC
LIMIT 10;
```

`WHERE id < :lastId`는 PK 인덱스를 그대로 활용하기 때문에, 조회 위치가 어디든 실행 계획이 동일하게 유지됩니다.

클라이언트는 응답에 담긴 커서 정보를 다음 요청의 `cursorKey`(문자열)로 전달하는 방식으로 탐색을 이어갑니다. 서버는 커서 값과 방향(direction)만으로 다음 조회 범위를 결정하기 때문에, 전체 데이터 크기나 현재 페이지 번호를 유지할 필요가 없습니다. 응답의 커서가 정확히 어떤 값을 담는지는 다음 절에서 살펴봅니다.

### 7.3 동작 원리

데이터가 8건(id: 1~8)이고 pageSize=2인 경우를 예로 들겠습니다.

```
전체 데이터: [8, 7, 6, 5, 4, 3, 2, 1]  (id, DESC 기준)
```

**FIRST** - 첫 페이지 (커서 없음):

```sql
SELECT *
FROM payment
ORDER BY id DESC
LIMIT 3;
-- pageSize + 1
-- 결과: [8, 7, 6]
```

- `actualContent`: [8, 7] (pageSize만큼 자름)
- `hasNext`: true (3건 > pageSize 2)
- `nextCursor`: id=7인 Payment 엔티티 (마지막 항목)
- `hasPrev`: false
- `prevCursor`: null

**NEXT** (cursorKey="7"):

```sql
SELECT *
FROM payment
WHERE id < 7
ORDER BY id DESC
LIMIT 3;
-- 결과: [6, 5, 4]
```

- `actualContent`: [6, 5]
- `hasNext`: true
- `nextCursor`: id=5인 Payment 엔티티
- `prevCursor`: id=6인 Payment 엔티티
- `hasPrev`: true

**NEXT** (cursorKey="3") - 마지막 페이지:

```sql
SELECT *
FROM payment
WHERE id < 3
ORDER BY id DESC
LIMIT 3;
-- 결과: [2, 1]
```

- `actualContent`: [2, 1]
- `hasNext`: false (2건 ≤ pageSize 2)
- `nextCursor`: null
- `hasPrev`: true
- `prevCursor`: id=2인 Payment 엔티티

여기서 짚어야 할 것이 하나 있습니다. `nextCursor`는 `Payment` 엔티티(`T?`) 그 자체이지만, 다음 요청의 `CursorRequest.cursorKey`는 문자열(`String?`)입니다. 즉 클라이언트가 응답으로 받은 `nextCursor`를 그대로 다음 요청에 실어 보낼 수는 없습니다. API 응답을 만드는 계층에서 `nextCursor` 엔티티로부터 `id` 값을 꺼내 문자열로 변환한 뒤, 클라이언트가 그 문자열을 다음 요청의 `cursorKey`로 전달하도록 별도로 감싸주어야 합니다.

이 `+1` 조회로 `hasNext`/`hasPrev`를 판별하는 방식은 3장 Slice에서 이미 다룬 바로 그 기법입니다. 커서 기반 조회에서도 동일한 원리가 그대로 재사용됩니다.

### 7.4 구현

```kotlin
fun <T> applyCursorPagination(
    cursorRequest: CursorRequest,
    cursorPath: NumberPath<Long>,
    contentQuery: Function<JPAQueryFactory, JPAQuery<T>>,
): CursorPageResponse<T> {
    val direction = cursorRequest.direction
    val pageSize = cursorRequest.pageSize
    val cursorValue = cursorRequest.cursorKey?.toLong()

    val query = contentQuery.apply(queryFactory)

    when (direction) {
        CursorDirection.FIRST, CursorDirection.LAST -> Unit
        CursorDirection.NEXT -> {
            requireNotNull(cursorValue) { "Cursor key must be provided for NEXT direction" }
            query.where(cursorPath.lt(cursorValue))
        }
        CursorDirection.PREV -> {
            requireNotNull(cursorValue) { "Cursor key must be provided for PREV direction" }
            query.where(cursorPath.gt(cursorValue))
        }
    }

    query.orderBy(
        when {
            direction.isForward -> cursorPath.desc()
            else -> cursorPath.asc()
        }
    )
    query.limit((pageSize + 1).toLong())
    val content = query.fetch()
    return CursorPageResponse(
        content = content,
        direction = direction,
        pageSize = pageSize,
    )
}
```

`contentQuery`에는 기본 조회 쿼리만 전달하면 됩니다. `WHERE` 커서 조건, `ORDER BY`, `LIMIT` 처리는 `applyCursorPagination`이 담당합니다.

실제 Repository에서는 다음과 같이 사용합니다.

```kotlin
fun findByCursor(cursorRequest: CursorRequest): CursorPageResponse<Payment> {
    return applyCursorPagination(
        cursorRequest = cursorRequest,
        cursorPath = qPayment.id,
        contentQuery = { selectFrom(qPayment) }
    )
}
```

### 7.5 `CursorPageResponse`

응답 객체는 팩토리 함수(`invoke`)를 통해 방향별 커서와 플래그를 자동으로 계산합니다.

```kotlin
data class CursorPageResponse<T> private constructor(
    val content: List<T>,
    val hasNext: Boolean,
    val hasPrev: Boolean,
    val nextCursor: T?,
    val prevCursor: T?,
)
```

| direction | content                   | hasNext | hasPrev | nextCursor     | prevCursor      |
|-----------|---------------------------|---------|---------|----------------|-----------------|
| FIRST     | take(pageSize)            | 초과 여부   | false   | 마지막 항목 or null | null            |
| NEXT      | take(pageSize)            | 초과 여부   | true    | 마지막 항목 or null | 첫 번째 항목         |
| PREV      | take(pageSize).reversed() | true    | 초과 여부   | 마지막 항목         | 첫 번째 항목 or null |
| LAST      | take(pageSize).reversed() | false   | 초과 여부   | null           | 첫 번째 항목 or null |

backward(PREV/LAST) 방향은 DB에서 ASC로 조회한 결과를 `reversed()`로 뒤집어 표시 순서를 맞춥니다.

지금까지 구현한 커서 방식이 실제로 offset 방식보다 얼마나 빠른지, 직접 측정해서 확인해보겠습니다.


## 8. 검증: limit/offset vs cursor

### 8.1 100만 건 데이터 셋업

성능 측정을 위해 payment 테이블에 100만 건 데이터를 셋업합니다. **Doubling 방식**(INSERT INTO ... SELECT)으로 초기 1행에서 20번 반복하면 2^20 = 1,048,576건을 빠르게 생성할 수 있습니다.

```sql
-- 1. 기존 데이터 초기화
TRUNCATE TABLE payment;

-- 2. 기준 1행 삽입
INSERT INTO payment (amount, created_at, updated_at)
VALUES (500.00, '2023-01-01 00:00:00', NOW());

-- 3. 아래 INSERT를 20번 반복 실행 (매 실행마다 행 수 2배 증가)
-- 1회: 2건 / 5회: 32건 / 10회: 1,024건 / 15회: 32,768건 / 20회: 1,048,576건
INSERT INTO payment (amount, created_at, updated_at)
SELECT ROUND(RAND() * 1000, 2),
       DATE_ADD('2023-01-01 00:00:00', INTERVAL FLOOR(RAND() * 730 * 24 * 3600) SECOND),
       NOW()
FROM payment;

-- 4. 검증
SELECT COUNT(*)
FROM payment;

SELECT DATE_FORMAT(created_at, '%Y-%m') AS month,
       COUNT(*)                         AS cnt
FROM payment
GROUP BY month
ORDER BY month;
```

### 8.2 쿼리 비교

데이터 셋업(1,048,576건, `created_at` 2023~2024년 분산)을 기준으로 limit/offset 방식과 커서 방식의 SQL을 각각 첫 번째·중간·마지막 페이지로 비교합니다. limit size는 100으로 고정합니다.

**limit / offset 방식**

```sql
-- 첫 번째 페이지 (offset 0)
SELECT *
FROM payment
ORDER BY id DESC
LIMIT 100 OFFSET 0;

-- 약 5,241 페이지 (offset ~524,000, 전체의 약 50% 지점)
SELECT *
FROM payment
ORDER BY id DESC
LIMIT 100 OFFSET 524000;

-- 약 8,241 페이지 (offset ~824,000, 전체의 약 79% 지점)
SELECT *
FROM payment
ORDER BY id DESC
LIMIT 100 OFFSET 824000;

-- 마지막 페이지 (offset ~1,048,476, 전체 끝 지점)
SELECT *
FROM payment
ORDER BY id DESC
LIMIT 100 OFFSET 1048476;
```

**커서 방식**

```sql
-- 첫 번째 페이지 (커서 없음, id 최댓값 1,310,693부터 시작)
SELECT *
FROM payment
ORDER BY id DESC
LIMIT 100;

-- 약 50% 지점 (id 약 655,000 기준)
SELECT *
FROM payment
WHERE id < 655000
ORDER BY id DESC
LIMIT 100;

-- 약 79% 지점 (id 약 281,000 기준)
SELECT *
FROM payment
WHERE id < 281000
ORDER BY id DESC
LIMIT 100;

-- 마지막 페이지 (id 약 100 기준, 끝 지점)
SELECT *
FROM payment
WHERE id < 100
ORDER BY id DESC
LIMIT 100;
```

### 8.3 측정 결과

![Update Performance](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/query-dsl/docs/images/page-Performance.svg)

측정 환경(하드웨어, MySQL 버전, 동시 부하 등)에 따라 절대적인 ms 수치는 달라질 수 있습니다. 그래서 여기서는 절대값 대신, 각 방식의 첫 번째 페이지 소요 시간을 기준(1.0배)으로 놓고 나머지 구간이 얼마나 늘어나는지 상대 배수로 환산해 비교합니다.

| 구간       | limit/offset  | cursor |
|----------|---------------|--------|
| 첫 번째     | 1.0배 (기준)     | 1.0배 (기준) |
| 약 50% 지점 | 2.0배          | 1.0배  |
| 약 79% 지점 | 2.7배          | 1.0배  |
| 마지막      | 3.3배          | 1.0배  |

limit/offset 방식은 뒤로 갈수록 격차가 급격히 벌어져, 첫 페이지 대비 마지막 페이지에서 소요 시간이 **3.3배**로 늘어납니다. 반면 cursor 방식은 조회 위치와 무관하게 항상 1.0배 언저리, 즉 일정한 비용을 유지합니다.

이 차이는 단발성 API 호출에서도 체감되지만, **전체 데이터를 순차적으로 읽어 리포트를 생성하는 배치 처리**에서 더욱 치명적입니다. 예를 들어 100만 건의 결제 데이터를 페이지 단위로 모두 읽어 월별 매출 집계를 산출하는 배치를 생각해보면, limit/offset 방식은 청크가 뒤로 넘어갈수록 쿼리 1건당 소요 시간이 계속 누적되어 증가합니다. 반면 cursor 방식은 청크 위치와 무관하게 매 쿼리가 일정한 비용으로 동작하기 때문에 전체 처리 시간이 선형에 가깝게 유지됩니다.

결국 **"어느 위치를 조회하든 동일한 실행 계획"**이라는 cursor의 특성이, 반복 호출이 누적되는 배치 환경에서 성능 격차를 더욱 크게 만드는 핵심 이유입니다.

커서 기반 페이지네이션은 offset의 한계를 확실하게 해결합니다. 하지만 빠르다고 해서 모든 상황에 적합한 것은 아닙니다. 커서를 적용하기 전에 반드시 확인해야 할 제약과, 애초에 커서가 적합하지 않은 경우를 짚어보겠습니다.


## 9. 커서의 한계와 선택 기준

### 9.1 `contentQuery`에 정렬을 넣지 마세요

`applyCursorPagination`은 내부에서 `cursorPath`를 기준으로 `ORDER BY`를 자동으로 추가합니다. `contentQuery`에 별도의 정렬 조건을 넣으면 충돌이 발생할 수 있습니다.

```kotlin
// Bad - contentQuery에 orderBy 추가
contentQuery = { selectFrom(qPayment).orderBy(qPayment.createdAt.desc()) }

// Good - 기본 조회 쿼리만
contentQuery = { selectFrom(qPayment) }
```

### 9.2 커서가 적합하지 않은 경우

커서 기반 조회는 ID처럼 **순차적이고 유니크한 값을 기준**으로 동작합니다. 다음 경우에는 적합하지 않을 수 있습니다.

**1. GROUP BY가 포함된 쿼리**

GROUP BY 결과에서 ID 기준으로 커서를 잡기 애매합니다. 예를 들어 카테고리별 합계를 조회하는 경우, 집계 결과의 행에는 단일 ID가 존재하지 않아 커서의 연속성이 깨집니다.

```sql
-- ID 커서를 적용하기 어려운 쿼리
SELECT category, SUM(amount)
FROM payment
GROUP BY category;
```

**2. ID와 다른 기준으로 정렬하는 경우**

커서 기반 조회는 ID의 대소 비교로 페이지를 나눕니다. 정렬 기준이 ID와 다른 경우(예: `amount DESC`) 커서가 정렬 순서와 일치하지 않아 데이터 누락이나 중복이 발생할 수 있습니다.

**3. 임의 페이지 이동이 필요한 경우**

"26페이지로 바로 이동" 같은 임의 페이지 이동은 커서 기반으로는 지원하기 어렵습니다. 커서는 이전 페이지의 결과에서 연속적으로 이어지는 구조이기 때문입니다.

정리하면, 커서 기반 조회는 **시계열 데이터를 ID 기준으로 순차 탐색**하는 패턴에 가장 잘 맞습니다. 피드, 알림 목록, 거래 내역처럼 최신순으로 스크롤하는 화면이 대표적인 적합 사례입니다.


## 10. 마무리

지금까지 살펴본 세 가지 페이징 방식을 정리하면 다음과 같습니다.

| 방식                    | Count 쿼리 | 후반부 성능 저하                  | 임의 페이지 이동 | 배치 처리 적합성 | 적합한 화면           |
|-----------------------|--------|---------------------------|-----------|----------|-----------------|
| applyPagination       | O (병렬) | offset 증가 시 최대 3.3배 이상 저하 | O         | X        | 전체 페이지 네비게이션    |
| applySlicePagination  | X      | offset 증가 시 최대 3.3배 이상 저하 | X         | X        | 무한 스크롤, 더보기     |
| applyCursorPagination | X      | 없음 (위치 무관 일정)             | X         | O        | 피드, 알림, 거래 내역, 배치 |

`applyPagination`과 `applySlicePagination`은 offset 기반이므로 뒤로 갈수록 쿼리 비용이 선형 이상으로 증가합니다. 실측 기준으로 첫 페이지 대비 마지막 페이지에서 **3.3배** 이상 느려졌고, 이 차이는 배치처럼 전체 데이터를 반복 순회하는 환경에서 청크마다 누적되어 전체 처리 시간에 직접적인 영향을 줍니다.

`applyCursorPagination`은 `WHERE id < :cursor` 조건으로 PK 인덱스를 직접 활용하기 때문에 조회 위치와 무관하게 실행 계획이 동일하게 유지됩니다. 단건 API뿐 아니라 전체 데이터를 순차적으로 읽어야 하는 배치 리포트에도 적합한 방식입니다.

세 가지 방식 모두 `Querydsl4RepositorySupport`를 통해 공통 함수로 제공됩니다. 임의 페이지 이동이 필요하면 `applyPagination`, 다음/이전 탐색만 필요하면 `applySlicePagination`, 대용량 순차 조회라면 `applyCursorPagination`을 선택하면 됩니다.
