# Batch Table

![](https://github.com/cheese10yun/TIL/raw/master/assets/meta-data-erd.png)

배치 테이블 batch_xxxx들은 Entity를 상속받는 엔티티 객체로 구성되어 있으며 앞에 batch가 생략된 클래스 이름을 가진다. (EX :BATCH_JOB_EXECUTUON -> JobExecution)

## BATCH_JOB_INSTANCE

JOB_INSTANCE_ID | VERSION | JOB_NAME | JOB_KEY
----------------|---------|----------|--------
1 | 0 | inactiveUserJob | df9e59b818ab301226e71dcf67795b07
2 | 0 | inactiveUserJob | 34c2f2838f31f237450a6c7659e36995
3 | 0 | orderDailySumJob | d41d8cd98f00b204e9800998ecf8427e
4 | 0 | orderDailySumJob | d6832decf796311c39d3d934a9d7cfd5
5 | 0 | orderDailySumJob | 212470e06656926b4b339a42dc5d64c3

**BATCH_JOB_INSTANCE 테이블은 Job Parameter에 따라 생성되는 테이블입니다.** 즉 같은 Batch Job 이더라도 Job Parameter가 다르면 BATCH_JOB_INSTANCE 테이블에 각각 기록되묘, Job
Parameter가 같다면 기록되지 않습니다.

BATCH_JOB_EXECUTION_PARAMS 기반으로 JOB_KEY를 만들며 해당 값은 유니크 제약조건이 있기 때문에 같은 BATCH_JOB_EXECUTION_PARAMS을 넘기는 경우 생성되지 않는다.

## BATCH_JOB_EXECUTION

JOB_EXECUTION_ID | VERSION | JOB_INSTANCE_ID | CREATE_TIME | START_TIME | END_TIME | STATUS | EXIT_CODE | EXIT_MESSAGE | LAST_UPDATED | JOB_CONFIGURATION_LOCATION
-----------------|---------|-----------------|-------------|------------|----------|--------|-----------|--------------|--------------|---------------------------
1 | 2 | 1 | 2019-07-02 06:51:22 | 2019-07-02 06:51:22 | 2019-07-02 06:51:23 | COMPLETED | COMPLETED |  | 2019-07-02 06:51:23 | NULL
2 | 2 | 2 | 2019-07-02 07:14:00 | 2019-07-02 07:14:00 | 2019-07-02 07:14:01 | COMPLETED | COMPLETED |  | 2019-07-02 07:14:01 | NULL
3 | 2 | 3 | 2020-01-13 11:00:50 | 2020-01-13 11:00:50 | 2020-01-13 11:00:50 | COMPLETED | COMPLETED |  | 2020-01-13 11:00:50 | NULL
4 | 2 | 4 | 2020-01-13 11:59:15 | 2020-01-13 11:59:15 | 2020-01-13 11:59:15 | COMPLETED | COMPLETED |  | 2020-01-13 11:59:15 | NULL
5 | 2 | 5 | 2020-01-13 12:48:53 | 2020-01-13 12:48:53 | 2020-01-13 12:48:54 | COMPLETED | COMPLETED |  | 2020-01-13 12:48:54 | NULL

Job에대한 실행정보가 저장되있는 테이블입니다. createTime, startTime, endTtme, status, exitCode등 해당 Job에대한 결과 정보와 commitCount, readCount, filterCount,
readSkipCount등 해당 Job에 디에틸한 활동 내용까지 저장되어 있다.

**JOB_EXECUTION와 JOB_INSTANCE는 부모-자식 관계입니다.** JOB_EXECUTION은 자신의 무모 JOB_INSTANCE는가 성송/실패 했던 내역을 가지고 있습니다.

### BATCH_JOB_EXECUTION Entity

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

## BATCH_JOB_EXECUTION_PARAMS

JOB_EXECUTION_ID | TYPE_CD | KEY_NAME | STRING_VAL | DATE_VAL | LONG_VAL | DOUBLE_VAL | IDENTIFYING
-----------------|---------|----------|------------|----------|----------|------------|------------
5 | STRING | requestDate | 2019-10-13 | 1970-01-01 00:00:00 | 0 | 0 | N
5 | LONG | run.id |  | 1970-01-01 00:00:00 | 2 | 0 | Y
5 | STRING | version | 12 | 1970-01-01 00:00:00 | 0 | 0 | Y
5 | STRING | -job.name | orderDailySumJob | 1970-01-01 00:00:00 | 0 | 0 | N

**BATCH_JOB_EXECUTION에 대한 Parameter정보들이 저장되는 곳이다** BATCH_JOB_EXECUTION, BATCH_JOB_EXECUTION_PARAMS 1:N 관계이며 위 테이블은 ID 5번에 들어가는 parameter
정보들이 저장된다

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