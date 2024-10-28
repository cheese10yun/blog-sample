Kotlin에는 `invoke` 외에도 유사하게 사용되는 기능들이 많습니다. 이들은 코드를 더 직관적이고 간결하게 만드는 데 도움을 주며, Kotlin의 확장성과 유연성을 잘 보여주는 기능들입니다.

### 1. **`get`과 `set` 연산자 함수**
- `get`과 `set` 연산자 함수는 인덱스 접근 연산자(`[]`)를 지원합니다. 이를 통해 클래스 인스턴스의 특정 요소에 접근하거나 설정할 때 배열처럼 사용할 수 있습니다.
- 예시:
  ```kotlin
  class MyList {
      private val items = mutableListOf<String>()
      
      operator fun get(index: Int): String {
          return items[index]
      }
      
      operator fun set(index: Int, value: String) {
          items[index] = value
      }
  }

  fun main() {
      val list = MyList()
      list[0] = "Hello"   // set 호출
      println(list[0])    // get 호출
  }
  ```

### 2. **`plus`와 `minus` 연산자 함수**
- `plus`와 `minus` 연산자는 `+`와 `-` 연산을 지원합니다. 이를 통해 컬렉션에 요소를 추가하거나 삭제하는 동작을 구현할 수 있습니다.
- 예시:
  ```kotlin
  data class Vector(val x: Int, val y: Int) {
      operator fun plus(other: Vector): Vector {
          return Vector(x + other.x, y + other.y)
      }
  }

  fun main() {
      val v1 = Vector(1, 2)
      val v2 = Vector(3, 4)
      println(v1 + v2) // plus 호출, 결과: Vector(x=4, y=6)
  }
  ```

### 3. **`contains` 연산자 함수**
- `contains` 함수는 `in` 키워드를 사용할 수 있도록 합니다. 이를 통해 특정 요소가 컬렉션에 포함되어 있는지를 확인할 수 있습니다.
- 예시:
  ```kotlin
  class MyCollection(val elements: List<Int>) {
      operator fun contains(value: Int): Boolean {
          return elements.contains(value)
      }
  }

  fun main() {
      val collection = MyCollection(listOf(1, 2, 3))
      println(2 in collection) // contains 호출, 결과: true
  }
  ```

### 4. **`compareTo` 연산자 함수**
- `compareTo` 함수는 `Comparable` 인터페이스의 기능을 확장하여 클래스 간의 크기 비교를 지원합니다. 이를 통해 `>`, `<`, `>=`, `<=` 등의 비교 연산자를 사용할 수 있습니다.
- 예시:
  ```kotlin
  class Person(val age: Int) {
      operator fun compareTo(other: Person): Int {
          return age - other.age
      }
  }

  fun main() {
      val person1 = Person(30)
      val person2 = Person(25)
      println(person1 > person2) // compareTo 호출, 결과: true
  }
  ```

### 5. **`unaryPlus`와 `unaryMinus` 연산자 함수**
- `unaryPlus`와 `unaryMinus`는 각각 `+`와 `-` 단항 연산자를 지원합니다. 이를 통해 객체에 대한 양수, 음수의 의미를 정의할 수 있습니다.
- 예시:
  ```kotlin
  data class Temperature(val degree: Int) {
      operator fun unaryMinus(): Temperature {
          return Temperature(-degree)
      }
  }

  fun main() {
      val temp = Temperature(10)
      println(-temp) // unaryMinus 호출, 결과: Temperature(degree=-10)
  }
  ```

### 6. **`rangeTo` 연산자 함수**
- `rangeTo`는 `..` 연산자를 지원해 범위를 정의할 수 있습니다. 예를 들어, `a..b`와 같은 표현이 `rangeTo` 함수로 변환됩니다.
- 예시:
  ```kotlin
  class Point(val x: Int) {
      operator fun rangeTo(other: Point): IntRange {
          return x..other.x
      }
  }

  fun main() {
      val start = Point(1)
      val end = Point(5)
      for (i in start..end) {
          println(i) // 1부터 5까지 출력
      }
  }
  ```

이처럼 Kotlin에서는 다양한 연산자 함수를 통해 특정 클래스가 마치 기본 데이터 타입처럼 사용할 수 있도록 연산자를 오버로딩할 수 있습니다. 이를 활용하면 더 읽기 쉽고 간결한 코드를 작성할 수 있습니다.