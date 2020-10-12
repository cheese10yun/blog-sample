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

다른 객체지향 언어처럼 코틀린 클래스는 데이터와 보통 캡슐화로 알려진 해당 데이터를 조작하는 함수로 이뤄진다. 코틀린은 특이하게도 모든 것이 기본적으로 `public` 이다. 따라서 정보와 연관된 데이터 구조의 세부 구현이 필요하다고 추정되며 이는 데이터 은닉 철학을 침해하는 것처럼 보인다. 코틀린은 이러한 딜레마를 특이한 방법으로 해결한다. 코틀린 클래스에서는 필드는 직업 선언할 수 없다.

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

private 생성자는 클래스 밖에서 인스턴스화할 수 없다는 것을 의미하고, 클래스 안쪽에서도 인스턴스화하지 않는다. 따라서 Nothing의 인스턴스는 존재하지 않는다. 코틀린 공식문서에서는 `결코 존재할 수 없는 값을 나타내기 위해 Nothing을 사용할 수 있다`고 명시되어 있다.

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

# 4장 함수형 프로그래밍

함수형 프로그래밍이라는 용어는 불변성을 선호하고, 순수 함수를 사용하는 경우에 동시성을 쉽게 구현할 수 있으며, 반복보다는 변형을 사용하고, 조건문보다는 필터를 사용하는 코딩 스타일을 지창한다.


## 알고르짐으세 fold 사용하기

**fold 함수를 사용해서 시퀀스나 컬렉션을 하나의 값으로 축약(reduce) 시킨다.** 

```kotlin
internal class Fold {

    @Test
    internal fun name() {
        val numbers = intArrayOf(1, 2, 3, 4)
        val sum = sum(*numbers)
        println(sum) // 10
    }

    fun sum(vararg nums: Int) =
            nums.fold(0) { acc, n -> acc + n }
}
```
fold는 2개의 인자를 받는다, 첫 번쨰는 누적자의 초기값이며 두 번쨰는 두 개의 인지ㅏ를 받아 누적자를 위해 새로운 값을 리턴하는 함수이다.

## reduce 함수를 사용해 축약하기

비오 있지 않은 컬렉션의 값을 축약하고 싶지만 누적자의 초기값을 설정하고 싶지 않을 경우 reduce를 사용할 수 있다.

reduce 함수는 fold 함수랑 거의 같은데 사용 목적도 같다. reduce 함수에는 누적자의 초기값 인자가 없다는 것이 fold와 가장 큰 차이점이다.

```kotlin
@Test
    internal fun `reduce sum`() {
        val numbers = intArrayOf(1, 2, 3, 4)
        val sum = sumReduce(*numbers)
        // acc: 1, i: 2
        //   acc: 3, i: 3
        //   acc: 6, i: 4
    }


    fun sumReduce(vararg nums: Int) =
            nums.reduce { acc, i ->
                println("acc: $acc, i: $i")
                acc + i
            }
```

# 5장 컬렉

## 컬렉션에서 맵만들기
키 리스트가 있을 때 각가의 키와 새성한 값을 연관시켜 맵을 만들고 싶을경우 associateWith 함수에 각 키에 대해 실행되는 람다를 제공해서 사용할 수 있다.


```kotlin
@Test
internal fun associateWith() {
    val keys = 'a'..'f'
    val associate = keys.associate {
        it to it.toString().repeat(5).capitalize()
    }
    println(associate)
}
```

## 컬레기션이 빈 경우 기본값 리턴하기

컬렉션을 처리할 때 컬렉션의 모든 요소가 선택에서 제외되지만 기본 응답을 리턴하고 싶은경우 ifEmpty, ifBlank 함수를 사용해 기본 값을 리턴할 수 있다.

```kotlin
@Test
internal fun ifEmpty() {
    val products = listOf(Product("goods", 1000.0, false))
    val joinToString = products.filter { it.onSale }
            .map { it.name }
            .ifEmpty { listOf("none") }
            .joinToString(separator = ", ")

    println(joinToString) // none

}
```

# 7장 영역 함수


## apply로 객체 생성 후에 초기화하기

객체를 사용하기 전에 생성자 인자만으로는 할 수 없는 초기화 작업이 필요한 경우 apply를 사용할 수 있다.

## 부수효과를 위해 aslo 사용하기
코드 흐름을 방해하지 않고 메시지를 출력하ㅏㄱ나 다른 부수혀ㅛ과를 생성하고 싶은 경우 aslo 함수를 사용해 부수 효과를 생성하는 동작을 수행한다.


## let 함수와 엘비스 연산자 사용하기
오직 널이 아닌 레퍼런스의 코드 블록을 실행하고 싶지만 널이라면 기본 값을 리턴하고 싶은 경우 엘비시 연산자를 결합한 안전호출 안산자와 함께 let 여역 함수를 하용할 수 있다.

## 임시 분셔로 let 사용하기
연산 결과를 임시 변수에 할당하지 않고 처리하고 싶다.

# 8장 코틀린 대리자

클래스 대리자를 통해 상속을 합성으로 대체할 수 있고, 속성 대리자를 통해 어떤 속성의 getter, setter를 다른 클래스에 있는 속성의 getter, setter 대체랄 수 있다.

## 대리자를 사용해서 합성 구현하기

다른 클래스의 인스터늣가 포함된 클래스를 만들고, 그 클래스에 연산을 위임하고 싶은 경우 연산을 위임할 메서드가 포함된 인터페이스를 만들고, 클래스에서 해당 인터페이스를 구현한 다음 by 키워드를 사용해 바깥쪽에 래퍼 클래스를 만든다.

최신 객체 지향 디자인은 강한 결합 없이 기능을 추가할 때 상속보다는 합성을 선호한다. 코틀린에서 by 키워드는 포함된 객체에 있는 모든 public 함수를 이 객ㅊ-를 담고 있는 컨테이너를 통해 노출할 수 있다.

```kotlin
interface Dialable {
    fun dial(number: String): String
}

class Phone : Dialable {
    override fun dial(number: String): String = "Dialing $number"
}

interface Snappable {
    fun takePictrue(): String
}

class Camera : Snappable {
    override fun takePictrue() = "Taking Picture"
}

class SmartPhone(
        private val phone: Dialable = Phone(),
        private val camera: Snappable = Camera()
) : Dialable by phone, Snappable by camera

```
생성자에서 Phone, Camera를 인스턴스화하고 모든 public 함수를 Phone, Camera 인스턴스를 위임 하도록 `Dialable by phone, Snappable by camera` by 키워드를 사용했다. 


```kotlin
class SmartPhoneTest {

    @Test
    internal fun `dialing delegates to internal phone`() {
        val smartPhone = SmartPhone()
        val dial = smartPhone.dial("111")
        println(dial) // Dialing 111
    }

    @Test
    internal fun `Taking picture delegates to internal camera`() {
        val smartPhone = SmartPhone()
        val message = smartPhone.takePictrue()
        println(message) // Taking Picture
    }
}
```
**by 키워드를 통해서 위임 받은 함수를 호출할 수 있게 된다. 만약 by 키워드를 사용해서 위임하지 않았다면 Dialable, Snappable를 구현하고 있는 구현 클래스 SmartPhone에서 각 상위 클래스의 세부 구현을 진행해야한다. 이 구현을 by 키워드를 통해서 위임했다.**

## lazy 대리자 사용하기

어떤 속성이 필요할 때까지 헤당 속성의 초기화를 지연시키고 싶은경우 코틀린 표준 라이브러의 lazy 대리자를 사용할 수 있다.


## 대리자로서 Map 제공하기
어러 값이 들어 있는 맵을 제공해 객체를 초기화 하고 싶은 경우 코틀린 맵에서 대리자가 되는 데 필요한 getValue, setValue 함수를 구현 할 수 있다.

```kotlin
class Project(val map: MutableMap<String, Any>) {
    val name: String by map
    val priority: Int by map
    val completed: Boolean by map
}

class ProjectTest {

    @Test
    internal fun `use map delegate for project`() {
        val project = Project(
                mutableMapOf(
                        "name" to "Lean Kotlin",
                        "priority" to 5,
                        "completed" to true
                )
        )

        println(project)
        // Project(map={name=Lean Kotlin, priority=5, completed=true})
    }
}
```
Project 생성자는 MutableMap을 인자로 받아 해당 맵의 키에 해당하는 값으로 Project 클래스의 모든 속성을 초기화한다. 이 코드가 동작하는 이유는 MutableMap에 ReadWriteProperty 대리자가 되는 데 필요한 올바른 시그니처의 setValue, getValue 확장 함수가 있기 때문이다.

# 12장 스프링프레임워크

스프링에서 작성한 클래스를 확장하는 프록시를 설정해야한다. 하지만 코틀린 클래스는 기본적으로 final 이기 때문에 스프링에서 이를 자동으로 확장하기 위해서는 클래스를 open으로 변경해주는 스프링 플러그인을 빌드 파일에 추가해야 한다.

프록시와 실체는 둘다 같은 인터페이스를 구현하거나 같은 클래스를 확장한다. 들어 오는 요청을 프록시가 가로채고 이 프록시는 사비스가 요구하는 모든 것을 적용한 다음 실체(Real Object)로 요청을 전달한다. 프록시는 필요하다면 응답 또한 가러채서 더 많은 일을 한다. 예를 들어 스프링 트랜잭션 프로시는 어떤 메서드 호출을 가로챈 다음 트랜잭션을 시작하고, 해당 메서드를 호출하고, 실체 메서드 안에 일어난 상황에 맞춰 트랜잭션을 커밋하거나 롤백한다.

스프링은 시동 과정에서 프록시를 생성한다. 실체 클래스라면 해당 클래스를 확장하는 데 이 부분이 코틀린에서 문제가된다. 코틀린은 기본적으로 정적으로 결합한다. 즉 클래스가 open 키워드를 사용해 확장을 위한 open으로 표시되지 않으면 메서드 재정의 또는 클래스 확장이 불가능하다. 코틀린은 이런 문제를 `all-open` 플러그인으로 해결한다. 이 플러그인은 클래스와 클래스에 포함된 함수에 명시적으로 open 키워드를 추가하지 않고 명시적인 open 애노테이션으로 클래스를 설정한다.

`all-open` 플러그인도 유용하지만 좀 더 뛰어난 스프링에 꼭 맞는 `kotlin-spring` 플러그인을 사용하는게 더 좋다.

```gradle
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.3.4.RELEASE"
	id("io.spring.dependency-management") version "1.0.10.RELEASE"
	kotlin("jvm") version "1.3.72" // (1)
	kotlin("plugin.spring") version "1.3.72" // (2)
}

group = "com.kotlin"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect") // (3)
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8") // (3)
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict") //(4)
		jvmTarget = "1.8"
	}
}
``` 
* (1): 코틀린 JVM 플러긴을 프로젝트에 추가
* (2): 코틀린 스프링 프러그인을 요구
* (3): 소스 코드가 코틀린으로 작성된 경우 필요
* (4): JSR-305와 연관된 널허용 애노테이션 지원

`kotlin-spring` 프러그인은 다음의 스프링 애노테이션으로 클래스를 열도록 설정되어 있다.

* `@Component`, `@Configuration`, `@Controller`, `@RestController`, `@Service`, `@Repositroy`
* `@Async`
* `@Transactional`
* `@Cacheable`
* `@SpringBootTest`

## 코틀린 data 클래스로 퍼시턴스 구현하기

`kotlin-jpa` 플러그인을 빌드 파일에 추가하면 JPA를 쉽게 사용할 수 있다.

```kotlin
data class Person(
        val name: String,
        val dob: LocalDate
)
```
JPA 관점에서 data 클래스는 두 가지 문제가 있다.

1. JPA는 모든 속성에 기본값을 제공하지 않는 이상 기본 생성자가 필수지만 data 클래스에는 기본 생성자가 없다.
2. val 속성과 함께 data 클래스를 생성하면 불변 객체가 생성되는데, JPA는 불변 객체와 더불어 잘동작하도록 설계되어 있지 않다.

### 기본 생성자 문제
코틀린은 기본 생성자문제를 해결하기 위해서 2가지 플러그인을 제공한다. `no-arg` 플러그인은 인자가 없는 생성자를 추가할 클래스를 선택할 수 있고, 기본 생성자 추가를 호출하는 애노테이션을 정의할 수 있다. `no-arg` 플러그인은 코틀린 엔티티에 기본 생성자를 자동으로 구성한다.

```gradle
plugins {
	kotlin("plugin.jpa") version "1.3.72"
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
}
```

`kotlin-spring` 플러그인 처럼 빌드 파일에 필요한 문법을 추가해 `no-arg` 플러그인을 사용할 수 있다. 컴파일러 플러그인은 합성한 기본 연산자를 코트린 클래스에 추가한다. 즉 자바나 코트린에는 합성 기본 연산자를 호출 할 수 없다. 하지만 스프링에서는 리플렉션을 사용해 합성 기본 연산자를 호출할 수 있다.

`kotlin-jpa` 플러그인이 `no-arg` 플러그인보다 사용이 더 쉽다. `kotlin-jpa` 플러그인은 `no-arg` 플러그인을 기반으로 만들어졌다. `kotlin-jpa` 플러그인은 다음 애노테이션으로 자동 표시된 클래스에 기본 생성자를 추가한다.

* @Entity
* @Embeddable
* @MappedSuperClass

### JPA 엔티티에 불변 클래스 사용의 어려움
JPA가 엔ㅁ티티에 불변 클래스를 사용하고 싶지 않는다. 따라서 스프링 개발 팀은 엔티티로 사용하고 싶은 코틀린 클래스에 필드 값을 변경할 수 있게 속성을 var 타입으로 사용하는 단순 클래스를 추천한다.