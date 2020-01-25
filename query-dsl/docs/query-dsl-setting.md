# Kotlin Query DSL setting

## build.gradle.kts

```groovy
plugins {
    kotlin("kapt") version "1.3.61" // query dsl plugin 푸가
}


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.querydsl:querydsl-jpa") // query dsl 의존성 추가
    runtimeOnly("com.h2database:h2")
    annotationProcessor(group = "com.querydsl", name = "querydsl-apt", classifier = "jpa") // query dsl 의존성 추가
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    kapt("com.querydsl:querydsl-apt:4.2.1:jpa") // query dsl 의존성 추가
}

```
## Setting Test

```kotlin
@Entity
@Table(name = "heelo")
data class Hello(
        @Column(name = "name", nullable = false)
        val name: String

) : EntityAuditing() 
```
Entity 정의

```kotlin
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
class QueryDslApplicationTests(
        private val em: EntityManager
) {

    @Test
    internal fun `querydsl setting test`() {
        val hello = Hello("yun")
        em.persist(hello)

        val query = JPAQueryFactory(em)

        val qHello = QHello.hello

        val findHello = query
                .selectFrom(qHello)
                .where(qHello.name.eq("yun"))
                .fetchOne()!!

        then(findHello.id).isNotNull()
        then(findHello.name).isEqualTo("yun")
    }
}
```
query dsl 기반으로 select test

```
2020-01-26 00:32:40.535  INFO 51870 --- [    Test worker] o.s.t.c.transaction.TransactionContext   : Began transaction (1) for test context [DefaultTestContext@5d303607 testClass = QueryDslApplicationTests, testInstance = com.example.querydsl.QueryDslApplicationTests@7f74e09a, testMethod = querydsl setting test$query_dsl@QueryDslApplicationTests, testException = [null], mergedContextConfiguration = [WebMergedContextConfiguration@3dc6159c testClass = QueryDslApplicationTests, locations = '{}', classes = '{class com.example.querydsl.QueryDslApplication}', contextInitializerClasses = '[]', activeProfiles = '{}', propertySourceLocations = '{}', propertySourceProperties = '{org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true}', contextCustomizers = set[org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@0, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizerFactory$Customizer@11f1eb41, org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@e55f707, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@5b2f0115, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@0, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@4bb6d362], resourceBasePath = 'src/main/webapp', contextLoader = 'org.springframework.boot.test.context.SpringBootContextLoader', parent = [null]], attributes = map['org.springframework.test.context.web.ServletTestExecutionListener.activateListener' -> true, 'org.springframework.test.context.web.ServletTestExecutionListener.populatedRequestContextHolder' -> true, 'org.springframework.test.context.web.ServletTestExecutionListener.resetRequestContextHolder' -> true]]; transaction manager [org.springframework.orm.jpa.JpaTransactionManager@751e73c5]; rollback [true]
Hibernate: insert into heelo (id, created_at, updated_at, name) values (null, ?, ?, ?)
2020-01-26 00:32:40.598 TRACE 51870 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [1] as [TIMESTAMP] - [2020-01-26T00:32:40.593]
2020-01-26 00:32:40.601 TRACE 51870 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [2] as [TIMESTAMP] - [2020-01-26T00:32:40.593]
2020-01-26 00:32:40.602 TRACE 51870 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [3] as [VARCHAR] - [yun]
Hibernate: select hello0_.id as id1_0_, hello0_.created_at as created_2_0_, hello0_.updated_at as updated_3_0_, hello0_.name as name4_0_ from heelo hello0_ where hello0_.name=?
2020-01-26 00:32:40.806 TRACE 51870 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [1] as [VARCHAR] - [yun]
2020-01-26 00:32:40.873  INFO 51870 --- [    Test worker] o.s.t.c.transaction.TransactionContext   : Rolled back transaction for test: [DefaultTestContext@5d303607 testClass = QueryDslApplicationTests, testInstance = com.example.querydsl.QueryDslApplicationTests@7f74e09a, testMethod = querydsl setting test$query_dsl@QueryDslApplicationTests, testException = [null], mergedContextConfiguration = [WebMergedContextConfiguration@3dc6159c testClass = QueryDslApplicationTests, locations = '{}', classes = '{class com.example.querydsl.QueryDslApplication}', contextInitializerClasses = '[]', activeProfiles = '{}', propertySourceLocations = '{}', propertySourceProperties = '{org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true}', contextCustomizers = set[org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@0, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizerFactory$Customizer@11f1eb41, org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@e55f707, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@5b2f0115, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@0, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@4bb6d362], resourceBasePath = 'src/main/webapp', contextLoader = 'org.springframework.boot.test.context.SpringBootContextLoader', parent = [null]], attributes = map['org.springframework.test.context.web.ServletTestExecutionListener.activateListener' -> true, 'org.springframework.test.context.web.ServletTestExecutionListener.populatedRequestContextHolder' -> true, 'org.springframework.test.context.web.ServletTestExecutionListener.resetRequestContextHolder' -> true]]
```
query 정상동작 확인