# Batch Table

![](https://github.com/cheese10yun/TIL/raw/master/assets/meta-data-erd.png)


배치 테이블 batch_xxxx들은 Entity를 상속받는 엔티티 객체로 구성되어 있으며 앞에 batch가 생략된 클래스 이름을 가진다. (EX :BATCH_JOB_EXECUTUON -> JobExecution)

## JobExecution

```java
public class JobExecution extends Entity {

	private final JobParameters jobParameters;

	private JobInstance jobInstance;

	private volatile Collection<StepExecution> stepExecutions = Collections.synchronizedSet(new LinkedHashSet<>());

	private volatile BatchStatus status = BatchStatus.STARTING;

	private volatile Date startTime = null;

	private volatile Date createTime = new Date(System.currentTimeMillis());

	private volatile Date endTime = null;

	private volatile Date lastUpdated = null;

	private volatile ExitStatus exitStatus = ExitStatus.UNKNOWN;

	private volatile ExecutionContext executionContext = new ExecutionContext();

	private transient volatile List<Throwable> failureExceptions = new CopyOnWriteArrayList<>();

	private final String jobConfigurationName;
}
```
* jobParameters : Job 실행에 필요한 매개변수 데이터입니다.
* jobInstance : Job 실행 단위가 되는 객체입니다.
* stepExecutuons : StepExecutuon을 여러개 가질 수 있는 Collection 타입입니다.
* status : Job 실행 상태를 나타내는 필드(Enum)입니다. 상태값으로는 `COMPLETED, STARTING, STARTED, STOPPING, STOPPED, FAILED, ABANDONED, UNKNOWN` 이 있습니다.
* startTime : Job이 실행된 시간입니다. null이면 시작되지 않았다는 의미 입니다.
* createTime : JobExecution이 생성된 시간입니다.
* endTime: JobExecution이 끝난 시간입니다.

## StepExecution

```java
public class StepExecution extends Entity {

	private final JobExecution jobExecution;

	private final String stepName;

	private volatile BatchStatus status = BatchStatus.STARTING;

	private volatile int readCount = 0;

	private volatile int writeCount = 0;

	private volatile int commitCount = 0;

	private volatile int rollbackCount = 0;

	private volatile int readSkipCount = 0;

	private volatile int processSkipCount = 0;

	private volatile int writeSkipCount = 0;

	private volatile Date startTime = new Date(System.currentTimeMillis());

	private volatile Date endTime = null;

	private volatile Date lastUpdated = null;

	private volatile ExecutionContext executionContext = new ExecutionContext();

	private volatile ExitStatus exitStatus = ExitStatus.EXECUTING;

	private volatile boolean terminateOnly;

	private volatile int filterCount;

	private transient volatile List<Throwable> failureExceptions = new CopyOnWriteArrayList<Throwable>();
}
```
* jobExecution : 현재의 jobExecutuon 정보를 담고 있는 필드입니다.
* stepName : Step의 이름을 가지고 있는 필드입니다.
* status : Job 실행 상태를 나타내는 필드(Enum)입니다. 상태값으로는 `COMPLETED, STARTING, STARTED, STOPPING, STOPPED, FAILED, ABANDONED, UNKNOWN` 이 있습니다.
* readCount : 성공적으로 읽은 레코드의 수입니다.
* wirteCount : 성공적으로 쓴 레코드 수 입니다.
* commitCount : Step의 실행에 대해 커밋된 트랜잭션 수입니다.
* rollbackCount : Step의 실행에 대해 롤백된 트랜잭션 수입니다.
* readSkipCount : 읽기에 실패해 건너뛴 레코드 수입니다,
* processSkipCount : 프로세스가 실패해 건너뛴 레코드 수입니다.
* writeSkipCount : 쓰기에 실패해 건너뛴 레코드 수입니다.
* startTime : Step이 실행딘 시간 입니다. null이면 시작하지 않았다는 것을 나타냅니다.
* endTime : Step의 실행 성공 여부의 관련 없이 Step이 끝난 시간입니다.
* lastUpdated : 마지막으로 수정된 시간입니다.
* executuonContext : Step 실행 사이에 유지해야 하는 사용자 데이터라 들어갑니다.
* terminateOnly : Job 실행 중지 여부입니다.
* filterCount : 실행에서 필터링된 레코드 수입니다.
* failureExecutions : Step 실행 중 발생한 예외를 List 타입으로 지정합니다.