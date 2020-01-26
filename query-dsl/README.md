# Query DSl 실전! 강의 학습 정리
> [실전! Querydsl](https://www.inflearn.com/course/Querydsl-%EC%8B%A4%EC%A0%84#)을 학습 내용 정리입니다.

## Query DSL With Kotlin Setting

### build.gradle.kts

```groovy
plugins {
    kotlin("kapt") version "1.3.61" // query dsl plugin 푸가
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.querydsl:querydsl-jpa") // query dsl 의존성 추가
    runtimeOnly("com.h2database:h2")
    annotationProcessor(group = "com.querydsl", name = "querydsl-apt", classifier = "jpa") // query dsl 의존성 추가
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    kapt("com.querydsl:querydsl-apt:4.2.1:jpa") // query dsl 의존성 추가
}

```
### Setting Test

```kotlin
@Entity
@Table(name = "heelo")
data class Hello(
        @Column(name = "name", nullable = false)
        val name: String

) : EntityAuditing() 
```
Entity 정의

```kotlin
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
class QueryDslApplicationTests(
        private val em: EntityManager
) {

    @Test
    internal fun `querydsl setting test`() {
        val hello = Hello("yun")
        em.persist(hello)

        val query = JPAQueryFactory(em)

        val qHello = QHello.hello

        val findHello = query
                .selectFrom(qHello)
                .where(qHello.name.eq("yun"))
                .fetchOne()!!

        then(findHello.id).isNotNull()
        then(findHello.name).isEqualTo("yun")
    }
}
```
query dsl 기반으로 select test

```
2020-01-26 01:42:11.185  INFO 53210 --- [    Test worker] o.s.t.c.transaction.TransactionContext   : Began transaction (1) for test context [DefaultTestContext@262bae0d testClass = QueryDslApplicationTests, testInstance = com.example.querydsl.QueryDslApplicationTests@3845bcdd, testMethod = querydsl setting test$query_dsl@QueryDslApplicationTests, testException = [null], mergedContextConfiguration = [WebMergedContextConfiguration@23acc0c7 testClass = QueryDslApplicationTests, locations = '{}', classes = '{class com.example.querydsl.QueryDslApplication}', contextInitializerClasses = '[]', activeProfiles = '{}', propertySourceLocations = '{}', propertySourceProperties = '{org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true}', contextCustomizers = set[org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@0, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizerFactory$Customizer@42eee529, org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@230b83e5, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@1ada8a06, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@0, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@6f3e4b24], resourceBasePath = 'src/main/webapp', contextLoader = 'org.springframework.boot.test.context.SpringBootContextLoader', parent = [null]], attributes = map['org.springframework.test.context.web.ServletTestExecutionListener.activateListener' -> true, 'org.springframework.test.context.web.ServletTestExecutionListener.populatedRequestContextHolder' -> true, 'org.springframework.test.context.web.ServletTestExecutionListener.resetRequestContextHolder' -> true]]; transaction manager [org.springframework.orm.jpa.JpaTransactionManager@38217803]; rollback [true]
2020-01-26 01:42:11.283  INFO 53210 --- [    Test worker] p6spy                                    : #1579970531283 | took 1ms | statement | connection 5| url jdbc:h2:mem:testdb
insert into heelo (id, created_at, updated_at, name) values (null, ?, ?, ?)
insert into heelo (id, created_at, updated_at, name) values (null, '2020-01-26T01:42:11.269+0900', '2020-01-26T01:42:11.269+0900', 'yun');
2020-01-26 01:42:11.565  INFO 53210 --- [    Test worker] p6spy                                    : #1579970531565 | took 3ms | statement | connection 5| url jdbc:h2:mem:testdb
select hello0_.id as id1_0_, hello0_.created_at as created_2_0_, hello0_.updated_at as updated_3_0_, hello0_.name as name4_0_ from heelo hello0_ where hello0_.name=?
select hello0_.id as id1_0_, hello0_.created_at as created_2_0_, hello0_.updated_at as updated_3_0_, hello0_.name as name4_0_ from heelo hello0_ where hello0_.name='yun';
2020-01-26 01:42:11.641  INFO 53210 --- [    Test worker] p6spy                                    : #1579970531641 | took 0ms | rollback | connection 5| url jdbc:h2:mem:testdb
```
query 정상동작 확인

## 검색 조건 쿼리

```kotlin

@Test
internal fun `query dsl search`() {
    //@formatter:off
    val member = query
            .selectFrom(qMember)
            .where(
                    qMember.username.eq("member1")
                    .and(qMember.age.eq(10))
            )
            .fetchOne()!!
    //@formatter:on

    then(member.username).isEqualTo("member1")
    then(member.age).isEqualTo(10)
}

@Test
internal fun `query dsl and 생략 가능`() {
    val member = query
            .selectFrom(qMember)
            .where(
                    qMember.username.eq("member1"),
                    qMember.age.eq(10)
            )
            .fetchOne()!!

    then(member.username).isEqualTo("member1")
    then(member.age).isEqualTo(10)
}
```
`and` 조건인 경우 생략이 가능하며 `,`으로 간략하게 가능하다.
 
 
 ## 결과 조회
 ```kotlin
@Test
internal fun `query dsl fetch type`() {

// 단건 조회
val member = query
        .selectFrom(qMember)
        .where(qMember.username.eq("member1"))
        .fetchOne()

// list 조회
val members = query
        .selectFrom(qMember)
        .fetch()

// 처음 한건 조회
val firstMember = query
        .selectFrom(qMember)
        .fetchFirst()

// 페이징 사용
val pagingMembers = query
        .selectFrom(qMember)
        .fetchResults()

// count 쿼리
val count = query
        .selectFrom(qMember)
        .fetchCount()

}
 ```
* fetch() : 리스트 조회, 데이터 없으면 빈 리스트 반환
* fetchOne() : 단 건 조회, 결과가 없으면 : null 결과가 둘 이상이면 : com.querydsl.core.NonUniqueResultException 
* fetchFirst() : limit(1).fetchOne()
* fetchResults() : 페이징 정보 포함, total count 쿼리 추가 실행 
* fetchCount() : count 쿼리로 변경해서 count 수 조회

## 정렬
```kotlin
@Test
internal fun `query dsl sort`() {
    val members = query
            .selectFrom(qMember)
            .orderBy(qMember.age.desc(), qMember.username.asc().nullsLast())
            .fetch()
}
```
* desc() , asc() : 일반 정렬
* nullsLast() , nullsFirst() : null 데이터 순서 부여

## 페이징
```kotlin
@Test
internal fun `query dsl paging fetch 조회 건수 제한`() {
    val members = query
            .selectFrom(qMember)
            .orderBy(qMember.username.desc())
            .offset(1)
            .limit(2)
            .fetch()
}

@Test
internal fun `query dsl paging fetch results 전체 조회 수가 필요`() {
    val paging = query
            .selectFrom(qMember)
            .orderBy(qMember.username.desc())
            .offset(1)
            .limit(2)
            .fetchResults()


    then(paging.total).isEqualTo(4)
    then(paging.limit).isEqualTo(2)
    then(paging.offset).isEqualTo(1)
    then(paging.results.size).isEqualTo(2)
}
```

주의: count 쿼리가 실행되니 성능상 주의!
> 참고: 실무에서 페이징 쿼리를 작성할 때, 데이터를 조회하는 쿼리는 여러 테이블을 조인해야 하지만, count 쿼리는 조인이 필요 없는 경우도 있다. 그런데 이렇게 자동화된 count 쿼리는 원본 쿼리와 같이 모두 조인을 해버리기 때문에 성능이 안나올 수 있다. count 쿼리에 조인이 필요없는 성능 최적화가 필요하다면, count 전용 쿼리를 별도로 작성해야 한다.

## 집합
```kotlin
@Test
internal fun `query dsl aggregation set`() {

    val result = query
            .select(
                    qMember.count(),
                    qMember.age.sum(),
                    qMember.age.avg(),
                    qMember.age.max(),
                    qMember.age.min()
            )
            .from(qMember)
            .fetch()


    val tuple = result[0]

    then(tuple.get(qMember.count())).isEqualTo(4)
    then(tuple.get(qMember.age.sum())).isEqualTo(100)
    then(tuple.get(qMember.age.avg())).isEqualByComparingTo(25.0)
    then(tuple.get(qMember.age.max())).isEqualTo(40)
    then(tuple.get(qMember.age.min())).isEqualTo(10)
}

@Test
internal fun `query dsl group by`() {

    val result = query
            .select(qTeam.name, qMember.age.avg())
            .from(qMember)
            .join(qMember.team, qTeam)
            .groupBy(qTeam.name)
            .fetch()

    val teamA = result[0]
    val teamB = result[1]

    then(teamA.get(qTeam.name)).isEqualTo("teamA")
    then(teamA.get(qMember.age.avg())).isEqualTo(15.0)

    then(teamB.get(qTeam.name)).isEqualTo("teamB")
    then(teamB.get(qMember.age.avg())).isEqualTo(35.0)
}
```
* JPQL이 제공하는 모든 집합 함수를 제공한다. 
* tuple은 프로젝션과 결과반환에서 설명한다.


## 조인

### 일반 조인
```kotlin
@Test
internal fun `query dsl join`() {

    val members = query
            .selectFrom(qMember)
            .join(qMember.team, qTeam)
            .where(qTeam.name.eq("teamA"))
            .fetch()

    then(members).anySatisfy {
        then(it.username).isIn("member1", "member2")
        then(it.team!!.name).isEqualTo("teamA")
    }

}
```

```sql
select
    member0_.id as id1_1_,
    member0_.created_at as created_2_1_,
    member0_.updated_at as updated_3_1_,
    member0_.age as age4_1_,
    member0_.team_id as team_id6_1_,
    member0_.username as username5_1_ 
from
    member member0_ 
inner join
    team team1_ 
        on member0_.team_id=team1_.id 
where
    team1_.name=?
```

* join() , innerJoin() : 내부 조인(inner join) 
* leftJoin() : left 외부 조인(left outer join) 
* rightJoin() : rigth 외부 조인(rigth outer join)
* JPQL의 on과 성능 최적화를 위한 fetch 조인 제공 -> 다음 on 절에서 설명

### 세타 조인 
**연관관계가 없는 필드로 조인**
```kotlin
@Test
internal fun `query dsl seta join`() {
    val members = query
            .select(qMember)
            .from(qMember, qTeam)
            .where(qMember.username.eq(qTeam.name))
            .fetch()
}
```

```sql
select
    member0_.id as id1_1_,
    member0_.created_at as created_2_1_,
    member0_.updated_at as updated_3_1_,
    member0_.age as age4_1_,
    member0_.team_id as team_id6_1_,
    member0_.username as username5_1_ 
from
    member member0_ cross 
join
    team team1_ 
where
    member0_.username=team1_.name
```
* from 절에 여러 엔티티를 선택해서 세타 조인
* 외부조인불가능 다음에설명할조인on을 사용하면외부조인가능
* SQL cross join이 진행된것을 확인

### 조인 on : 조인 대상 필터링
* 조인대상필터링
* 연관관계없는엔티티외부조인

```kotlin
@Test
internal fun `query dsl join on`() {
    val members = query
            .select(qMember)
            .from(qMember)
            .leftJoin(qMember.team, qTeam).on(qTeam.name.eq("teamA"))
            .fetch()

    then(members).anySatisfy {
        then(it.team!!.name).isEqualTo("teamA")
    }
}
```

```sql
select
    member0_.id as id1_1_,
    member0_.created_at as created_2_1_,
    member0_.updated_at as updated_3_1_,
    member0_.age as age4_1_,
    member0_.team_id as team_id6_1_,
    member0_.username as username5_1_ 
from
    member member0_ 
left outer join
    team team1_ 
        on member0_.team_id=team1_.id and (team1_.name=?)
```
참고: on 절을 활용해 조인 대상을 필터링 할 때, 외부조인이 아니라 내부조인(inner join)을 사용하면, where 절에서 필터링 하는 것과 기능이 동일하다. 따라서 on 절을 활용한 조인 대상 필터링을 사용할 때, 내부조인 이면 익숙한 where 절로 해결하고, 정말 외부조인이 필요한 경우에만 이 기능을 사용하자.

## 조인 on : 연관관계 없는 엔티티 외부 조인

```kotlin
@Test
internal fun `query dsl ro relation`() {
    val result = query
            .select(qMember, qTeam)
            .from(qMember)
            .leftJoin(qTeam).on(qMember.username.eq(qTeam.name))
            .fetch()


    for (tuple in result) {
        println("tuple : ${tuple}")
    }
}
```

```sql
select
    member0_.id as id1_1_0_,
    team1_.id as id1_2_1_,
    member0_.created_at as created_2_1_0_,
    member0_.updated_at as updated_3_1_0_,
    member0_.age as age4_1_0_,
    member0_.team_id as team_id6_1_0_,
    member0_.username as username5_1_0_,
    team1_.created_at as created_2_2_1_,
    team1_.updated_at as updated_3_2_1_,
    team1_.name as name4_2_1_ 
from
    member member0_ 
left outer join
    team team1_ 
        on (
            member0_.username=team1_.name
        )
// tuple : [Member(username=member1, age=10, team=Team(name=teamA)), null]
// tuple : [Member(username=member2, age=20, team=Team(name=teamA)), null]
// tuple : [Member(username=member3, age=30, team=Team(name=teamB)), null]
// tuple : [Member(username=member4, age=40, team=Team(name=teamB)), null]
```

* 하이버네이트 5.1부터 on 을 사용해서 서로 관계가 없는 필드로 외부 조인하는 기능이 추가되었다. 물론 내 부 조인도 가능하다.
* 주의! 문법을 잘 봐야 한다. leftJoin() 부분에 일반 조인과 다르게 엔티티 하나만 들어간다.
  * 일반조인: leftJoin(member.team, team) 
  * on조인: from(member).leftJoin(team).on(xxx)
  
### 조인 - fetch join
* 페치 조인은 SQL에서 제공하는 기능은 아니다. SQL조인을 활용해서 연관된 엔티티를 SQL 한번에 조회하 는 기능이다. 주로 성능 최적화에 사용하는 방법이다.
```kotlin
@Test
internal fun `query dsl fetch join`() {
    val member = query
            .selectFrom(qMember)
            .join(qMember.team, qTeam).fetchJoin()
            .where(qMember.username.eq("member1"))
            .fetchOne()!!

}
```

```sql
select
    member0_.id as id1_1_0_,
    team1_.id as id1_2_1_,
    member0_.created_at as created_2_1_0_,
    member0_.updated_at as updated_3_1_0_,
    member0_.age as age4_1_0_,
    member0_.team_id as team_id6_1_0_,
    member0_.username as username5_1_0_,
    team1_.created_at as created_2_2_1_,
    team1_.updated_at as updated_3_2_1_,
    team1_.name as name4_2_1_ 
from
    member member0_ 
inner join
    team team1_ 
        on member0_.team_id=team1_.id 
where
    member0_.username=?
```

```kotlin
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "team_id")
var team: Team? = null
```
실제 페치 전략이 Lazy이더라도, 즉시 로딩으로 한 번에 조회 할 수 있다.

## 서브 쿼리

```kotlin
@Test
internal fun `query dsl sub query 나이가 가장 큰 값`() {
    val qMemberSub = QMember("memberSub")
    val member = query
            .selectFrom(qMember)
            .where(qMember.age.eq(
                    JPAExpressions
                            .select(qMemberSub.age.max())
                            .from(qMemberSub)
            ))
            .fetchOne()!!

    then(member.age).isEqualTo(40)
}
```

```sql
select
    member0_.id as id1_1_,
    member0_.created_at as created_2_1_,
    member0_.updated_at as updated_3_1_,
    member0_.age as age4_1_,
    member0_.team_id as team_id6_1_,
    member0_.username as username5_1_ 
from
    member member0_ 
where
    member0_.age=(
        select
            max(member1_.age) 
        from
            member member1_
    )
```

```kotlin
@Test
internal fun `query dsl sub query 평균 보다 큰 나이`() {
    val qMemberSub = QMember("memberSub")
    val members = query
            .selectFrom(qMember)
            .where(qMember.age.goe(
                    JPAExpressions
                            .select(qMemberSub.age.avg())
                            .from(qMemberSub)
            ))
            .fetch()!!

    then(members).anySatisfy {
        then(it.age).isIn(30, 40)
    }
}
```
```kotlin
@Test
internal fun `query dsl sub query 평균 보다 큰 나이`() {
    val qMemberSub = QMember("memberSub")
    val members = query
            .selectFrom(qMember)
            .where(qMember.age.goe(
                    JPAExpressions
                            .select(qMemberSub.age.avg())
                            .from(qMemberSub)
            ))
            .fetch()!!

    then(members).anySatisfy {
        then(it.age).isIn(30, 40)
    }
}
```

```sql
select
    member0_.id as id1_1_,
    member0_.created_at as created_2_1_,
    member0_.updated_at as updated_3_1_,
    member0_.age as age4_1_,
    member0_.team_id as team_id6_1_,
    member0_.username as username5_1_ 
from
    member member0_ 
where
    member0_.age>=(
        select
            avg(cast(member1_.age as double)) 
        from
            member member1_
    )
```

### from 절의 서브쿼리 한계
**JPA JPQL 서브쿼리의 한계점으로 from 절의 서브쿼리(인라인 뷰)는 지원하지 않는다. 당연히 Querydsl 도 지원하지 않는다. 하이버네이트 구현체를 사용하면 select 절의 서브쿼리는 지원한다. Querydsl도 하 이버네이트 구현체를 사용하면 select 절의 서브쿼리를 지원한다.**

### from 절의 서브쿼리 해결방안
1. 서브쿼리를 join으로 변경한다. (가능한 상황도 있고, 불가능한 상황도 있다.)
2. 애플리케이션에서 쿼리를 2번 분리해서 실행한다.
3. nativeSQL을 사용한다.

## Case 문
```kotlin
@Test
internal fun `query dsl basic case`() {
    val results = query
            .select(qMember.age
                    .`when`(10).then("열살")
                    .`when`(20).then("스무살")
                    .otherwise("이타"))
            .from(qMember)
            .fetch()

    for (str in results){
        println(str)
    }
}
```
```sql
select
    case 
        when member0_.age=? then ? 
        when member0_.age=? then ? 
        else '이타' 
    end as col_0_0_ 
from
    member member0_
```
