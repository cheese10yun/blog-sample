# Junit5 With Kotlin

Spring boot 2.2 버전부터는 Junit5 디펜던시를 기본으로 포함하고 있습니다. Junit5 주요 테스트 어노테이션과 Spring boot에서 활용법을 정리해보았습니다. 

## @ValueSource

`@ValueSource`어노테이션을 사용하면 배열을 값을 테스트 메서드로 손쉽게 전달할 수 있습니다.

```kotlin
@ParameterizedTest
@ValueSource(strings = ["", " "])
internal fun `isBlank `(value: String) {
    print("value: $value ") // value:  value:
    assertThat(value.isBlank()).isTrue()
}

@ParameterizedTest
@ValueSource(ints = [1, 2, 3, 4])
internal fun `ints values`(value: Int) {
    print("value: $value ") // value: 1 value: 2 value: 3 value: 4
}
``` 
Int, String 이 이외에도 기본형 데이터 타입을 지원하고 있습니다.


## EnumSource

`@EnumSource` 어노테이션을 통해서 Enum을 효율적으로 테스트 할 수 있습니다. 

```kotlin
enum class Quarter(val value: Int, val description: String) {
    Q1(1, "1분기"),
    Q2(2, "2분기"),
    Q3(3, "3분기"),
    Q4(4, "4분기")
}
```
각 분기를 뜻하는 Enum을 위와 같이 정리했습니다.

```kotlin
@ParameterizedTest
@EnumSource(Quarter::class)
internal fun `분기의  value 값은 1 ~ 4 값이다`(quarter: Quarter) {
    println(quarter.name) // quarter: Q1 quarter: Q2 quarter: Q3 quarter: Q4
    assertThat(quarter.value in 1..4).isTrue()
}
```
enum에 정의된 모든 값들을 출력하는 것을 확인할 수 있습니다. `@EnumSource`을 사용하면 모든 enum을 iterator 하기 편리합니다.

```kotlin
@ParameterizedTest
@EnumSource(value = Quarter::class, names = ["Q1", "Q2"])
internal fun `names을 통해서 특정 enum 값만 가져올 수 있다`(quarter: Quarter) {
    print("${quarter.name} ") // quarter: Q1 quarter: Q2
    assertThat(quarter.value in 1..2).isTrue()
}
```
특정 enum을 지정해서 가져오고 싶은 경우 `names = ["Q1", "Q2"]`을 사용하면 됩니다.


## @CsvSource
`@CsvSource` 어노테이션을 통해서 CSV 포멧으로 테스팅을 편리하게 진행 할 수 있습니다.

```kotlin
@ParameterizedTest
@CsvSource(
        "010-1234-1234,01012341234",
        "010-2333-2333,01023332333",
        "02-223-1232,022231232"
)
internal fun `전화번호는 '-'를 제거한다`(value: String, expected: String) {
    val valueReplace = value.replace("-", "")
    assertThat(valueReplace).isEqualTo(expected)
}
```
`,` 단위로 테스트 메서드의 매개변수로 값을 넘길 수 있습니다.

## @MethodSource

`@MethodSource` 어노테이션을 통해서 복잡한 객체를 보다 쉽게 생성하고 테스트할 수 있습니다.

```kotlin
data class Amount(
        val price: Int,
        val ea: Int) {

    val totalPrice: Int
        get() = price * ea
}
```
가격과 수량을 입력하면 totalPrice 계산하는 단순한 객체 입니다. 해당 객체를 `@MethodSource`를 통해서 테스트를 진행해 보겠습니다.

```kotlin
@ParameterizedTest
@MethodSource("providerAmount")
internal fun `amount total price 테스트 `(amount: Amount, expectedTotalPrice: Int) {
    assertThat(amount.totalPrice).isEqualTo(expectedTotalPrice)
}

companion object {
    @JvmStatic
    fun providerAmount() = listOf(
            Arguments.of(Amount(1000, 2), 2000),
            Arguments.of(Amount(2000, 5), 10000),
            Arguments.of(Amount(4000, 5), 20000),
            Arguments.of(Amount(5000, 3), 15000)
    )
}
```
`@MethodSource()`에 입력하는 문자열과,  값을 지정하는 static 메서드명과 일치해야 합니다. 테스트 하고자 하는 객체와, 예상되는 값을 넘겨받아 다양한 객체의 경우를 쉽게 테스트할 수 있습니다.

## Spring Boot
Junit5 관련된 내용은 아니지만 Junit5와 Spring Boot 관련 테스트할때 좋은 패턴을 정리했습니다.

### 생성자 주입
`@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)` 어노테이션을 통해서 테스트 코드에서도 생성자 주입이 가능해 졌습니다.

```kotlin
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ActiveProfiles("test")
@DataJpaTest
internal class MemberRepositoryTest(val memberRepository: MemberRepository) {

    @Test
    internal fun `members 조회 테스트`() {
        //given
        val email = "asd@asd.com"
        val name = "name"

        //when
        val member = memberRepository.save(Member(email, name))

        //then
        assertThat(member.email).isEqualTo(email)
        assertThat(member.name).isEqualTo(name)
    }
}
``` 

### DSL 지원
```kotlin
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ActiveProfiles("test")
@AutoConfigureMockMvc
internal class MemberApiTest(
        val memberRepository: MemberRepository,
        val mockMvc: MockMvc
) {

    @Test
    internal fun `test`() {
        memberRepository.saveAll(listOf(
                Member("email1@asd.com", "jin"),
                Member("email2@asd.com", "yun"),
                Member("email3@asd.com", "wan"),
                Member("email4@asd.com", "kong"),
                Member("email5@asd.com", "joo")
        ))

        mockMvc.get("/members") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$[0].name") { value("jin") }
            jsonPath("$[1].name") { value("yun") }
            jsonPath("$[2].name") { value("wan") }
            jsonPath("$[3].name") { value("kong") }
            jsonPath("$[4].name") { value("joo") }
        }.andDo {
            print()
        }
    }
}
```
WebMvc에서도 DSL 사용을 할 수 있습니다. Web 관련 테스트 코드를 작성하기 더욱 편리해졌습니다.

### @Sql 손쉽게 Data Set up
`*.sql` 파일로 손쉽게 데이터를 Set up할 수 있습니다. JPA를 사용중이라면 Given절로 JPA를 만들기가 어렵고 불편한 경우 사용하면 좋을거 같습니다.


```sql
# member-data-setup.sql
insert into member (`email`, `name`, `created_at`, `updated_at`)
values
('sample1@asd.com', 'name', now(), now()),
('sample2@asd.com', 'name', now(), now()),
('sample3@asd.com', 'name', now(), now()),
('sample4@asd.com', 'name', now(), now()),
('sample5@asd.com', 'name', now(), now()),
('sample6@asd.com', 'name', now(), now()),
('sample15@asd.com', 'name', now(), now());
```

```
└── test
    ├── kotlin
    │   └── com
    └── resources
        └── member-data-setup.sql
```

위에서 작성한  `*.sql` 파일을 `test/resources` 디렉토리에 위치시킵니다

```kotlin
@Test
@Sql("/member-data-setup.sql")
internal fun name() {
    val members = memberRepository.findAll()

    then(members).anySatisfy {
        then(it.name).isEqualTo("name")
        then(it.email).contains("@")
                .startsWith("sample")
                .endsWith("com")
    }
}
```
`@Sql` 어노테이션을 통헤서 해당 디렉터리의 위치와 파일 이름을 작성합니다. 기본적인 디렉터리를 `test/resources`을 바라보기 때문에 위와 같은 경우 파일명만 작성합니다.

## AssertJ
Junit5의 관련된 내용은 아니지만 이번 Spring Boot 2.2 Release에서 AssertJ 관련된 내용이 있어 AssertJ의 사용과 간략한 팁을 정리했습니다.

`AssertJ`는 개인적으로 선호하는 Test Matcher입니다. static 메서드로 동작하기 때문에 자동 완성으로 Matcher 기능들을 손쉽게 사용할 수 있고, Matcher에서 지원해주는 기능도 막강합니다. AssertJ에서는 BDD 스타일의 BDDAssertion을 제공해주고 있습니다.


```kotlin
@Test
internal fun `member save test`() {
    //given
    val email = "asd@asd.com"
    val name = "name"

    //when
    val member = memberRepository.save(Member(email, name))

    //then
    
    // 기존 사용법 assertThat 
    assertThat(member.email).isEqualTo(email)
    assertThat(member.name).isEqualTo(name)
    assertThat(member.createdAt).isBeforeOrEqualTo(LocalDateTime.now())
    assertThat(member.updatedAt).isBeforeOrEqualTo(LocalDateTime.now())
    
    // BDD 사용법
    then(member.email).isEqualTo(email)
    then(member.name).isEqualTo(name)
    then(member.createdAt).isBeforeOrEqualTo(LocalDateTime.now())
    then(member.updatedAt).isBeforeOrEqualTo(LocalDateTime.now())
}
```
`assertThat` -> `then` 으로 대체되었습니다. 코드도 적어지고 더 직관적으로 되어서 좋아졌습니다.


```kotlin
@Test
internal fun `문장 검사`() {
    then("AssertJ is best matcher").isNotNull()
            .startsWith("AssertJ")
            .contains(" ")
            .endsWith("matcher")
}
```
위와 같은 형식으로 코드를 연결해서 테스트할 수도 있습니다.

```kotlin
@Test
internal fun `findByName test`() {
    //given
    memberRepository.saveAll(listOf(
            Member("email1@asd.com", "kim"),
            Member("email2@asd.com", "kim"),
            Member("email3@asd.com", "kim"),
            Member("email4@asd.com", "name"),
            Member("email5@asd.com", "name")
    ))

    //when
    val members = memberRepository.findByName("kim")

    //then
    then(members).anySatisfy {
        then(it.name).isEqualTo("kim")
    }
}
```

`anySatisfy` 람다 표현식으로 members를 iterator 돌리면서 해당 `kim`과 일치하는지 편리하게 확인할 수 있습니다. 이 밖에도 다양한 것들을 제공하고 있고 계속 발전하고 있는 AssertJ를 추천드립니다.

## 참고
* [Guide to JUnit 5 Parameterized Tests](https://www.baeldung.com/parameterized-tests-junit-5)
* [머루의개발블로그 : Spring 5.2 와 Spring boot 2.2 추가된 Test 기능들](http://wonwoo.ml/index.php/post/category/kotlin)
* [Spring Boot 2.2 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.2-Release-Notes)
* [Baeldung : Guide to JUnit 5 Parameterized Tests](https://www.baeldung.com/parameterized-tests-junit-5)