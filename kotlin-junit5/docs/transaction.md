## 트랜잭션 단위

```kotlin
@RestController
@RequestMapping("/sample")
class SampleController(
        private val aRepository: ARepository,
        private val bRepository: BRepository
) {
    @GetMapping
    fun transaction() {
        aRepository.save(A("A"))
        bRepository.save(B("B")) // 여기에서 exception 발생시 위 save는 Rollback 되지 않는다.
    }
}
```
`bRepository.save(B("B"))` 에서 예외가 발생했을 경우 트랜잭션 단위는 클라스 단위로 지정되기 때문에 `aRepository.save(A("A"))` Rollback 되지 않습니다.

![](https://github.com/cheese10yun/TIL/blob/master/assets/transacion-proxy.png?raw=true)

스프링 부트에서는 기본적으로 CGLIB Proxy 방식으로 트랜잭션을 처리합니다. (이전에는  Dynamic Proxy 패턴) Proxy 패턴을 사용하기 때문에 클래스 단위로 트랜잭션이 시작되고 묶이게 됩니다.

![](https://github.com/cheese10yun/TIL/blob/master/assets/transacion-group.png?raw=true)

트랜잭션 단위를 보면 위와 같습니다. Proxy 방식이기 때문에 클래스 단위로 묶이게 됩니다. 그렇가면 JPA Repository는 interface인데 어떻게 트랜잭션이 발생하게 되는지 의문을 갖는 분들도 있습니다.


```kotlin
interface ARepository : JpaRepository<A, Long> {
}
```

![](https://github.com/cheese10yun/TIL/blob/master/assets/jpa-simple-repository.png?raw=true)

`JpaRepository` 인터페이스의 세부 구현체가 `SimpleJpaRepository`가 있습니다. 세부 코드를 보면 아래와 같습니다.

![](https://github.com/cheese10yun/TIL/blob/master/assets/jpa-simple-repository-save.png?raw=true)

`save()` 메서드에 `@Transactional`이 있고 여기에서 트랜잭션이 동작하게 됩니다. 다시 본론으로 돌아와서 아래 그림처럼 트랜잭션 그룹이 다르기 때문에 commit, rollback 기준도 클래스를 기반으로 나뉘게 됩니다.

![](https://github.com/cheese10yun/TIL/blob/master/assets/transacion-group.png?raw=true)


그렇다면 위 두 개의 트랜잭션을 묶을려면 어떻게 해야할까요? 답은 간단합니다 이 두개를 묶는 트랜잭션을 설정하면 됩니다.

```kotlin
@RestController
@RequestMapping("/sample")
class SampleController(
        private val aRepository: ARepository,
        private val bRepository: BRepository
) {
    @GetMapping
    @Transactional // aRepository, bRepository 트랜잭션을 묶는다.
    fun transaction() {
        aRepository.save(A("A"))
        bRepository.save(B("B")) // 여기에서 exception 발생시 위 save는 Rollback 된다.
    }
}
```
`@Transactional`을 추가하게 되면 `aRepository, bRepository` 트랜잭션이 같이 묶이게 됩니다.(물론 컨트롤러에 추가하는 것보다 별도의 트랜잭션를 담당하는 Service 영역을 추가하는 것이 바람직합니다.)

![](https://github.com/cheese10yun/TIL/blob/master/assets/transacion-group-2.png?raw=true)

이렇게 되면 두 트랜잭션이 같이 묶이게 되고 `bRepository.save(B("B"))` 에서 exception이 발생하게 되면 `bRepository.save(B("B"))`, `aRepository.save(A("A"))`이 모두 rollback이 진행됩니다.