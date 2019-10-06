# 무식 하게 배우는 JPA

## @Transactional 어노테이션 위치에 따른 차이
```kotlin
@RestController
@RequestMapping("/members")
class MemberApi(
        private var memberRepository: MemberRepository) {

    @PostMapping
    fun createMember(@RequestBody dto: MemberSignUpRequest): Member {
        return memberRepository.save(dto.toEntity())
    }

//    @Transactional
    fun getMembers(page: Pageable): List<Member> {
        println("Transaction Start name : ${TransactionSynchronizationManager.getCurrentTransactionName()}")
        val members = memberRepository.findAll()
        val member = members[0]
        member.updateName(name = UUID.randomUUID().toString())
        println(member.name)

        println("Transaction End name : ${TransactionSynchronizationManager.getCurrentTransactionName()}")
        return members
    }
}
```

### @Transactional이 없는 경우

```sql
Transaction Start name : null
2019-10-06 03:51:03.053  INFO 4066 --- [nio-8890-exec-1] o.h.h.i.QueryTranslatorFactoryInitiator  : HHH000397: Using ASTQueryTranslatorFactory
Hibernate: 
    select
        member0_.id as id1_0_,
        member0_.email as email2_0_,
        member0_.name as name3_0_ 
    from
        member member0_
aca1a8ee-4f19-45f4-8d38-312322ee9693
Transaction End name : null
```

### @Transactional이 없는 경우

```sql
Transaction Start name : com.example.springkotlin.domain.member.api.MemberApi.getMembers
2019-10-06 03:52:47.785  INFO 4156 --- [nio-8890-exec-1] o.h.h.i.QueryTranslatorFactoryInitiator  : HHH000397: Using ASTQueryTranslatorFactory
Hibernate: 
    select
        member0_.id as id1_0_,
        member0_.email as email2_0_,
        member0_.name as name3_0_ 
    from
        member member0_
f14c2167-89a1-406d-b567-9baca5e13927
Transaction End name : com.example.springkotlin.domain.member.api.MemberApi.getMembers
Hibernate: 
    update
        member 
    set
        email=?,
        name=? 
    where
        id=?
``` 

### 차이점
결과는 동일 하다. **update 쿼리 유무가 차이가 있다.** 

:exclamation: **뇌피셜 이므로 정확하지는 않음**

우선 `@Transaction` 어노테이션이 없는 경우는 **트랜잭션 시작 시점은 SimpleJpaRepository 에서 시작된다.** JpaRepository를 타고 올라가면 `SimpleJpaRepository` 클래스 까지 올라가게 되며 그리고 **JpaRepository 인터페이스로 find, delete, create 등이 가능했던 이유는 SimpleJpaRepository에서 @Transaction을 선언했기 때문이다.**

```java
@Repository
@Transactional(readOnly = true)
public class SimpleJpaRepository<T, ID> implements JpaRepositoryImplementation<T, ID> {
    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.jpa.repository.JpaRepository#findAll()
	 */
	public List<T> findAll() {
		return getQuery(null, Sort.unsorted()).getResultList();
	}

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.CrudRepository#delete(java.lang.Object)
	 */
	@Transactional
	public void delete(T entity) {

		Assert.notNull(entity, "Entity must not be null!");

		if (entityInformation.isNew(entity)) {
			return;
		}

		T existing = em.find(entityInformation.getJavaType(), entityInformation.getId(entity));
		// if the entity to be deleted doesn't exist, delete is a NOOP
		if (existing == null) {
			return;
		}
		em.remove(em.contains(entity) ? entity : em.merge(entity));
	}

}
```

**그렇다면 트랜잭션이 `SimpleJpaRepository` 클래스에서 시작되면 `memberRepository.findAll()` 메서드 종료가 되면 트랜잭션이 종료된다.(Flush, Commit을 완료한다.)** 그 다음에 `member.updateName(..)` 메서드에는 트랜잭션이 없다. 하지만 **영속성 컨텍스트는 살아 있으니 해당 커밋은 가능 하다.** 이런 상태에서는 `show-sql: true`에 log가 찍히지 않는 것을 보인다.

반면 컨트롤러 코드에 `@Transactional` 어노테이션이 있는 경우는 트랜젝션 이름이 `MemberApi.getMembers`으로 동일한 것을 확인 할 수 있다. 그렇다는 것은 **memberRepository.findAll(), member.updateName(..)가 동일한 트랜잭션에서 진행된다는 것이고 트랜잭션이 있는 경우 `show-sql: true`의 log가 찍히는 것으로 판단된다.**

그렇다면 트랜잭션 범위와, 영속성 컨텍스트의 범위는 다르다는 것으로 보인다. 그렇다면 이 둘이 어떻게 다른지 알아보자.


## 벌크 연산


### 벌크 연산 안됨
```kotlin
@RestController
@RequestMapping("/members")
class MemberApi(
private var memberRepository: MemberRepository) {

    @GetMapping
    @Transactional
    fun getMembers(page: Pageable): List<Member> {
        val members = memberRepository.findAll()

        for(member in members){
            member.updateName("none_name")
        }
        return members
    }
}
```
일반적으로 JPA 기반으로 UPDATE 작업 수행 하는 코드이다. 영속성컨텍스트에 데이터를 가져와서`member`를 수정하는 방법이다.

```SQL
2019-10-06 04:18:57.291  INFO 4988 --- [(3)-192.168.0.3] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2019-10-06 04:18:57.291  INFO 4988 --- [(3)-192.168.0.3] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2019-10-06 04:18:57.311  INFO 4988 --- [(3)-192.168.0.3] o.s.web.servlet.DispatcherServlet        : Completed initialization in 20 ms
2019-10-06 04:19:02.081  INFO 4988 --- [nio-8890-exec-1] o.h.h.i.QueryTranslatorFactoryInitiator  : HHH000397: Using ASTQueryTranslatorFactory
Hibernate: 
    select
        member0_.id as id1_0_,
        member0_.email as email2_0_,
        member0_.name as name3_0_ 
    from
        member member0_
Hibernate: 
    update
        member 
    set
        email=?,
        name=? 
    where
        id=?
Hibernate: 
    update
        member 
    set
        email=?,
        name=? 
    where
        id=?
Hibernate: 
    update
        member 
    set
        email=?,
        name=? 
    where
        id=?
Hibernate: 
    update
        member 
    set
        email=?,
        name=? 
    where
        id=?
Hibernate: 
    update
        member 
    set
        email=?,
        name=? 
    where
        id=?
```
UPDATE 로그를 보면 영속성 컨텍스트에서 하나씩 꺼내와서 UPDATE 쿼리를 진행하고 있다.

### 벌크 연산 됨

```kotlin
@RestController
@RequestMapping("/members")
class MemberApi(
private var memberRepository: MemberRepository) {
    @GetMapping
    @Transactional
    fun getMembers(page: Pageable): List<Member> {
        val ids = listOf(1L, 2L, 3L, 4L, 5L)
        val count = memberRepository.updateName(ids)
        println("update count : $count")
        return memberRepository.findAll()
    }
}
```
* **조회를 하지 않아도(영속성 컨텍스트를 담아 오지 않아도) 벌크 수정이 가능하다.**


```kotlin
interface MemberRepository : JpaRepository<Member, Long> {

    @Modifying
    @Query(
            "update Member m set m.name = 'none_name' " +
                    "where m.id in :ids "
    )
    fun updateName(@Param("ids") ids: List<Long>) : Int

}
```
**스프링 데이터 JPA에서 벌크, 수정, 삭제 쿼리는 `@Modifying` 어노테이션을 사용하면된다.** 벌크성 쿼리를 실행하고 나서 영속성 컨텍스트를 초기화하고 싶으면 `@Modifying(clearAutomatically = true)` 옵션을 true로 지정하면 된다. 기본은 false이다. [영속성 컨텍스트를 초기화해야하는 이유](https://github.com/cheese10yun/TIL/blob/master/Spring/jpa/jpa.md#벌크-연산-주의점) 


```sql
Hibernate: 
    update
        member 
    set
        name='none_name' 
    where
        id in (
            ? , ? , ? , ? , ?
        )
update count : 5
Hibernate: 
    select
        member0_.id as id1_0_,
        member0_.email as email2_0_,
        member0_.name as name3_0_ 
    from
        member member0_
```
출력된 log를 보면 알수 있듯이 조회없이(영속성 컨텍스트없이) 수정이 가능하다.히 `where id in (...)`을 이용해서 벌크성으로 객체를 수정하고 있다.