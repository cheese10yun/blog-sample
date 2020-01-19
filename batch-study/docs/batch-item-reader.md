> [Spring Batch 가이드 - 7. ItemReader](https://github.com/jojoldu/spring-batch-in-action/blob/master/6_CHUNK.md)을 보고 정리한 글입니다.

# ItemRader
기존에 Step에서는 Tasklet 단위로 처리되고, Tasklet 중에서 ChunkOrientedTasklet를 통해 Chunk를 처리하며 이를 구성하는 3요소로 ItemReader, ItemWProcessor, ItemWriter가 있었습니다.

## ItemRader 소개
![](https://github.com/cheese10yun/TIL/blob/master/assets/spring-batch-item-reader.png?raw=true)


 Spring Batch의 ItemReader는 데이터를 읽어들입니다. **그게 꼭 DB의 데이터만을 얘기하진 않습니다.** File, XML, JSOM. CSV. Excel 등 다른 데이터 소스를 배치 처리의 입력으로 사용할 수 있습니다. 

 **이외에도 Spring Batch에서 지원하지 않은 Reader가 필요할 경우 직접 Reader를 만들수도 있습니다.**

 먼저 ItemReader를 살펴보면 **read() 만 가지고 있습니다.**

 ```java
 public interface ItemReader<T> {

	/**
	 * Reads a piece of input data and advance to the next one. Implementations
	 * <strong>must</strong> return <code>null</code> at the end of the input
	 * data set. In a transactional setting, caller might get the same item
	 * twice from successive calls (or otherwise), if the first call was in a
	 * transaction that rolled back.
	 * 
	 * @throws ParseException if there is a problem parsing the current record
	 * (but the next one may still be valid)
	 * @throws NonTransientResourceException if there is a fatal exception in
	 * the underlying resource. After throwing this exception implementations
	 * should endeavour to return null from subsequent calls to read.
	 * @throws UnexpectedInputException if there is an uncategorised problem
	 * with the input data. Assume potentially transient, so subsequent calls to
	 * read might succeed.
	 * @throws Exception if an there is a non-specific error.
	 * @return T the item to be processed or {@code null} if the data source is
	 * exhausted
	 */
	@Nullable
	T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException;
}
 ```

 ItemStream 인터페이스는 주기적으로 상태를 저장하고 오류가 발생하면 해당 상태에서 복원하기 위한 마커 인터페이스입니다. 즉, **배치 프로세스의 실행 컨텍스트와 연계해서 ItemReader의 상태를 저장하고 실패한 곳에서 다시실행할 수 있게 해주는 역할을 합니다.**

 ```java
 public interface ItemStream {

	/**
	 * Open the stream for the provided {@link ExecutionContext}.
	 *
	 * @param executionContext current step's {@link org.springframework.batch.item.ExecutionContext}.  Will be the
	 *                            executionContext from the last run of the step on a restart.
	 * @throws IllegalArgumentException if context is null
	 */
	void open(ExecutionContext executionContext) throws ItemStreamException;

	/**
	 * Indicates that the execution context provided during open is about to be saved. If any state is remaining, but
	 * has not been put in the context, it should be added here.
	 * 
	 * @param executionContext to be updated
	 * @throws IllegalArgumentException if executionContext is null.
	 */
	void update(ExecutionContext executionContext) throws ItemStreamException;

	/**
	 * If any resources are needed for the stream to operate they need to be destroyed here. Once this method has been
	 * called all other methods (except open) may throw an exception.
	 */
	void close() throws ItemStreamException;
}
 ```
ItemStream의 3개 메소드는 다음과 같은 역할을 합니다.
* open(), close()는 스트림을 열고 닫습니다.
* update()를 사용하면 Batch 처리의 상태를 업데이트 할 수 있습니다.

### JpaPagingItemReader
Spring Batch JPA를 지원하기 위해 JpaPagingItemReader를 공식적으로 지원하고 있습니다.

> 현재 Querydsl, Jooq 등을 통한 ItemReader 구현체는 공식 지원하지 않습니다. CustomItemReader 구현체를 만드셔야만 합니다.
이건 다른 글을 통해서 소개 드리겠습니다.
당장 필요하신 분들은 공식 문서를 참고해보세요

JPA는 Hibernate와 많은 유사점을 가지고 있습니다만, 한가지 다른 것이 있다면 **Hibernate 에선 Cursor가 지원되지만 JPA에는 Cursor 기반 Database 접근을 지원하지 않습니다.**

**PagingItemReader 주의 사항 정렬 (Order) 가 무조건 포함되어 있어야 합니다.**