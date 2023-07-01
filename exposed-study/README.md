# Kotlin 기반 경량 ORM Exposed

## Exposed 란 ?

[Exposed](https://github.com/JetBrains/Exposed)는 JetBrains에서 만든 Kotlin 언어 기반의 ORM 프레임워크입니다. Exposed는 두 가지 레벨의 데이터베이스 access를 제공합니다. SQL을 매핑 한 DSL 방식, 경량화한 DAO 방식을 제공하며 공식적으로 H2, MySQL, MariaDB, Oracle, PostgreSQL, SQL Server, SQLite 데이터베이스를 지원합니다.

## 그래서 왜 쓰는 데 ?

저는 개인적, 회사 업무에서 [Spring Data JPA](https://spring.io/projects/spring-data-jpa)를 주로 사용하고 있습니다. JPA가 가져다주는 큰 장점이 많아 적극적으로 사용하고 있지만 **특정 상황에서 하이버네이트의 단점이 있어 이를 보안하기 위해서 Exposed를 사용하고 있습니다.** 해당 내용은 [Batch Insert 성능 향상기 1편 - With JPA](https://cheese10yun.github.io/jpa-batch-insert/), [Batch Insert 성능 향상기 2편 - 성능 측정](https://cheese10yun.github.io/spring-batch-batch-insert/)에서 포스팅한 바 있습니다.

## Getting Started

### MySQL

```yml
version: '3'

services:
    db_mysql:
        container_name: mysql.local
        image: mysql/mysql-server:5.7
        environment:
            MYSQL_ROOT_HOST: '%'
            MYSQL_DATABASE: 'exposed_study'
            MYSQL_ALLOW_EMPTY_PASSWORD: 'yes'
        ports:
            - '3366:3306'
        volumes:
            - './volumes/mysql/default:/var/lib/mysql'
        command:
            - 'mysqld'
            - '--character-set-server=utf8mb4'
            - '--collation-server=utf8mb4_unicode_ci'
            - '--sql_mode=STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION'
```

```
$ docker-compose up -d
```

Exposed에서 지원해 주는 MySQL 기반으로 진행하기 위해서 해당 환경을 Docker로 구성합니다.

### Gradle
```gradle
dependencies {
    implementation("mysql:mysql-connector-java")
    implementation("org.jetbrains.exposed:exposed-core:0.31.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.31.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.31.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.31.1")
}
```
필요한 의존성을 추가합니다.

### Config

```kotlin
class ExposedGettingStarted {
    private val config = HikariConfig().apply {
        jdbcUrl = "jdbc:mysql://localhost:3366/exposed_study?useSSL=false&serverTimezone=UTC&autoReconnect=true&rewriteBatchedStatements=true"
        driverClassName = "com.mysql.cj.jdbc.Driver"
        username = "root"
        password = ""
    }

    private val dataSource = HikariDataSource(config)
    ...
}
```
`HikariConfig`에 위에서 생성한 MySQL 정보를 입력하여 `DataSource`를 생성합니다.


## DSL 방식

```kotlin
object Payments : LongIdTable(name = "payment") {
    val orderId = long("order_id")
    val amount = decimal("amount", 19, 4)
}

class ExposedGettingStarted {

    @Test
    fun `exposed DSL`() {
        // connection to MySQL
        Database.connect(dataSource)

        transaction {
            // Show SQL logging
            addLogger(StdOutSqlLogger)

            // CREATE TABLE IF NOT EXISTS payment (id BIGINT AUTO_INCREMENT PRIMARY KEY, order_id BIGINT NOT NULL, amount DECIMAL(19, 4) NOT NULL)
            SchemaUtils.create(Payments)

            // INSERT INTO payment (amount, order_id) VALUES (1, 1)
            // ...
            (1..5).map {
                Payments.insert { payments ->
                    payments[amount] = it.toBigDecimal()
                    payments[orderId] = it.toLong()
                }
            }

            // UPDATE payment SET amount=0 WHERE payment.amount >= 0
            Payments.update({ amount greaterEq BigDecimal.ZERO })
            {
                it[amount] = BigDecimal.ZERO
            }

            // SELECT payment.id, payment.order_id, payment.amount FROM payment WHERE payment.amount = 0
            // Payment(amount=1.0000, orderId=1)
            Payments.select { amount eq BigDecimal.ZERO }
                    .forEach { println(it) }

            // DELETE FROM payment WHERE payment.amount >= 1
            Payments.deleteWhere { amount greaterEq BigDecimal.ONE }

            // DROP TABLE IF EXISTS payment
            SchemaUtils.drop(Payments)
        }
    }
}
```

`Payments`객체에 테이블 정보를 작성 하여 테이블을 생성하여 Insert, Select, Update, Delete를 하고 테이블을 Drop 합니다. SQL를 매핑한 DSL 방식으로 코틀린 코드 베이스로 SQL을 조작할 수 있습니다.

## DAO 방식

```kotlin
class Payment(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Payment>(Payments)
    var amount by Payments.amount
    var orderId by Payments.orderId
}

class ExposedGettingStarted {
    @Test
    fun `exposed DAO`() {
        // connection to MySQL
        Database.connect(dataSource)

        transaction {
            // Show SQL logging
            addLogger(StdOutSqlLogger)

            // CREATE TABLE IF NOT EXISTS payment (id BIGINT AUTO_INCREMENT PRIMARY KEY, order_id BIGINT NOT NULL, amount DECIMAL(19, 4) NOT NULL)
            SchemaUtils.create(Payments)

            // INSERT INTO payment (amount, order_id) VALUES (1, 1)
            // ...
            (1..20).map {
                Payment.new {
                    amount = it.toBigDecimal()
                    orderId = it.toLong()
                }
            }

            // UPDATE payment SET amount=0 WHERE id = 1
            // ...
            Payment.all()
                    .forEach { it.amount = BigDecimal.ZERO }

            // SELECT payment.id, payment.order_id, payment.amount FROM payment WHERE payment.amount >= 1
            // Payment(amount=1.0000, orderId=1)
            Payment.find { amount eq BigDecimal.ONE }
                    .forEach { println(it) }

            // DELETE FROM payment WHERE payment.id = 1
            // ...
            Payment.all()
                    .forEach { it.delete() }

            // DROP TABLE IF EXISTS payment
            SchemaUtils.drop(Payments)
        }
    }
}
```

DSL 객체인 `Payments` 기반으로 DAO `Payment`를 객체를 생성합니다. DAO는 `payment` 테이블에 대한 Data Access Object의 기능을 전담하게 됩니다. Data JPA의 Repository와 비슷한 개념입니다.

## With Spring Boot

Exposed에서는 Spring Boot를 지원하는 [exposed-spring-boot-starter](https://github.com/JetBrains/Exposed/tree/master/exposed-spring-boot-starter) 의존성을 제공하고 있습니다.

### gradle
```gradle
implementation("org.jetbrains.exposed:exposed-spring-boot-starter:0.31.1")
```
스프링 부트에서 공식적으로 지원하는 의존성은 아니기 때문에 버전을 명확하게 명시해야 합니다.

### properties
```yml
spring:
    datasource:
        url: jdbc:mysql://localhost:3366/exposed_study?useSSL=false&serverTimezone=UTC&autoReconnect=true&rewriteBatchedStatements=true&logger=Slf4JLogger&profileSQL=false&maxQuerySizeToLog=100000
        username: root
        password:
        driver-class-name: com.mysql.cj.jdbc.Driver
    exposed:
        generate-ddl: true
        excluded-packages: com.example.exposedstudy

logging.level.Exposed: debug
```

`datasource`에 대한 설정을 스프링 부트에서 사용하는 방식 그대로 사용할 수 있고 `generate-ddl` 설정이 활성화되어 있는 경우 데이터베이스 스키마를 생성하고, 특정 스키마를 제외하고 싶은 경우 `excluded-packages` 설정으로 제외할 수 있습니다. 실제 코드를 보면 매우 단순합니다. `logging.level.Exposed: debug` 경우 별도의 설정 없이 Show SQL Log를 볼 수 있습니다.

```kotlin
@Configuration
@AutoConfigureAfter(DataSourceAutoConfiguration::class)
@EnableTransactionManagement
open class ExposedAutoConfiguration(private val applicationContext: ApplicationContext) {

    @Value("\${spring.exposed.excluded-packages:}#{T(java.util.Collections).emptyList()}")
    private lateinit var excludedPackages: List<String>

    @Bean
    open fun springTransactionManager(datasource: DataSource) = SpringTransactionManager(datasource)

    @Bean
    @ConditionalOnProperty("spring.exposed.generate-ddl", havingValue = "true", matchIfMissing = false)
    open fun databaseInitializer() = DatabaseInitializer(applicationContext, excludedPackages)
}

open class DatabaseInitializer(private val applicationContext: ApplicationContext, private val excludedPackages: List<String>) : ApplicationRunner, Ordered {
    override fun getOrder(): Int = DATABASE_INITIALIZER_ORDER

    companion object {
        const val DATABASE_INITIALIZER_ORDER = 0
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun run(args: ApplicationArguments?) {
        val exposedTables = discoverExposedTables(applicationContext, excludedPackages)
        logger.info("Schema generation for tables '{}'", exposedTables.map { it.tableName })

        logger.info("ddl {}", exposedTables.map { it.ddl }.joinToString())
        SchemaUtils.create(*exposedTables.toTypedArray())
    }
}

fun discoverExposedTables(applicationContext: ApplicationContext, excludedPackages: List<String>): List<Table> {
    val provider = ClassPathScanningCandidateComponentProvider(false)
    provider.addIncludeFilter(AssignableTypeFilter(Table::class.java))
    excludedPackages.forEach { provider.addExcludeFilter(RegexPatternTypeFilter(Pattern.compile(it.replace(".", "\\.") + ".*"))) }
    val packages = AutoConfigurationPackages.get(applicationContext)
    val components = packages.map { provider.findCandidateComponents(it) }.flatten()
    return components.map { Class.forName(it.beanClassName).kotlin.objectInstance as Table }
}
```

스프링 표현식으로 제외 시킬 `excludedPackages`를 List로 받고, `generate-ddl` 여부에 따라 `DatabaseInitializer` 빈을 등록여부를 결정합니다. 만약 해당 빈을 등록하게 되면 `DatabaseInitializer` 객체가 `ApplicationRunner`을 구현하고 있기 때문에 스프링 어플리케이션이 실행하는 경우 `run()` 메서드에서 스키마를 생성하게 됩니다. (`excludedPackages`는 제외)

**`run()` 메서드에 `@Transactional` 어노테이션이 있는 것을 볼 수 있습니다. 이것은 `spring-transaction`모듈을 통해서 `TransactionSynchronizationManager`를 기반으로 스프링의 트랜잭션 메커니즘을 그대로 사용할 수 있다는 의미 입니다.**

> 스프링의 트랜잭션 동기화 메커니즘
> ![](https://raw.githubusercontent.com/cheese10yun/TIL/master/assets/TransactionSynchronizations.png)
> [토비의 스프링 3.1, 361 페이지](http://m.yes24.com/goods/detail/7516911)
> 스프링은 위와 같은 방식으로 트랜잭션 동기화를 진행합니다. 해당 방식은 트랜잭션을 시작하기 위해 만든 Connection 오브젝트를 특별한 저장소에 보관해두고, 이후에 호출되는 메서드에서 저장된 Connection을 가져다가 사용합니다.

## Testing in Spring Boot

```kotlin
@SpringBootTest
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ActiveProfiles("test")
open class ExposedTestSupport

class ExposedGettingStartedInSpringBoot : ExposedTestSupport() {

    @Test
    fun `exposed DAO`() {
        // connection to MySQL
        // Database.connect(dataSource) 스프링 Bean의 DataSource를 사용하기 때문에 주석

        // transaction { 스프링 @Transactional 으로 트랜잭션을 시작하기 때문에 주석
            // Show SQL logging
            // addLogger(StdOutSqlLogger)  logging.level.Exposed: debug 으로 Show SQL logging 확인

            // CREATE TABLE IF NOT EXISTS payment (id BIGINT AUTO_INCREMENT PRIMARY KEY, order_id BIGINT NOT NULL, amount DECIMAL(19, 4) NOT NULL)
            //  SchemaUtils.create(Payments)  generate-ddl: true 으로 스키마 생성

            // INSERT INTO payment (amount, order_id) VALUES (1, 1)
            // ...
            (1..20).map {
                Payment.new {
                    amount = it.toBigDecimal()
                    orderId = it.toLong()
                }
            }

            // UPDATE payment SET amount=0 WHERE id = 1
            // ...
            Payment.all()
                    .forEach { it.amount = BigDecimal.ZERO }

            // SELECT payment.id, payment.order_id, payment.amount FROM payment WHERE payment.amount >= 1
            // Payment(amount=1.0000, orderId=1)
            Payment.find { amount eq BigDecimal.ONE }
                    .forEach { println(it) }

            // DELETE FROM payment WHERE payment.id = 1
            // ...
            Payment.all()
                    .forEach { it.delete() }

            // DROP TABLE IF EXISTS payment
            // SchemaUtils.drop(Payments)
        // }
    }

    @Test
    fun `exposed DSL`() {
        // connection to MySQL
        // Database.connect(dataSource) 스프링 Bean의 DataSource를 사용하기 때문에 주석

        // transaction { 스프링 @Transactional 으로 트랜잭션을 시작하기 때문에 주석
            // Show SQL logging
            // addLogger(StdOutSqlLogger)  logging.level.Exposed: debug 으로 Show SQL logging 확인

            // CREATE TABLE IF NOT EXISTS payment (id BIGINT AUTO_INCREMENT PRIMARY KEY, order_id BIGINT NOT NULL, amount DECIMAL(19, 4) NOT NULL)
            //  SchemaUtils.create(Payments)  generate-ddl: true 으로 스키마 생성

            // INSERT INTO payment (amount, order_id) VALUES (1, 1)
            // ...
            (1..5).map {
                Payments.insert { payments ->
                    payments[amount] = it.toBigDecimal()
                    payments[orderId] = it.toLong()
                }
            }

            // UPDATE payment SET amount=0 WHERE payment.amount >= 0
            Payments.update({ amount greaterEq BigDecimal.ZERO })
            {
                it[amount] = BigDecimal.ZERO
            }

            // SELECT payment.id, payment.order_id, payment.amount FROM payment WHERE payment.amount = 0
            // Payment(amount=1.0000, orderId=1)
            Payments.select { amount eq BigDecimal.ZERO }
                    .forEach { println(it) }

            // DELETE FROM payment WHERE payment.amount >= 1
            Payments.deleteWhere { amount greaterEq BigDecimal.ONE }

            // DROP TABLE IF EXISTS payment
            // SchemaUtils.drop(Payments)
        // }
    }
}
```

`DataSource`는 스프링 Bean을 사용하기 때문에 제거했으며, `transaction { ... }`으로 트랜잭션을 시작했던 코드를 스프링의 `@Transactional`으로 대체했습니다. 또한 `SchemaUtils.create(Payments)`으로 스키마를 생성했던 부분을 `generate-ddl: true` 속성 파일로 대체했습니다. 그리고 `ExposedTestSupport` 객체에 `@Transactional`가 있어 테스트 코드의 최종 데이터는 모두 Rollback을 진행하게 됩니다. **Spring 환경에서 Exposed를 사용하게 되면 보다 간결하게 사용 가능합니다.**

## 심화

### batch insert

```kotlin
@Test
fun `batch insert`() {
    val data = (1..10).map { it }
    Books.batchInsert(
            data,
            ignore = false,
            shouldReturnGeneratedValues = false
    ) {
        this[Books.writer] = 1L
        this[Books.title] = "$it-title"
        this[Books.price] = it.toBigDecimal()
        this[Books.createdAt] = LocalDateTime.now()
        this[Books.updatedAt] = LocalDateTime.now()
    }
}
```

`Exposed`는 `batchInsert()` 메서드를 지원하기 때문에 쉽게 batch insert를 진행할 수 있습니다. Mysql의 경우 JDBC 드라이버에 `rewriteBatchedStatements=true` 속성을 반드시 입력해야 batch insert가 가능합니다. `shouldReturnGeneratedValues` 값을 false로 지정하면 `auto_increment`으로 증가된 ID 값을 가져오지 않기에 성능이 향상될 수 있습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/62ba5c8fd643af1a9ab8c8bea88043d397591447/exposed-study/docs/images/batch-insert-logger.png)

로그에 출력되는 SQL은 batch insert가 진행되지 않고 개별 insert로 출력 됩니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/62ba5c8fd643af1a9ab8c8bea88043d397591447/exposed-study/docs/images/mysql-log.png)
**하지만 실제 데이터베이스의 로그를 확인해보면 batch insert가 정상적으로 동작하는 것을 확인할 수 있습니다.**

> SQL Log 확인 방법
> `show variables like 'general_log%';` 확인 해서 `general_log`가 OFF인 경우
> `set global general_log = 'ON';` 설정 이후 `general_log_file` 경로에 로그 파일 확인

| Variable\_name | Value |
| :--- | :--- |
| general\_log | OFF |
| general\_log\_file | /var/lib/mysql/2eb41ec6a5fe.log |

### 연관 관계

```kotlin
object Books : LongIdTable("book") {
    val writer = reference("writer_id", Writers)
    val title = varchar("title", 150)
    val price = decimal("price", 10, 4)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object Writers : LongIdTable("writer") {
    val name = varchar("name", 150)
    val email = varchar("email", 150)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}
```
reference을 통해서 객체의 연관 관계를 참조할 수 있게 할 수 있습니다.

### Join

```kotlin
@Test
fun `join`() {
    val writerId = insertWriter("yun", "yun@asd.com")[Writers.id].value
    (1..5).map {
        insertBook("$it-title", BigDecimal.TEN, writerId)
    }

    // SELECT book.id, book.title, book.price, writer.`name`, writer.email FROM book INNER JOIN writer ON writer.id = book.writer_id
    (Books innerJoin Writers)
            .slice(
                    Books.id,
                    Books.title,
                    Books.price,
                    Writers.name,
                    Writers.email,
            )
            .selectAll()
            .forEach {
                it.fieldIndex
                println("bookId: ${it[Books.id]}, title: ${it[Books.title]}, writerName: ${it[Writers.name]}, writerEmail: ${it[Writers.email]}")
            }
}

```
DSL 기반으로 조인 SQL로 쉽게 작성할 수 있습니다.

## 참고

* [Exposed Wiki](https://github.com/JetBrains/Exposed/wiki)
* [토비의 스프링 3.1, 361 페이지](http://m.yes24.com/goods/detail/7516911)