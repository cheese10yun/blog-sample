> [기억보단 기록을 - 10. Spring Batch 가이드 - Spring Batch 테스트 코드](https://jojoldu.tistory.com/455)을 보고 정리한 글입니다.

# Spring Batch Test

## Spring Context 문제

JobLauncherTestUtils 으로 테스트하는 경우

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