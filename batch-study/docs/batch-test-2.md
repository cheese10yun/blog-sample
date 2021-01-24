# Spring Batch Test Code

스프링 배치 애플리케이션을 테스트 코드를 작성하면서 만났던 이슈와 그에 따른 나름의 노하우를 정리한 포스팅입니다.

## @SpringBatchTest 으로 편리하게 테스트 가능

[Spring Batch 4.1](https://docs.spring.io/spring-batch/docs/4.1.x/reference/html/whatsnew.html) 버전 부터는 `@SpringBatchTest` Annotation을 지원합니다.  

```java
@RunWith(SpringRunner.class)
@SpringBatchTest
@ContextConfiguration(classes = {JobConfiguration.class})
public class JobTest {

   @Autowired
   private JobLauncherTestUtils jobLauncherTestUtils;

   @Autowired
   private JobRepositoryTestUtils jobRepositoryTestUtils;


   @Before
   public void clearMetadata() {
      jobRepositoryTestUtils.removeJobExecutions();
   }

   @Test
   public void testJob() throws Exception {
      // given
      JobParameters jobParameters =
            jobLauncherTestUtils.getUniqueJobParameters();

      // when
      JobExecution jobExecution =
            jobLauncherTestUtils.launchJob(jobParameters);

      // then
      Assert.assertEquals(ExitStatus.COMPLETED,
                          jobExecution.getExitStatus());
   }
}
```

`@SpringBatchTest` 으로 위 코드를 자동으로 구성할 수 있습니다. 

## 한 개의 Step만 단위 테스트 하고 싶은 경우
