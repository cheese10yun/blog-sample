> [코틀린 쿡북](http://www.kyobobook.co.kr/product/detailViewKor.laf?ejkGb=KOR&mallGb=KOR&barcode=9791189909147) 을보고 정리한 내용입니다.


# 1장 코틀린 설치와 실행

## 코틀린 DSL

```gradle
plugins {
	kotlin("jvm") version "1.3.72"
}
```
그레이들 5.0 이상 버전에서는 코틀린 DSL을 사용할 수 있고, 그레이들 5.0 이상 버전에서는 그레이들 플러그인 리포지토리(https://plugins.gradle.org)에 등록되어 있는 `kotlin-gradle-plugin` 사용 가능하다. **최신 문법을 사용 `repositories` 블록 처럼 플러그인을 찾아야할 장소 언급을 할 필요가 없다. 이는 그레이들 플러그인 저장소에 등록된 모든 그레이들 플러그인에 해당하는 사항이다. 또한 plugins 블록을 사용하면 자동으로 플러그인이 적용되기 때문에 apply 문도 사용할 필요가 없다.**

> 그레이들에서 코틀린 DSL을 사용하는 기본 빌드 파일 이름은 settings.gradle.kts, build.gradle.kts 이다.

그루비 DSL과 가장 큰 차이점은 다음과 같다.

* 모든 문자열에 큰따움 표를 사용한다.
* 코틀린 DSL에서는 괄호 사용이 필수다.
* 코틀린은 콜론(:) 대신 등호 기호 (=)를 사용해 값을 할당한다.


`settings.gradle.kts` 파일 사용을 권장하지만 필수는 아니다. 그레이들 빌드 과정의 초기화 단계에서 그레리들이 어떤 프로젝트 빌드 파일을 분석해야 하는지 결정하는 순간에 `settings.gradle.kts`가 처리된다. **멀티프로젝트 빌드에서 루트의 어떤 하위 디렉토리가 그레이들 프로젝트인지에 대한 정보가 `settings.gradle.kts` 파일에 담겨있다. 그레이들은 설정 정보와 의존성을 하위 프로젝트 사이에서 공유할 수 있고. 하위 프로젝트가 다른 하위 프로젝트를 의존할 수 있드며, 심지어 하위 프로젝트를 병렬로 빌드할 수도 있다.**

## 그레이들 사용해 코틀린 프로젝트 빌드하기

```gradle
plugins {
    `java-libray` // (1)
	kotlin("jvm") version "1.3.72" // (2)
}

repositoies {
    jcentet()
}

dependencies {
    implementations(kotlin("stdlib")) // (3)
}
```
* (1): 자바 라이브러리 플러그인에서 그레이들 작업을 추가
* (2): 코틀린 플러그인을 그레이들에 추가
* (3): 코틀린 표준 라이브러리를 프로젝트 컴파일 타임에 추


# 2장 코틀린 기초

## 코틀린에서 널 허용 타입 사용하기

```kotlin
internal class KotlinTest {

    data class Person(
            val first: String,
            val middle: String?,
            val last: String
    )

    @Test
    internal fun `안전 호출 연산자와 엘비스 연산`() {
        val person = Person("first", null, "last")
        val middleLength = person.middle?.length ?: 0
        println(middleLength)
    }
}
```

* `?.`(안전한 호출 연산자) 은 null 검사와 메서드 호출을 한 번의 연산으로 수행한다. 호출하려는 값이 null이 아니라면 `?.`은 일반 메서드 처럼 작동한다. 호출하려는 값이 null이면 이 호출은 무시되고 null이 결과 값이 된다.
* `?:`(엘비스 연산자) null 대신 사용할 디폴트 값을 지정할 때 편리하게 사용할 수있는 연산자를 제공한다.
*  middle이 널일 경우 엘비스 연산자는 0을 리턴한다.


## to로 Pair 인스턴스 생성하기
코틀린은 Pair 인스턴스의 리스토부터 맵을 생성하는 mapOf와 같은 맵 생성을 위한 최상위 함수를 몇가지 제공한다. 코틀린 표준 라이브러리에 있는 mapOf 함수의 시그니처는 다음과 같다.

`fun <K, V> mapOf(vararg pairs: Pair<K, V>): Map<K, V>`

Pair는 first와 second라는 이름의 두 개의 원소를 갖는 데이터 클래스다. Pair 클래스의 시그니처는 다음과 같다.

`data class Pair<out A, out B>: Seriallizable`

Pair 클래스의 first, second 속성은 A, B의 제네릭 값에 해당한다. 2개의 인자를 받는 생성자를 사용해서 Pair 클래스를 생성할 수 있지만 to 함수를 사용하는것이 더 일반적이다. 


```kotlin
@Test
internal fun `create map using to function`() {
    val mapOf = mapOf("a" to 1, "b" to 2, "c" to 2)

    then(mapOf).anySatisfy { key, value ->
        then(key).isIn("a", "b", "c")
        then(value).isIn(1, 2)
    }
}
```

# 3장 코틀린 객체 지향 프로그래밍

객체 초기화. 사용자 정의 getter, setter, late initialization, lazy initialization, 싱글톤 객체 생성, Nothing 클래스 등을 살펴 본다.


## const, val 차이 이해하기

컴파일 타임 상수에 const 변경자를 사용한다. val 키워드는 변수에 한 번 할당되면 변경이 불가능함을 나타내지만 이러한 할당은 실행 시간에 일어난다. 

코틀린 `val`은 값이 변경 불가능한 변수임을 나타낸다. 자바에서는 `final` 키워다가 같은 목적으로 사용된다. 그렇다면 코틀린에서 `const` 변경자도 지워하는 이유가 뭘까?
**컴파일 타임 상수는 반드시 객체나, `companion object` 선언의 최상위 속성 또는 멤버여야한다. 컴파일 타임 상수는 문자열 또는 기본 타입의 레퍼 클래스(Byte, Short, Int, Long, Float, Double, Char, Boolean)이며, getter를 가질 수 없다. 컴파일 타임 상수는 컴파일 시점에 값을 사용할 수있도록 main 함수를 포함한 모든 함수 바깥쪽에 할당돼야 한다.**


```kotlin
class Task(val name: String, _priority: Int = DEFAULT_PRIORITY) {

    companion object {
        const val MIN_PRIORITY = 1 // (1)
        const val MAX_PRIORITY = 5 // (1)
        const val DEFAULT_PRIORITY = 3 // (1)
    }

    var priority = validPriority(_priority) // (2)
        set(value) {
            field = validPriority(value)
        }

    private fun validPriority(p: Int) = p.coerceIn(MIN_PRIORITY, MAX_PRIORITY) // (3)
}
```

* (1): 컴파일 타임 상수
* (2): 사용자 정의  setter를 사용하는 속성
* (3): private 검증 함수

## 사용자 정의 getter, setter 생성하기

다른 객체지향 언어처럼 코틀린 클래스는 데이터와 보통 캡슐화ㅓ로 알려진 해당 데이터를 조작하는 함수로 이뤄진다. 코틀린은 특이하게도 모든 것이 기본적으로 public이다. 따라서 정보와 연관된 데이터 구조의 세부 구현이 필요하다고 추정되며 이는 데이터 은닉 철학을 침해하는 것처럼 보인다. 코틀린은 이러한 딜레마를 특이한 방법으로 해결한다. 코틀린 클래스에서는 필드는 직업 선언할 수 없다.

```kotlin
class Task(val name: String) {
    var priority = 3
}
```

Task 클래스는 name, priority라는 두 가지 속성을 정의한다. 속성 하나는 주 생성자 안에 선언된 반면 다른 속성은 클래스의 최상위 멤버로 선언되었다. 이 방식으로 priority값을 할당할 수 있지만 클래스를 인스턴스할 떄 priority에 값을 할당할 수 없다는 것이다.

```kotlin
var priority = 3
    set(value) {
        field = value
    }

val isLowPriority
    get() = priority < 3


```
위 처럼 파생 속성을 위한 getter, setter 메서드를 정의할 수 있다.

## Lazy 기법

```kotlin
class Customer(val name: String) {
//    val message: List<String> = loadMessage()
    val message: List<String> by lazy { loadMessage() }
    private fun loadMessage(): List<String> {
        return listOf("1", "2", "3")
    }
}

class CustomerTest {

    @Test
    internal fun `none lazy, 객체 생성 시점에 loadMessage를 호출한다`() {
        // val message: List<String> = loadMessage()
        val customer = Customer("yun")
        customer.message
        println(customer)
    }
    
    @Test
    internal fun `lazy, 객체 생성 시점에 loadMessage를 호출하지 않고, 조회 시점까지 lazy하게 간다`() {
        // val message: List<String> = loadMessage()
        val customer = Customer("yun")
        customer.message
        println(customer)
    }
}
```
* `lazy` 키워드를 통해서 원하는 시점에 데이터를 조회할 수 있다.

## lateinit

생성자에 속성 초기화를 위한 정보가 충분하지 않으면 해당 속성으로 만들고 싶을 경우 속성에 `lateinit` 키워드를 사용할 수 있다.

```kotlin
@EntityListeners(value = [AuditingEntityListener::class])
@MappedSuperclass
abstract class AuditingEntity : AuditingEntityId() {

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime
        protected set

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime
        protected set
}
```
프레임 워크를 사용하다 보면 인스턴스가 이미 생성된 후에 결정되는 값들이 있다 이런 경우 `lateinit`를 사용하면 된다. 만약 초기화가 이루어지지 않았다면 not null 항목이기 때문에 해당 항목을 조회할 때 예외가 발생한다

## lateinit, lazy의 차이

lateinit 변경자는 var 속성에 사용된다. lazy 대리자는 속성에 처음 접글할 때 평가되는 람다를 받는다.

## 싱글톤 생성하기
 
싱글톤 디자인 패턴은 특정 클래스의 인스턴스를 오직 하나만 존재하도록 메커니즘을 정의하는 것이다.

1. 클래스의 모든 생성자를 private를 정의한다.
2. 필요하다면 해당 클래스를 인스턴스화 하고 그 인스턴스 레퍼런스를 리톤하는 정적 팩토리 메서드를 제공한다.


```kotlin
object Singleton {
    val myPriority = 3

    fun function() = "hello"
}
```
클래스 하나당 인스턴스를 딱 하나만 존재하게 만들고 싶은 경우 class 대신 object 키워드를 사용한다.

```java
public final class Singleton {
   private static final int myPriority = 3;
   public static final Singleton INSTANCE; //(1)

   public final int getMyPriority() {
      return myPriority;
   }

   @NotNull
   public final String function() {
      return "hello";
   }

   private Singleton() { // //(2)
   }

   static {
      Singleton var0 = new Singleton(); //(3)
      INSTANCE = var0;
      myPriority = 3;
   }
}
```
생성된 바이트코드를 디컴파일 하면 다음과 같은 결과가 나온다.

* (1): INSTANCE 속성 생ㄹ성
* (2): private 생성자
* (3): 싱글톤의 열성적인 인스턴스화

## Nothing

절대 리턴하지 않는 함수에 Nothing을 사용한다.

```kotlin
package kotlin

public class Nothing prifvate constructor()

```

private 생성자는 클래스 밖에서 인스턴스화할 수 없다느 ㄴ것을 의미하고, 클래스 안쪽에서도 인스턴스화하지 않는다. 따라서 Nothing의 인스턴스는 존재하지 않는다. 코틀린 공식문서에서는 `결코 존재할 수 없는 값을 나타내기 위해 Nothing을 사용할 수 있다`고 명시되어 있다.

```kotlin
fun doNothing(): Nothing = throw Exceotion("Nothing at all")
```
리턴 타입을 반드시 구체적으로 명시해야 하는데 해당 메서드는 결코 리턴하지 않으므로 리턴 타입은 Nothing이다.

```kotlin
val x = null
```

구체적인 타입 없이 변수에 널을 할당하는 경우 컴파일러는 x에 대한 다른 정보가 없기 떄문에 추론된 x의 타입은 Nothing? 이다. **더 중요한 사실은 코틀린에서 Nothing 클래스는 실제로 다른 모든 타입의 하위 타입이라는 것이다.**

Nothing 클래스가 다른 모든 타입의 하위 타윕어아야 하는 이유는 다음과 같다

```kotlin
val x = if (Random.nextBoolean()) "true" else throw Exception("nope")
```

x의 투론 타입은 `Random.nextBoolean()` 함수가 생성하는 불리언이 참인경우 문자열에 따라서 달라진다. 또는 심지어 Any일 수도 있다. 이 코드는 else 절은 Nothing과 할당되는 문자열에 따른 타입에 수행하고 최종 리턴타입은 Nothing이 아닌 다른 타입이 된다.