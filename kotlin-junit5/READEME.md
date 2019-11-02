# Junit5 With Kotlin

Spring boot 2.2 버전부터는 Junit5 디펜던시를 기본으로 포함하고 있습니다.

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
enum에 정의된 모든 값들을 출력하는 것을 확인 할 수 있습니다. `@EnumSource`을 사용하면 모든 enum을 iterator 하기 편리합니다.   

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

`@MethodSource` 어노테이션을 통해서 복잡한 객체를 보다 쉽게 생성하고 테스트 할 수 있습니다. 

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
`@MethodSource()`에 입력하는 문자열과,  값을 지정하는 static 메서드명과 일치해야 합니다. 테스트 하고자 하는 객체와, 예상되는 값을 넘겨 받아 다양한 객체의 경우를 쉽게 테스트 할 수 있습니다. 

## 참고
* [Guide to JUnit 5 Parameterized Tests](https://www.baeldung.com/parameterized-tests-junit-5)