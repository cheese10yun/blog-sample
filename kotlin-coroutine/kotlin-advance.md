# [코틀린 고급편](https://www.inflearn.com/course/%EC%BD%94%ED%8B%80%EB%A6%B0-%EA%B3%A0%EA%B8%89%ED%8E%B8)

# 제네릭

## 1강 제네릭과 타입 파리미터

![](images/002.png)

```kotlin
class Cage {

    private val animals: MutableList<Animal> = mutableListOf()

    fun getFirst(): Animal {
        return animals.first()
    }

    fun put(animal: Animal) {
        this.animals.add(animal)
    }

    fun moveForm(cage: Cage) {
        this.animals.addAll(cage.animals)
    }
}

abstract class Animal(val name: String)

abstract class Fish(name: String) : Animal(name)

class GoldFish(name: String) : Fish(name)

class Crap(name: String) : Fish(name)
```

```kotlin
fun main() {
    val cage = Cage()
    cage2.put(Crap("잉어"))
    // type miss match 오류
    val carp: Carp = cage2.getFirst()
}
```

* cage에 잉어만 있지만 getFirst() 메소드를 호출하면 Animal이 나온다.
* 이 문제를 해결하기 위해서 제네릭을 사용한다.

```kotlin
class Cage2<T> {

    private val animals: MutableList<T> = mutableListOf()

    fun getFirst(): T {
        return animals.first()
    }

    fun put(animal: T) {
        this.animals.add(animal)
    }

    fun moveForm(cage: Cage2<T>) {
        this.animals.addAll(cage.animals)
    }
}
```

```kotlin
fun main() {
    val cage2 = Cage2<Crap>()
    cage2.put(Crap("잉어"))
    val crap: Crap = cage2.getFirst()
}
```

* 제네릭 <T>를 사용하면 Cage2 클래스를 생성할 때 타입을 지정할 수 있다.

```kotlin
fun main() {
    val goldFishCage = Cage2<GoldFish>()
    goldFishCage.put(GoldFish("금붕어"))

    val fishCage = Cage2<Fish>()
    // type miss match 오류
    fishCage.moveForm(goldFishCage)
}
```

* Cage<T> 간의 함수 호출인데 왜 GlideFish를 Fish로 옮길 수 없을까?
* **이는 제네릭과 무공변을 알아야 한다.**

## 2강 배열과 리스트. 제니릭과 무공변

### 상속관계의 의미

![](images/003.png)

![](images/004.png)

* 상위 타입이 들어가는 자리에 하위 타입이 대신 위치할 수 있다

![](images/005.png)

* Cage2<Fish>에 Cage2<GoldFish>를 넣을 수 없다.

![](images/006.png)

* **Cage2<Fish>에 Cage2<GoldFish>는 아무관계가 없다**
* **Cage2는 무공변 (in-variant, 불공변)하다 라고 말한다.**

### 무공변

* 왜 Fish와 GoldFish 간의 상속관계까 제네릭 클래스에서 유지되지 않을까?
* 왜 제네릭 클래스는 타입 파라미터 간의 상속관계까 있더라도 무공변할까?

### Java 배열

![](images/007.png)

* Java의 배열은 제네릭과 다르다.
* A 객체가 B 객체의 하위 타입이라면, A 배열이 B 배열의 하위 타입으로 간주된다.
* **Java의 배열은 공변 하다.**

![](images/008.png)

```java
String[] strs = new String[]{"A", "B", "C"}
Object[] objs = strs // String[]은 Object[]의 하위 타입이니 objs에 strs를 넣을 수 있다.

objs[0] = 1; // 컴파일상 가능함 

```

* objs는 Object[] 타입이니 1을 넣을 수 있을 것처럼 컴파일상의 문제는 없어 보이지만 objs는 사실 String[] 이기 때문에 int를 넣을 수 없다. 
* 떄문에 런터임 때 에러가 발생한다.
* 타입 세이프하지 않는 코드로 위험하다.


### Java의 배열과 리스트

```java
import java.util.List;

List<String> strs = List.of("A", "B", "C")
List<Object> objs = strs // Type Missmatch!
```
* **List는 제네릭을 사용하기 때문에 공변인 Array와 다르게 무공변하다.**
* 위 코드는 컴파일 떄에 원천적으로 불가능하다.
* 그러기 떄문에 제네릭은 


## 3강 공변과 반공변

![](images/009.png)


```kotlin

class Cage2<T> {
    
    fun moveForm(cage: Cage2<out T>) {
        this.animals.addAll(cage.animals)
    }
}
```
* **out을 붙이면 moveForm 함수를 호출할 때 Cage2는 공변하게 된다.**
* 변성을 준다는 것은 무공변에서 무공변으로 된다.
* out을 통해서 변셩(variance)를 주었기 떄문에 out을 variance anootation 이라고도 부른다.


![](images/009.png)

```kotlin

fun getFirst(): T {
    return animals.first()
}

fun put(animal: T) {
    this.animals.add(animal)
}

fun moveForm(otherCage: Cage2<out T>) {
    otherCage.getFirst() // 사용 가능
    otherCage.put(this.getFirst()) // 오류 발생
    this.animals.addAll(otherCage.animals)
}
```
* otherCage는 데이터를 꺼내는 getFirst() 함수만 사용할 수 있다.
* **otherCage는 생상자 (데이터를 꺼내는)역할만 할 수 있다.**

![](images/012.png)

![](images/011.png)

* 허용해주는 경우 타입 안전성이 깨져 런타임 오류가 발생한다.
* this는 잉어를 말하지만 otherCage는 금붕어를 말하는 것이기 때문에 잉어를 옮기는 것은 불가능하다

![](images/013.png)


```kotlin
fun moveForm(otherCage: Cage2<in T>) {
    otherCage.animals.addAll(this.animals)
}
```
* in이 붙은 otherCage는 데이터를 받을 수만 있다. otherCage는 소비자이다.


### 함수에 대해 공변, 반공변 정리

* out: (함수파라미터 입장에서) 생상자, 공변
* in: (함수 파라미터 입장에서) 소비자, 반공변

![](images/014.png)

![](images/015.png)