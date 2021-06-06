# Custom Item Reader

pageable 기반의 Item Reader를 커스텀해서 작성할 일이 많습니다. 블라 블라...

## PageableItemReader 전체 코드

```kotlin
open class PageableItemReader<T>(
        private val name: String,
        private val sort: Sort,
        private val pageSize: Int = DEFAULT_PAGE_SIZE,
        private val query: (Pageable) -> Page<T>
) : ItemStreamSupport(), ItemReader<T> {

    private var totalPage by notNull<Int>()
    private var page = 0
    private var readContent = mutableListOf<T>()
    private lateinit var stepExecution: StepExecution
    
    init {
        super.setName(this.name)
    }

    override fun read(): T? {
        return when {
            this.readContent.isEmpty() -> null
            else -> this.readContent.removeAt(this.readContent.size - 1)
        }
    }

    @BeforeStep
    @Suppress("UNUSED")
    fun beforeStep(stepExecution: StepExecution) {
        this.stepExecution = stepExecution
        this.totalPage = readRows().totalPages
        this.page = totalPage - 1
    }

    @BeforeRead
    @Suppress("UNUSED")
    fun beforeRead() {
        when {
            this.page < 0 -> return
            this.readContent.isEmpty() -> readContent = readRows(page).content.toMutableList()
        }
    }

    @AfterRead
    @Suppress("UNUSED")
    fun afterRead() {
        if (readContent.isEmpty()) page--
    }

    private fun readRows(page: Int = 0): Page<T> {
        return when {
            this.page < 0 -> Page.empty()
            else -> try {
                this.query.invoke(PageRequest.of(page, pageSize, sort))
            } catch (e: Exception) {
                this.stepExecution.status = BatchStatus.FAILED
                this.stepExecution.exitStatus = ExitStatus.FAILED
                throw e
            }
        }
    }
}
```

### PageableItemReader 생성자

`PageableItemReader`는 이전에 작성 `JpaPagingItemReaderBuilder` 사용법과 비슷합니다. 코틀린에서는 Named Parameter를 지원하기 때문에 별의 Builder Pattern을 사용하지 않고 손쉽게 객채
생성이 가능하기 때문에 별도의 Builder를 생성하지 않고 객채 생성을 진행 하면됩니다.

```kotlin
open class PageableItemReader<T>(
        private val name: String,
        private val sort: Sort,
        private val pageSize: Int = DEFAULT_PAGE_SIZE,
        private val query: (Pageable) -> Page<T>
) : ItemStreamSupport(), ItemReader<T> {
    ....
}
```

* name: 해당 페이징 reader의 이름을 지정합니다.
* sort: 페이징 처리를 위한 sort를 받습니다. 당연한 이야기 이지만 페이징 처리르 위해서는 sort를 받아야 합니다.
* pageSIZE: paging 처리르 위한 pageSIZE를 받습니다. 여기서 중요한 점은 pageSIZE만큼 ChunkSIZE가 설정되며, 해당 ChunkSize의로 트랜잭션 범위가 묶이게 됩니다.
* query: 실제 페이징 쿼리를 진행하는 Pageable 인퍼페이스를 받습니다.

### BeforeStep

```kotlin
@BeforeStep
@Suppress("UNUSED")
fun beforeStep(stepExecution: StepExecution) {
    this.stepExecution = stepExecution
    this.totalPage = readRows().totalPages
    this.page = totalPage - 1
}
```

`@BeforeStep` 아노테이션으로 해당 Step이 진행되기 전에 실행됩니다. 실행할 stepExceution을 지정하고 페이징 작업에 필요한 `totalPage`, `page`을 지정합니다. `totalPage`를 구해올
때 `readRows()`를 통해서 얻어 옵니다.

### readRows

```kotlin
private fun readRows(page: Int = 0): Page<T> {
    return when {
        this.page < 0 -> Page.empty()
        else -> try {
            this.query.invoke(PageRequest.of(page, pageSize, sort))
        } catch (e: Exception) {
            this.stepExecution.status = BatchStatus.FAILED
            this.stepExecution.exitStatus = ExitStatus.FAILED
            throw e
        }
    }
}
```

`query(Pageable)`를 이용해서 실제 query를 invoke를 진행합니다. 이로써 `Pagealbe`객체를 통해서 페징처리를 위한 `totalPage`, `totlaElement`, `content`등 필요한 정보를 얻을 수 있습니다.

### BeforeRead

```kotlin
@BeforeRead
@Suppress("UNUSED")
fun beforeRead() {
    when {
        this.page < 0 -> return
        this.readContent.isEmpty() -> readContent = readRows(page).content.toMutableList()
    }
}
```

`@BeforeRead`어노테이션으로 실제 ItemReader가 동작하기전에 해당 코드가 동작합니다. `this.page < 0`경우  **(page는 0부터 시작 하니 0 page까지 다읽고 -1 page가 되면 더이상 read할 데이터가 없으니
종료 합니다)** 이제 더이상 작업을 진행하지 않기 위해서 return으로 종료를 진행합니다.

또 `this.readContent.isEmpty()` 해당 page content가 없는 경우 다음 페이지 (정확히는 페이지를 감소 시키기 때문에 이전 이라고 할 수 있음)의 content 리스트를 가저옵니다. 페이지 감소는 `read` 메서드 에서
진행됩니다.

### read

```kotlin
override fun read(): T? {
    return when {
        this.readContent.isEmpty() -> null
        else -> this.readContent.removeAt(this.readContent.size - 1)
    }
}
```

`ItemReader<T>` 인터페이스의 메서드로써 실제 ItemReader가 read할때 호출 되는 메서드입니다.

`this.readContent.isEmpty() -> null` 페이지가 비어 있는 경우 null을 리턴하여 모든 대상 ItemReader의 끝을 알리게 됩니다.

그게 아닌 경우는 `this.readContent.removeAt(this.readContent.size - 1)` 코드를 통해서 conent에서 list를 제거 하며 제거된 element를 리턴 하게 됩니다.

#### Chunk를 처리하는 방법

```kotlin
public void Chunk_처리_방법(int chunkSize, int totalSize){
    for (int i= 0; i < totalSize; i = i + chunkSize){
        List items = new ArrayList();
        for(int j = 0; j < chunkSize; j++){
            Object item = itemReader.read();
            Object processedItem = itemProcessor.process(item);
            items.add(processedItem);
        }
        itemWriter.write(items);
    }
}
```

`read()` 메서드를 이해하기 위해서는 Chunk 처리에 대한 코드 흐름을 이해해야합니다. Chunk 처리는 위와 같은 메커니즘으로 동작하게 됩니다. itemReader를 통해서 한개의 element를 구해오고, 그것을 itemProcessor를
통해서 가공을 합니다.(필요에 따라 ItemProcessor는 없을 수 있으며 없는 경우 ItemWriter의 리스트에 바로 추가 됩니다.)

**즉 `this.readContent.removeAt(this.readContent.size - 1)` code로 element를 리턴하게 되어 writeItem element에 추가되는 것입니다.**

**또 `removeAt(...)` 코드를 통해서 element를 반환하는 동시에 반환된(Read가 완료된) element는 page content에서 제거하고 있습니다**

### AfterRead

```kotlin
@AfterRead
@Suppress("UNUSED")
fun afterRead() {
    if (readContent.isEmpty()) page--
}
```

`@AfterRead`를 어노테이션을 통해서 Read가 완료된 시점에 호출됩니다.종료됩니다. `if (readContent.isEmpty()) page--` 코드를 통해서 read 한 content 리스타가 비어 있는 경우 다음 페이지 (뒤레서 부터
시작 하니 실제는 이전 페이지라고 할 수있음) 옮겨 가기 위해서 `page--`을 진행한다

## PageableItemReader 사용법

```kotlin
private fun reader(): PageableItemReader<Order> {
    return PageableItemReader(
            name = "PageableItemReaderReader",
            sort = Sort.by(Sort.Direction.ASC, "id"),
            pageSize = chunkSize,
            query = { orderRepository.findByAmountGreaterThan(BigDecimal(5000), it) }
    )
}
```

PageableItemReader 생성을 위한 필요 값들을 넘겨줍니다. `query`는 amount 5000 보다 큰 값에 대한 order를 넘겨 받게 됩니다.

STEP_EXECUTION_ID | VERSION | STEP_NAME | JOB_EXECUTION_ID | START_TIME | END_TIME | STATUS | COMMIT_COUNT | READ_COUNT | FILTER_COUNT | WRITE_COUNT | READ_SKIP_COUNT | WRITE_SKIP_COUNT | PROCESS_SKIP_COUNT | ROLLBACK_COUNT | EXIT_CODE | EXIT_MESSAGE | LAST_UPDATED
------------------|---------|-----------|------------------|------------|----------|--------|--------------|------------|--------------|-------------|-----------------|------------------|--------------------|----------------|-----------|--------------|-------------
135 | 7 | PageableItemReaderStep | 125 | 2020-01-25 11:54:19 | 2020-01-25 11:54:19 | COMPLETED | 5 | 988 | 0 | 988 | 0 | 0 | 0 | 0 | COMPLETED |  | 2020-01-25 11:54:19

실제 Step을 보시면 해당 Step이 정상동작하는 것을 확인 할 수 있습니다.