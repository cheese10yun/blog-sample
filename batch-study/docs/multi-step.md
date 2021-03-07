# 멀티 스레드로 여러 개의 Step 실행하기

> 해당 글은 [처음으로 배우는 스프링 부트 2](http://www.hanbit.co.kr/store/books/look.php?p_code=B4458049183)을 보고 학습한 내용을 정리한 글입니다.

여러 Step을 동시에 실행하는 경우 스프링 부트 배치는 멀티 스레드로 Step을 실행하는 여러 전략을 제공합니다.

* TaskExecutor를 사용해 여러 Step 동작시키기
* 여러 개의 Flow 실행시키기
* 파티셔닝을 사용한 병렬 프로그래밍

## TaskExecutor를 이용한 여러 Step 동작시키기

Task는 Runnable 인터페이스를 구현해 각각의 스레드가 독립저긍로 실행되도록 작업을 할당하는 객체입니다. 스프링에서는 이러한 Task를 실행하는 객체를 TaskExecutor 인터페이스를 통해 구련하도록 정의했습니다. TaskExecutor
인터페이스를 구현한 객체가 여럿 있지만 여기서는 스레드를 요청할 때마다 스레드를 새로 생성하는 SimpleAsyncTaskExecutor 객체를 사용합니다.

```kotlin
    @Bean
    @JobScope
    fun orderPagingStep(orderPagingReader: JpaPagingItemReader<Order>, taskExecutor: TaskExecutor): Step {
        return stepBuilderFactory.get("orderPagingStep")
                .chunk<Order, Order>(CHUNK_SIZE)
                .reader(orderPagingReader)
                .processor(pagingProcessor())
                .writer(pagingWriter())
                .taskExecutor(taskExecutor) // (1)
                .throttleLimit(2) // (2)
                .build()
                
    }

    @Bean
    @StepScope
    fun taskExecutor(): TaskExecutor {
        return SimpleAsyncTaskExecutor("Batch_task")
    }


```

* (1) 비으로 생성한 TaskExecutor를 등록
* (2) `throttleLimit()` 설정된 제한 횟수만큼 스레드를 동시에 시키겠다는 의미, 따라서 시스템에 할당된 스레드 풀 크기 보다 작은 값으로 설정되어야합니다. 만약 `1`로 설정하면 기존의 동기화 방식과 동일한 방식으로
  실행됩니다. `2`로 설정하면 스레드를 2개씩 실행시킵니다.
* (3) SimpleAsyncTaskExecutor를 생성해 빈을 등록합니다. 새엇ㅇ자의 매개변수로 들어가는 값ㅇ은 Task에 할당되는 이름입니다. 기본적으로 첫번째 Task `Batch_task1` 이라는 이름으로 할당되며 뒤에 붙는 숫자가 하나씩
  증가하여 이름이 정해집니다

```
2019-09-07 16:28:58.341 TRACE 38453 --- [    Batch_task1] o.h.type.descriptor.sql.BasicBinder      : binding parameter [1] as [BIGINT] - [167]
2019-09-07 16:28:58.341 TRACE 38453 --- [    Batch_task2] o.h.type.descriptor.sql.BasicExtractor   : extracted value ([amount2_0_0_] : [NUMERIC]) - [1209.11]
2019-09-07 16:28:58.341 TRACE 38453 --- [    Batch_task3] o.h.type.descriptor.sql.BasicExtractor   : extracted value ([amount2_0_0_] : [NUMERIC]) - [910.00]
2019-09-07 16:28:58.341 TRACE 38453 --- [    Batch_task2] o.h.type.descriptor.sql.BasicExtractor   : extracted value ([created_3_0_0_] : [TIMESTAMP]) - [2019-09-08T01:28:44]
2019-09-07 16:28:58.341 TRACE 38453 --- [    Batch_task2] o.h.type.descriptor.sql.BasicExtractor   : extracted value ([updated_4_0_0_] : [TIMESTAMP]) - [2019-09-08T01:28:44]
2019-09-07 16:28:58.341 TRACE 38453 --- [    Batch_task3] o.h.type.descriptor.sql.BasicExtractor   : extracted value ([created_3_0_0_] : [TIMESTAMP]) - [2019-09-08T01:28:44]
2019-09-07 16:28:58.341 TRACE 38453 --- [    Batch_task3] o.h.type.descriptor.sql.BasicExtractor   : extracted value ([updated_4_0_0_] : [TIMESTAMP]) - [2019-09-08T01:28:44]
2019-09-07 16:28:58.341 TRACE 38453 --- [    Batch_task4] o.h.type.descriptor.sql.BasicExtractor   : extracted value ([amount2_0_0_] : [NUMERIC]) - [94.00]
2019-09-07 16:28:58.342 TRACE 38453 --- [    Batch_task4] o.h.type.descriptor.sql.BasicExtractor   : extracted value ([created_3_0_0_] : [TIMESTAMP]) - [2019-09-08T01:28:44]
2019-09-07 16:28:58.342 TRACE 38453 --- [    Batch_task4] o.h.type.descriptor.sql.BasicExtractor   : extracted value ([updated_4_0_0_] : [TIMESTAMP]) - [2019-09-08T01:28:44]
Hibernate: select order0_.id as id1_0_0_, order0_.amount as amount2_0_0_, order0_.created_at as created_3_0_0_, order0_.updated_at as updated_4_0_0_ from orders order0_ where ord
2019-09-07 16:28:58.745 TRACE 38453 --- [    Batch_task5] o.h.type.descriptor.sql.BasicBinder      : binding parameter [1] as [NUMERIC] - [1209.11]
```

살행 로그를 보면 여러 스레드가 병렬로 실행되어 ㄹ그 실행 순서가 섞여있는 확인할 수있습니다. 스레드명이 `Batch_task5`까지 있어 스레드가 총 5개 할당되었음을 확인할 수있습니다.

## 여러 Flow 실행시키기

## 파티셔닝을 사용한 병렬 프로그래밍

파티셔닝을 통해서 Step 여러 개를 병렬로 실행 시킬 수 있습니다.

![](https://raw.githubusercontent.com/cheese10yun/TIL/master/assets/batch-partitioner.png)

특정 Step 절차 중 마스터가 있는데, 여기서 마스터를 여러 슬레이브로 나눕니다. **슬레이브는 일반적으로실행되는 스레드라고 생각 할 수 있습니다.** 슬레이브와 마스터는 모두 Step의 인스턴스입니다. **모든 슬레이브의 작업이 완료되면 결과가
합쳐저서 마스터가 완료되고 Step이 마무리 됩니다.**

휴면회원으로 전환할 회원을 각 등급에 따라 병렬로 처리하게끔 파티셔닝을 구혀해봅니다.

```java
public interface Partitioner{
    Map<String, ExecutionContext> partition(int gridSIZE);
}
```

Partitioner 인터페이스는 partition() 메서드만 제공합니다. **Partitioner() 메서드는 Step의 최대 분할 수를 지정하는 파라미터 gridSIZE를 갖습니다.** 키는 스레드명, 값은 ExecutionContext를 갖는
Map 타입을 반환합니다.

```java
public class InactiveUserPartitioner implements Partitioner {

    private static final String GRADE = "grade";
    private static final String INACTIVE_USER_TASK = "InactiveUserTask";

    @Override
    public Map<String, ExecutionContext> partition(int gridSIZE) {
        Map<String, ExecutionContext> map = new HashMap<>(gridSIZE); // (1)
        Grade[] grades = Grade.values(); // (2)
        for (int i = 0, length = grades.length; i < length; i++){ // (3)
            ExecutionContext context = new ExecutionContext();
            context.putString(GRADE, grades[i].name()); // (4)
            map.put(INACTIVE_USER_TASK + i, context) // (5)
        }
    }
}
```

* (1) gridSIZE만큼 Map을 할당합니다.
* (2) Grade Enum에 정의된 모든 값을 grades 배열 변수로 할당합니다.
* (3) grades 값만큼 파티션 생성하는 루프문을 돌립니다.
* (4) Step에서 파라미터로 Grade 값을 받아 사용합니다. 이대 ExecutionContext 키값은 `grade` 입니다.
* (5) 반환되는 map에 `InactiveUserTask1..2..3` 형식으로 파티션 키값을 지정하고 `(4)`에서 추가한 ExecutionContext를 map에 추가합니다.

```java


    @Bean
    @JobScope // (1)
    public Step partitionerStep(StepBuilderFactory stepBuilderFactory, Step inactiveJobStep) {
        return stepBuilderFactory
            .get("partitionerStep")
            .partitioner("partitionerStep", new InactiveUserPartitioner())
            .gridSIZE(5) // (2)
            .step(inactiveJobStep)
            .taskExecutor(taskExecutor())
            .build();
    }

    @Bean
    @StepScope
    public ListItemReader<User> inactiveUserReader(@Value("#{stepExecutionContext[grade]}") String grade, UserRepository userRepository) { // (4)
        List<User> inactiveUsers = userRepository.findByUpdateDateBeforeAndStatusEqualsAndGradeEquals(LocldaDateTime.now().minuYeaer(1), UserStatus.Active, Grade.valueOf(grade)); // (5)

        return new ListItemReader<>(inactiveUsers)
    }
```

* (1) Job 실행 때마다 빈을 새로 생성하는 @JobScope를 추가했습니다.
* (2) 파티셔닝을 사용하는 프로퍼티에 위에서 생성한 객체를 등록합니다.
* (3) grade 사이즈를 입력합니다.
* (4) SpEL을 사용해서 ExecutionContext에 할당한 등급값을 불러옵니다.
* (5) 휴면회원을 불러오는 쿼리에 등급을 추가해 해당 등급의 휴면 회원만 불러오도록 설정합니다.