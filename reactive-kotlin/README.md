> 해당 내용은 [코틀린 리액티브 프로그래밍](http://www.kyobobook.co.kr/product/detailViewKor.laf?mallGb=KOR&ejkGb=KOR&barcode=9791161752976&orderClick=LA6) 을 보고 정리한 내용 입니다.

# 1장 리액티브 프로그래밍의 소개

## 리액티브 프로그랴밍이란 무엇인가 ?

리액티브 프로그래밍은 데이터 스트림과 변경 사항 전파를 중심으로 하는 비동기 프로그래밍 패러다임이다. 간단하게 말하면 데이터와 데이터 스트림에 영향을 미치는 모든 변경 사항을 관련된 모든 당사자들, 예를 들면 최종 사용자나 컴포넌트, 하위 구성 요소, 또는 어떻게든 연결돼 있는 다른 프로그램 등에게 전파하는 프로그램을 리액티브 프로그래램이라고 한다.


# 3장 옵저버블과 옵저버와 구독자

## 옵저버블

리액티브 프로그래밍에서 옵저버블은 **그 컨슈머가 소비할 수 있는 값을 산출해 내는 기본 계산 작업을 갖고 있다.** 여기서 중요한 것은 컨슈머가 값을 풀 방식으로 접근하지 않는다는 점이다. 오히려 **옵저버블은 컨슈머에게 값을 푸쉬하는 역할을 한다. 따라서 옵저버블은 인련의 연산자를 거친 아이템을 최종 옵저버로 보내는 푸시 기반의 조합 가능한 이터레이터다.**

* 옵저버는 옵저버블을 구독한다.
* 옵저버블이 그 내부의 아이템들을 내보내기 시작한다.
* 옵저버는 옵저버블에서 내보내는 모든 아이템에 반응 한다.

### 옵저버블이 동작하는 방

* onNext: 옵저버블은 모든 아이템을 하나씩 이 메서드에 전달한다.
* onComplete: 모든 아이템이 onNext 메서드를 통과하면 옵저버블은 onComplete 메서드를 호출한다.
* onError: 옵저버블에서 에러가 발생하면 onError 메서드가 호출돼 정의된 대로 에러를 처리한다. onError와 onComplete는 터미널 이벤트다. onError가 호출 돼었을 경우 onComplete
가 호출되지 않으며, 반대의 경우도 마찬가지이다.

```kotlin
@Test
internal fun `observer`() {
    val observer: Observer<Any> = object : Observer<Any> {
        override fun onComplete() {
            println("onComplete")
        }

        override fun onSubscribe(d: Disposable) {
            println("onSubscribe: $d")
        }

        override fun onError(e: Throwable) {
            println("onError: $e")
        }

        override fun onNext(item: Any) {
            println("onNext: $item")
        }
    }

    val observable: Observable<Any> = listOf("One", 2, "Three", "Four", 4.5, "Five", 6.0f).toObservable() //6

    observable.subscribe(observer)//7

    val observableOnList: Observable<List<Any>> = Observable.just(
        listOf("One", 2, "Three", "Four", 4.5, "Five", 6.0f),
        listOf("List with Single Item"),
        listOf(1, 2, 3, 4, 5, 6)
    )//8

    observableOnList.subscribe(observer)//9
}
```
* observer 인터페이스는 4개의 메서드가 선언돼 있다.
* onComplete() 메서드는 Observable이 **오류 없이 모든 아이템을 처리하면** 호출된다.
* onNext() 메서드는 옵저버블이 내보내는 각 아이템에 대해 호출된다.
* onError() 메서드는 옵저버블이 오류가 발생했을 때 호출된다.
* onSubscribe() 메서드는 옵저버가 옵저버블을 구독할 떄 마다 호출된다 .

#### Observable.create 메서드 이해

```kotlin
@Test
fun `Observable create 메서드 이해`() {
    // Observer 생성
    val observable = object : Observer<Any> {
        override fun onComplete() {
            println("onComplete")
        }

        override fun onSubscribe(d: Disposable) {
            println("onSubscribe: $d")
        }

        override fun onError(e: Throwable) {
            println("onError: $e")
        }

        override fun onNext(item: Any) {
            println("onNext: $item")
        }
    }

    val observable1 = Observable.create<String> {
        it.onNext("Emit 1")
        it.onNext("Emit 2")
        it.onNext("Emit 3")
        it.onNext("Emit 4")
        it.onComplete() // 완료 한다
    }

    observable1.subscribe(observable)

    val observable2 = Observable.create<String> {
        it.onNext("Emit 1")
        it.onNext("Emit 2")
        it.onNext("Emit 3")
        it.onNext("Emit 4")
        it.onError(Exception("Custom Exception")) // 예외를 발생시킨다
    }

    observable2.subscribe(observable)
}
```
Observable.create 메서드는 사용자가 지정한 데이터 구조를 사용하거나 내보내는 값을 제어하려고 할 떄 유용하다.

#### Observable.from 메서드 이해

```kotlin
@Test
fun `Observable from 메서드 이해`() {
    // Observer 생성
    val observer = object : Observer<Any> {
        override fun onComplete() {
            println("onComplete")
        }

        override fun onSubscribe(d: Disposable) {
            println("onSubscribe: $d")
        }

        override fun onError(e: Throwable) {
            println("onError: $e")
        }

        override fun onNext(item: Any) {
            println("onNext: $item")
        }
    }

    val list = listOf("string 1", "string 2", "string 3", "string 4")

    val fromIterable = Observable.fromIterable(list)

    fromIterable.subscribe(observer)


    val callable = object : Callable<String> {
        override fun call(): String {
            return "From Callable"
        }
    }

    val fromCallable = Observable.fromCallable(callable)
    fromCallable.subscribe(observer)


    val future = object : Future<String> {
        override fun isDone(): Boolean = true

        override fun get(): String = "Hello From Future"

        override fun get(timeout: Long, unit: TimeUnit): String = "Hello From Future"

        override fun cancel(mayInterruptIfRunning: Boolean): Boolean = false

        override fun isCancelled(): Boolean = false
    }

    val fromFuture = Observable.fromFuture(future)
    fromFuture.subscribe(observer)
}
```

#### Observable.just 메서드 이해
```kotlin
@Test
fun `observerable just 함수의 이해`() {
    val observer = object : Observer<Any> {
        override fun onComplete() {
            println("onComplete")
        }

        override fun onSubscribe(d: Disposable) {
            println("onSubscribe: $d")
        }

        override fun onError(e: Throwable) {
            println("onError: $e")
        }

        override fun onNext(item: Any) {
            println("onNext: $item")
        }
    }

    Observable.just("A String").subscribe(observer)
    Observable.just(54).subscribe(observer)
    Observable.just(listOf("string 1", "string 2", "string 3", "string 4")).subscribe(observer)
    Observable.just(
        mapOf(
            Pair("Key 1", "Value1"),
            Pair("Key 2", "Value2"),
            Pair("Key 3", "Value3")
        )
    ).subscribe(observer)
    Observable.just("string 1", "string 2", "string 3", "string 4").subscribe(observer) // (1)


//onSubscribe: 0
//onNext: A String
//onComplete
//onSubscribe: 0
//onNext: 54
//onComplete
//onSubscribe: 0
//onNext: [string 1, string 2, string 3, string 4]
//onComplete
//onSubscribe: 0
//onNext: {Key 1=Value1, Key 2=Value2, Key 3=Value3}
//onComplete
//onSubscribe: io.reactivex.internal.operators.observable.ObservableFromArray$FromArrayDisposable@75bfb235
//onNext: string 1
//onNext: string 2
//onNext: string 3
//onNext: string 4
//onComplete
}
```
Iterable 인스턴스를 Observable.just에 단일 인로 넘기면 전체 목록을 하나의 아이템으로 배출하는데, 이는 Iterable 내부의 각각의 아이템을 Observable로 생성하는 Observable.from과는 다르다.


* 인자와 함께 Observable.just를 호출
* Observable.just는 옵저버블을 생성
* onNext 알림을 통해 각각의 아이템을 내보냄
* 모든 인자의 제출이 완료되면 onComplete 알림을 실행

**위 결과에서 확인할 수 있듯이 리스트와 맵도 단일 아이템으로 취급한다.** 하지만 문자열을 여러개 보내는 (1) 코드의 결과를 보면 인자를 별개의 아이템으로 받아들여 내보내고 있다.


### Observable의 다른 팩토리 메서드

```kotlin
@Test
fun `Observable의 다른팩토리 메서드`() {
    val observer = object : Observer<Any> {
        override fun onComplete() {
            println("onComplete")
        }

        override fun onSubscribe(d: Disposable) {
            println("onSubscribe: $d")
        }

        override fun onError(e: Throwable) {
            println("onError: $e")
        }

        override fun onNext(item: Any) {
            println("onNext: $item")
        }
    }

    Observable.range(0, 10).subscribe(observer) // (1)
    Observable.empty<String>().subscribe(observer) // (2)

    runBlocking {
        Observable.interval(300, TimeUnit.MILLISECONDS).subscribe(observer) // (3)
        delay(900)

        Observable.timer(400, TimeUnit.MILLISECONDS).subscribe(observer) // (4)
        delay(450)
    }
}
```

* Observable.range() 메서드로 옵저버블을 생성하고 제공된 start부터 시작해서 count 만큼 정수를 내보낸다
* Observable.empty() 메서드는 onNext()으로 보내지 않고 즉시 onComplete() 메서드를 발생시킨다.
* Observable.interval() 메서드는 지정된 간격만큼 순차적으로 보내는데, 구독을 취소하거나 프로그램이 종료될 때까지 이어진다.
* Observable.timer() 메서드는 지정된 시간이 경과한 후에 한 번만 실행된다.

### 구독과 해지
 
Observable(관찰돼야 하는 대상)과 Observer(관찰해야 하는 주체)가 있다. 어떻게 이 둘을 연결할까 ? Observable과 Observer는 키보드 처럼 입력 장치와 컴퓨터를 연결할 때처럼 매개체가 필요하다. Subscribe 연산자에 대해 1개에서 3개의 메서드(onNext, onComplete, onError)로 전달 할 수 있다.


```kotlin
@Test
fun `구독과 해지`() {
    val observable = Observable.range(1, 5)

    observable.subscribe(
        {
            println("Next $it")
        },
        {
            println("Error ${it.message}")
        },
        {
            println("Done")
        }
    )

    val observer = object : Observer<Int> {
        override fun onComplete() {
            println("onComplete")
        }

        override fun onSubscribe(d: Disposable) {
            println("onSubscribe")
        }

        override fun onNext(t: Int) {
            println("onNext: $t")
        }

        override fun onError(e: Throwable) {
            println("onError: ${e.message}")
        }
    }

    observable.subscribe(observer)
    //Next 1
    //Next 2
    //Next 3
    //Next 4
    //Next 5
    //Done
    //onSubscribe
    //onNext: 1
    //onNext: 2
    //onNext: 3
    //onNext: 4
    //onNext: 5
    //onComplete
}
```

```kotlin

@Test
fun `구독과 해지2`() {
    runBlocking {
        val observable = Observable.interval(100, TimeUnit.MILLISECONDS)
        val observer = object : Observer<Long> {
            lateinit var disposable: Disposable
            
            override fun onComplete() {
                println("onComplete")
            }

            override fun onSubscribe(d: Disposable) {
                println("onSubscribe")
                disposable = d
            }

            override fun onNext(item: Long) {
                println("onNext: $item")
                if (item >= 10 && disposable.isDisposed.not()) {
                    disposable.dispose()
                    println("dispose")
                }
            }

            override fun onError(e: Throwable) {
                println("onError: ${e.message}")
            }
        }

        observable.subscribe(observer)
        delay(1500L)
    }
}
```
* Disposable 를 통해서 구독을 멈출 수가 있다.