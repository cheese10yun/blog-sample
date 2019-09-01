# Transactional 테스트

## Case 1 :

```java
@GetMapping
  public A create(){
      final A a = aService.aCreate();
      bService.bCreate(a);
      return a;
  }

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AService {

  public A aCreate(){
    final A a = aRepository.save(new A());
    log.error("A -> currentTransactionName : {}", TransactionSynchronizationManager.getCurrentTransactionName());
    // A -> currentTransactionName : com.gradle.sample.a.AService.aCreate
    return a;
  }

}

@Service
//@Transactional // 트랜잭션 주석  처리되있음
@RequiredArgsConstructor
@Slf4j
public class BService {
    public A bCreate(final A a) {
        a.setName("1111");
        // Update Query 발생
        log.error("B => currentTransactionName : {}", TransactionSynchronizationManager.getCurrentTransactionName());
        //  B => currentTransactionName : null
        // @Transactional 주석이 아닐 경우에는 B => currentTransactionName : com.gradle.sample.b.BService.bCreate

        bRepository.save(new B());
        return a;
    }
}
```
* **컨트롤러 메서드에서 `aCreate()`, `b.Create()` 메서드 각각 호출 한 경우**
* `B => currentTransactionName : null` : Transa]ctional 어노테이션이 없으면 트랜잭션은 전위 되지 않는다. ?
* `a.setName("1111");` :  BService 에서 update 쿼리가 발생한다 (트랜잭션이 없는 상태)
* `bRepository.save(new B());` : insert into 발생 (트랜잭션이 없는 상태)
* `@Transactional` 없어도 데이터베이스에 반영되는 정보는 다르지 않음


```java
// 다른 코드들은 위와 동일
@Service
//@Transactional // 트랜잭션 주석  처리되있음
@RequiredArgsConstructor
@Slf4j
public class BService {
    public A bCreate(final A a) {
        a.setName("1111");
        // Update Query 발생
        log.error("B => currentTransactionName : {}", TransactionSynchronizationManager.getCurrentTransactionName());
        //  B => currentTransactionName : null
        // @Transactional 주석이 아닐 경우에는 B => currentTransactionName : com.gradle.sample.b.BService.bCreate

        bRepository.save(new B());
        if(true){
            throw new RuntimeException();
        }
        return a;
    }
}
```

* A는 Insert 진행됨
* `a.setName("1111");` : **update query 발생**
* `bRepository.save(new B());` : `throw new RuntimeException();` 때문에 insert 되지 않음 (`a.setName("1111");`는 update query 발생함)
* `@Transactional` 어노테이션이 있는 경우 `a.setName("1111");`의 update query 발생하지 않음
* **`@Transactional`가 있는 경우 `bCreate(final A a){...}` 메서드의 트랜잭션 범위가 같이 그룹화되기 때문에 RuntimeException(); 발생하면 Rollback이 징행된다. 반면 `@Transcation`이 없는 경우 트랜잭션 범위가 같이 그룹화되지 않기 때문에 `a.setName("1111");` 의 commit과, `bRepository.save(new B());` commit이 다르기 떄문으로 판단, `bRepository.save(new B());`의 `@Transcation`이 발동하면 `a.setName("1111");`의 트랜잭션이 Commit되는 것으로 보임**


## Case 2 

```java
@GetMapping
  public A create(){
      final A a = aService.aCreate();
      return a;
  }

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AService {

    private final ARepository aRepository;
    private final BService bService;

    public A aCreate() {
        final A a = aRepository.save(new A());
        log.error("A -> currentTransactionName : {}", 
        TransactionSynchronizationManager.getCurrentTransactionName());
        // A -> currentTransactionName : com.gradle.sample.a.AService.aCreate
        bService.bCreate(a);
        return a;
    }

}
@Service
//@Transactional
@RequiredArgsConstructor
@Slf4j
public class BService {

    private final BRepository bRepository;

    public A bCreate(final A a) {
        a.setName("1111");
        log.error("B => currentTransactionName : {}", TransactionSynchronizationManager.getCurrentTransactionName());
        // B => currentTransactionName : com.gradle.sample.a.AService.aCreate
        // @Transactional 트랜잭션이 있는 경우, B => currentTransactionName : com.gradle.sample.a.AService.aCreate
        bRepository.save(new B());
        return a;
    }
}
```

* **컨틀로러 a.create -> a.save(), b.Create() -> b.save();**의 순서를 가짐
* `@Trnasaction`어노테이션이 없기 때문에 `AService`의 트랜잭션이 그대로 전이됨
* `@Transactional`어노테이션이 있어도 `AService`의 트랜잭션이 그대로 전이 되기 떄문에 `currentTransactionName` 동일 하다.
* a Insert, a Update, B Insert 그대로 동작한다.


```java

@Service
//@Transactional
@RequiredArgsConstructor
@Slf4j
public class BService {

    private final BRepository bRepository;

    public A bCreate(final A a) {
        a.setName("1111");
        log.error("B => currentTransactionName : {}", TransactionSynchronizationManager.getCurrentTransactionName());
        // B => currentTransactionName : com.gradle.sample.a.AService.aCreate
        // @Transactional 트랜잭션이 있는 경우, B => currentTransactionName : com.gradle.sample.a.AService.aCreate

        if(true){
            throw new RuntimeException();
        }
        bRepository.save(new B());
        return a;
    }
}
```
* `AService`에서 트랜잭션이 전이 되기 때문에 Rollback도 a, b 같이 진행된다. (`@Transactional`이 있든 없든 `AService`에서 전이 되기 때문에 결과는 같다.)
* 트랜잭션 전이를 막으려면 메서드 호출을 각각 해야 될거 같다. 

```java 

// 트랜잭션이 독립적으로 가져감, b exception시 a 롤백안됨
public void createAll() {
    a.create();
    b.create();
}

// 트랜잭션이 전이 됨, b.create 롤백시 a.create 롤백 즉, createAll() 전체 롤백
public void createAll() {
    a.create();
}
 
// AService
public void create(){
    b.create();
}
```
* JPA에서는 `propagation` 옵션이 제대로 동작하지 않는거 같다???.