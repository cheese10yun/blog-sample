> [Spring Batch 가이드 - 6. Chunk 지향 처리](https://github.com/jojoldu/spring-batch-in-action/blob/master/6_CHUNK.md)을 보고 정리한 글입니다.

# Chunk 지향 처리

Spring Batch의 킁 장점 중 하나는 Chunk 지향 처리에있습니다. Chunk 지향에 대해서 알아보겠습니다.

## Chunk 란 ?

Spring Batch에서의 Chunk란 **데이터의 덩어리로 작업 할 때 각 커밋 사이의 처리되는 row수를 뜻합니다.**

**Chunk 단위로 트랜잭션을 수행하기 때문에 실패할 경우 해당 Chunk 만큼 롤백이 되고, 이전에 커밋도니 트랜잭션 범위까지는 반영이 됩니다.**

![](https://docs.spring.io/spring-batch/docs/4.0.x/reference/html/images/chunk-oriented-processing.png)

> 이미지 출처 [docs.spring.io](https://docs.spring.io/spring-batch/docs/4.0.x/reference/html/index-single.html#chunkOrientedProcessing)

* Reader에서 데이터를 하나 읽어 옵니다.
* 읽어온 데이터를 Processor에서 가공합니다.
* 가공된 데이터들을 별도의 공간에 모은뒤, Chunk 단위만큼 쌓이게 되면 Writer에 전달하고 Writer는 일괄 저장합니다.

**Reader와 Processor에서는 1건씩 다뤄지고, Writer에선 Chunk 단위로 처리된다는 것이 중요합니다.**

Chunk 지향 처리를 Java 코드로 표현하면 아래처럼 될 것 같습니다.

```java
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

**즉 chunkSize 별로 묶는 다는 것은 total_size에서 chunk_size 만큼 읽어 자장한다는 의미입니다.**

## ChunkOrientedTasklet 엿보기

Chunk 지향 처리의 전체 로직을 다루는 것은 ChunkOrientedTasklet 클래스입니다.

```java
public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Chunk<I> inputs = (Chunk)chunkContext.getAttribute("INPUTS");
        if (inputs == null) {
            inputs = this.chunkProvider.provide(contribution);
            if (this.buffering) {
                chunkContext.setAttribute("INPUTS", inputs);
            }
        }

        this.chunkProcessor.process(contribution, inputs);
        this.chunkProvider.postProcess(contribution, inputs);
        if (inputs.isBusy()) {
            logger.debug("Inputs still busy");
            return RepeatStatus.CONTINUABLE;
        } else {
            chunkContext.removeAttribute("INPUTS");
            chunkContext.setComplete();
            if (logger.isDebugEnabled()) {
                logger.debug("Inputs not busy, ended: " + inputs.isEnd());
            }

            return RepeatStatus.continueIf(!inputs.isEnd());
        }
    }
```

여기서 자세히 보셔야할 코드는 execute() 입니다. Chunk 단위로 작업하기 위한 전체 코드입니다.

```java
inputs = this.chunkProvider.provide(contribution);
```

코드를 통해서 Reader에서 Chunk size만큼 데이터를 가져옵니다.

```java
this.chunkProcessor.process(contribution, inputs);
```

Reader로 받은 데이터를 Processor(가공)하고 Writer(저장)합니다.

데이터를 가져오는 chunkProvider.provide()를 가보시면 어떻게 데이터를 가져오는지 알 수 있습니다.

```java
@Override
public Chunk<I> provide(final StepContribution contribution) throws Exception {

    final Chunk<I> inputs = new Chunk<I>();
    repeatOperations.iterate(new RepeatCallback() {

        @Override
        public RepeatStatus doInIteration(final RepeatContext context) throws Exception {
            I item = null;
            try {
                item = read(contribution, inputs);
            }
            catch (SkipOverflowException e) {
                // read() tells us about an excess of skips by throwing an
                // exception
                return RepeatStatus.FINISHED;
            }
            if (item == null) {
                inputs.setEnd();
                return RepeatStatus.FINISHED;
            }
            inputs.add(item);
            contribution.incrementReadCount();
            return RepeatStatus.CONTINUABLE;
        }
    });
    return inputs;
}
```

`inputs`이 ChunkSize 만큼 쌓일때까지 `read()`를 호출합니다. 이 read() 는 내부를 보시면 실제로는 ItemReader.read를 호출합니다.

```java
@Nullable
protected I read(StepContribution contribution, Chunk<I> chunk) throws SkipOverflowException, Exception {
    return doRead();
}

@Nullable
protected final I doRead() throws Exception {
    try {
        listener.beforeRead();
        I item = itemReader.read();
        if(item != null) {
            listener.afterRead(item);
        }
        return item;
    }
    catch (Exception e) {
        if (logger.isDebugEnabled()) {
            logger.debug(e.getMessage() + " : " + e.getClass().getName());
        }
        listener.onReadError(e);
        throw e;
    }
}
```

**즉, ItemReader.read에서 1건씩 데이터를 조회해 Chunk size만큼 데이터를 쌓는 것이 provide()가 하는 일입니다**. 자 그럼 이렇게 쌓아준 데이터를 어떻게 가공하고 저장하는지 한번 확인해보겠습니다.

## SimpleChunkProcessor 엿보기

Processor와 Writer 로직을 담고 있는 것은 ChunkProcessor 가 담당하고 있습니다.

```java
public interface ChunkProcessor<I> {
	
	void process(StepContribution contribution, Chunk<I> chunk) throws Exception;

}
```

ChunkProcessor 인터페이스이고 기본적으로 사용되는 구현체는 SimpleChunkProcessor 입니다.

```java
@Override
public final void process(StepContribution contribution, Chunk<I> inputs) throws Exception {

    // Allow temporary state to be stored in the user data field
    initializeUserData(inputs);

    // If there is no input we don't have to do anything more
    if (isComplete(inputs)) {
        return;
    }

    // Make the transformation, calling remove() on the inputs iterator if
    // any items are filtered. Might throw exception and cause rollback.
    Chunk<O> outputs = transform(contribution, inputs);

    // Adjust the filter count based on available data
    contribution.incrementFilterCount(getFilterCount(inputs, outputs));

    // Adjust the outputs if necessary for housekeeping purposes, and then
    // write them out...
    write(contribution, inputs, getAdjustedOutputs(inputs, outputs));

}
```

* `Chunk<T> inputs` 를 파라미터로 받습니다
    * 이 데이터는 앞어 `chunkProvider.provide()` 에서 받은 ChunkSize만큼 쌓인 item 입니다.
* `transform()` 에서는 전달 받은 input을 `doProcess()`로 전달하고 변환값을 받습니다.
* `transform()` 을 통해 가공된 대량의 데이터는 `wirte()`를 통해 일괄 저장됩니다.
    * `wirte()`는 저장이될 수 있고, 외부 API로 전송할 수도 있습니다.
    * 이는 개발자가 `ItemWriter`를 어떻게 구현했는지에 따라 달라집니다.

## Page Size vs Chunk Size

기존에 Spring Batch를 사용해보신 분들은 아마 PagingItemReader를 많이들 사용해보셨을 것입니다. PagingItemReader를 사용하신 분들 중 간혹 Page Size와 Chunk Size를 같은 의미로 오해하시는 분들이
계시는데요. **Page Size와 Chunk Size는 서로 의미하는 바가 다릅니다.**

**Chunk Size는 한번에 처리될 트랜잭션 단위를 얘기하며, Page Size는 한번에 조회할 Item의 양을 얘기합니다.**

2개가 어떻게 다른지 실제 Spring Batch의 ItemReader 코드를 직접 들여다보겠습니다.

### PagingItemReader

PagingItemReader의 부모 클래스인 AbstractItemCountingItemStreamItemReader의 read() 메소드를 먼저 보겠습니다.

```java
@Override
public T read() throws Exception, UnexpectedInputException, ParseException {
    if (currentItemCount >= maxItemCount) {
        return null;
    }
    currentItemCount++;
    T item = doRead();
    if(item instanceof ItemCountAware) {
        ((ItemCountAware) item).setItemCount(currentItemCount);
    }
    return item;
}
```

보시는것처럼 읽어올 데이터가 있다면 `doRead()`를 호출합니다.

```java
@Override
protected T doRead() throws Exception {

    synchronized (lock) {

        if (results == null || current >= pageSize) {

            if (logger.isDebugEnabled()) {
                logger.debug("Reading page " + getPage());
            }

            doReadPage();
            page++;
            if (current >= pageSize) {
                current = 0;
            }

        }

        int next = current++;
        if (next < results.size()) {
            return results.get(next);
        }
        else {
            return null;
        }

    }

}
```

doRead()에서는 현재 읽어올 데이터가 없거나, Page Size를 초과한 경우 doReadPage()를 호출합니다. 읽어올 데이터가 없는 경우 read가 처음 시작할 때를 얘기합니다. Page SIZE를 초과하는 경우, 예를 들면 Page
Size가 10인데, 이번에 읽어야할 데이터가 11 번째 데이터인 경우입니다. 이런 경우 Page Size를 초과했기 때문에 `doReadPage()`를 호출합니다.

**즉, Page 단위로 끊어서 조회하는 것입니다.**

doReadPage()부터는 하위 구현 클래스에서 각자만의 방식으로 페이징 쿼리를 생성합니다. 여기서는 보편적으로 많이 사용하시는 JpaPagingItemReader 코드를 살펴보겠습니다.

JpaPagingItemReader의 doReadPage()의 코드는 아래와 같습니다.

```java
@Override
@SuppressWarnings("unchecked")
protected void doReadPage() {

    EntityTransaction tx = null;
    
    if (transacted) {
        tx = entityManager.getTransaction();
        tx.begin();
        
        entityManager.flush();
        entityManager.clear();
    }//end if

    Query query = createQuery().setFirstResult(getPage() * getPageSize()).setMaxResults(getPageSize());

    if (parameterValues != null) {
        for (Map.Entry<String, Object> me : parameterValues.entrySet()) {
            query.setParameter(me.getKey(), me.getValue());
        }
    }

    if (results == null) {
        results = new CopyOnWriteArrayList<>();
    }
    else {
        results.clear();
    }
    
    if (!transacted) {
        List<T> queryResult = query.getResultList();
        for (T entity : queryResult) {
            entityManager.detach(entity);
            results.add(entity);
        }//end if
    } else {
        results.addAll(query.getResultList());
        tx.commit();
    }//end if
}
```

```java
Query query = createQuery().setFirstResult(getPage() * getPageSize()).setMaxResults(getPageSize());
```

Page 만큼 추가 조회

```java
results.addAll(query.getResultList());
```

조회 결과 results에 저장

Reader에서 지정한 Page Size 만큼 offset, limit 값을 지정하여 페이징 쿼리를 생성 (`createQuery()`) 하고, 사용(`query.getResulList()`) 합니다. 이렇게 저장된 results에 `read()`가
호출할때마나 하나씩 거내서 전달합니다.

**즉, Page Size는 페이징 쿼리에서 Page의 Size를 지정하기 위한 값입니다.**

만약 2개 값이 다르면 어떻게 될까요?
**PageSize가 10이고, ChunkSize가 50이라면 ItemReader에서 Page 조회가 5번 일어나면 1번 의 트랜잭션이 발생하여 Chunk가 처리됩니다.**

한번의 트랜잭션 처리를 위해 5번의 쿼리 조회가 발생하기 때문에 성능상 이슈가 발생할 수 있습니다. 그래서 Spring Batch의 PagingItemReader에는 클래스 상단에 다음과 같은 주석을 남겨두었습니다.

> Setting a fairly large page size and using a commit interval that matches the page size should provide better performance.
(상당히 큰 페이지 크기를 설정하고 페이지 크기와 일치하는 커미트 간격을 사용하면 성능이 향상됩니다.)

2개 값이 의미하는 바가 다르지만 위에서 언급한 여러 이슈로 **2개 값(ChunkSIZE, PageSIZE)을 일치시키는 것이 보편적으로 좋은 방법이니 꼭 2개 값을 일치시키시길 추천드립니다.**