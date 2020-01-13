> [기억보단 기록을 - 10. Spring Batch 가이드 - Spring Batch 테스트 코드](https://jojoldu.tistory.com/455)을 보고 정리한 글입니다.

# Spring Batch Test

## Spring Context 문제

JobLauncherTestUtils 으로 테스트하는 경우 아래 코드를 처럼 Bean에 올라가야할 Job이 반드시 하나이어야 합니다. (@Autowired setJob()로 현재 Bean에 올라간 Job을 주입받기 때문)

```java
/**
* The Job instance that can be manipulated (e.g. launched) in this utility.
* 
* @param job the {@link AbstractJob} to use
*/
@Autowired
public void setJob(Job job) {
	this.job = job;
}
```
JobLauncherTestUtils에서 여러개의 등록된 Job Bean 중 어떤것을 올려야할지 모르기때문에 에러가 발생하게 됩니다.


```
@TestPropertySource(properties = {"job.name=xxx"})
```
위와 같이 properties으로 해결이 가능하지만 properties 변경되게 되면 Spring에서는 테스트가 구동되는 동안 계속 Spring Bean Context를 올리게 됩니다. 그렇게 되면 테스트 결과가 현저하게 느려집니다.

하지만 장점도 있습니다. Job, Step, Reader 등의 Bean의 name이 중복되더라도 해당 실행에서는 Bean을 올리지않기 때문에 비교적 Bean name문제에서 자유롭습니다.

또 실제 Spring Batch가 구동되는 Production에서도 해당 Job에서 사용할 Bean들만 올리기 때문에 속도, 리소스 적인 면에서 장점이 될 수 있습니다.

## @ContextConfiguration 사용하기
> @SpringBootTest(classes={...}) 는 내부적으로 @ContextConfiguration를 사용하기 때문에 둘은 같습니다.

해당 어노테이션으로 ApplicatopnContext에서 괸리할 Bean과 Configuration 들을 지정할수 있습니다. 떄문에 특정 Batch Job의 설정들만 가져와서 수행할 수 있습니다. **다만 이 방식을 선택해도 기본적으로 전체 테스트 수행시 Spring Context가 재실행되는 것은 여전합니다.**

```java
@Configuration
@EnableAutoConfiguration
@EnableBatchProcessing // (1)
public class TestBatchLegacyConfig {

    @Bean
    public JobLauncherTestUtils jobLauncherTestUtils() { // (2)
        return new JobLauncherTestUtils();
    }
}
```
* (1)@EnableBatchProcessing
  * 배치 환경을 자동 설정합니다.
  * 테스트 환경에서도 필요하기 때문에 별도의 설정에서 선언되어 사용합니다.
  * 모든 테스트 클래스에서 선언하는 불편함을 없애기 위함입니다.
* (2) @Bean JobLauncherTestUtils
  * 스프링 배치 테스트 유틸인 JobLauncherTestUtils을 Bean으로 등록합니다.
  * JobLauncherTestUtils 를 이용해서 JobParameter를 사용한 Job 실행 등이 이루어집니다.
  * JobLauncherTestUtils Bean을 각 테스트 코드에서 @Autowired로 호출해서 사용합니다.


## @SpringBatchTest
4.1.x 이상 (부트 2.1) 버전에서 추가된 @SpringBatchTest 입니다. 해당 어노테이션을 추가하면 자동으로 ApplicationContext 테스트에 필요한 Bean들을 등록할 수 있습니다.

자동으로 등록되는 빈은 총 4개입니다

* JobLauncherTestUtils : 스프링 배치 테스트에 필요한 전반적인 유틸 기능들을 지원
* JobRepositoryTestUtils : DB에 저장된 JobExcution을 생성/삭제 지원
* StepScopeTestExecutionListener : 배치 단위 테스트시 StepScope 컨텍스트를 생성, 해당 컨텍스트를 통해 JobParamerter 등을 단위 테스트에서 DI 받을 수 있음
* JobSopceTestExecutionListener : 배치 단위 테스트시 JobScope 컨텍스트를 생성, 해당 컨텍스트를 통해 JobParameter등을 단위 테스트에서 DI 받을 수 있음

```java
@RunWith(SpringRunner.class)
@SpringBatchTest // (1)
@SpringBootTest(classes={BatchJpaTestConfiguration.class, TestBatchConfig.class}) // (2)
public class BatchIntegrationTestJobConfigurationNewTest {
    ...
}

@Configuration
@EnableAutoConfiguration
@EnableBatchProcessing
public class TestBatchConfig {}
```

* (1) @SpringBatchTest
  * 현재 테스트에선 JobLauncherTestUtils를 지원 받기 위해 사용됩니다.
* (2) TestBatchConfig.class
  * @SpringBatchTest 로 인해 불필요한 설정이 제거된 Config 클래스