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
* onNext 메서드 내부에 검사 로직을 위치시켰는데 제출된 값이 10보다 크거나 같은지 확인하거, 배출이 이미 중단괴거나 해지되지 않은 경우 5번에 그것을 해지한다.

## 핫, 콜드 옵저버블

### 콜드 옵저버블
```kotlin
@Test
fun `콜드 옵저버블`() {
    val observable = listOf("string1", "string2", "string3", "string4").toObservable()
    
    observable.subscribe(
        {
            println("Received $it")
        },
        {
            println("Error ${it.message}")
        },
        {
            println("Done")
        }
    )

    observable.subscribe(
        {
            println("Received $it")
        },
        {
            println("Error ${it.message}")
        },
        {
            println("Done")
        }
    )
//Received string1
//Received string2
//Received string3
//Received string4
//Done
//Received string1
//Received string2
//Received string3
//Received string4
//Done
}
```
동일한 옵저버블을 여러 번 구독해도 모든 구독의 새로운 배출을 얻을 수 있다. 옵저버블은 특징적인 기능을 갖고 있는데 **각 구독마다 처음부터 아이템을 배출하는 것을 콜드 옵저버블이라고 한다. 구체적으로 설명하면 콜드 옵저블은 구독 시에 실행을 시작하고 subscribe가 호출되면 아이템을 푸시하기 시작하는데 각 구독에서 아이템의 동일한 순서를 푸시한다.**

### 핫 옵저버블

콜드 옵저블은 수동적이며 구독이 호출될 때까지 아무것도 보내지 않는다. 핫 옵저버블은 콜드 옵저버블과 반대로, 배출을 시작하기 위해 수독할 필요가 없다. 콜드 옵저버블을 CD/DVD 레코딩으로 본다면 핫 옵저버블은 TV 채널과 비슷하다. **핫 옵저버블은 시청자가 시청하는지 여부에 관계없이 콘텐츠를 계속 브로드캐스팅 한다. 핫 옵저저블은 데이터 보다는 이벤트와 유사하다.**

```kotlin
@Test
fun `핫 옵저버블`() {
    val connectableObservable = listOf("string1", "string2", "string3", "string4", "string5").toObservable().publish()

    connectableObservable.subscribe { println("Subscription 1: $it") } // 콜드 옵저버블
    connectableObservable.map(String::reversed).subscribe { println("Subscription 2: $it") } // 콜드 옵저버블
    connectableObservable.connect() // connect를 사용하여 콜드 옵저저블을 핫 옵저저블로 변경한다.
    connectableObservable.subscribe { println("Subscription 3: $it") }
}
``` 

```kotlin
@Test
fun `핫 옵저버블2`() {
    val connectableObservable = Observable.interval(100, TimeUnit.MILLISECONDS).publish()

    connectableObservable.subscribe { println("Subscription 1: $it") } // 콜드 옵저버블
    connectableObservable.subscribe { println("Subscription 2: $it") }
    connectableObservable.connect() // connect를 사용하여 콜드 옵저저블을 핫 옵저저블로 변경한다.
    runBlocking { delay(500) }

    connectableObservable.subscribe { println("Subscription 3: $it") }
    runBlocking { delay(500) }
//Subscription 1: 0
//Subscription 2: 0
//Subscription 1: 1
//Subscription 2: 1
//Subscription 1: 2
//Subscription 2: 2
//Subscription 1: 3
//Subscription 2: 3
//Subscription 1: 4
//Subscription 2: 4
//Subscription 1: 5
//Subscription 2: 5
//Subscription 3: 5
//Subscription 1: 6
//Subscription 2: 6
//Subscription 3: 6
//Subscription 1: 7
//Subscription 2: 7
//Subscription 3: 7
//Subscription 1: 8
//Subscription 2: 8
//Subscription 3: 8
//Subscription 1: 9
//Subscription 2: 9
//Subscription 3: 9
}
```
세 번째 구독이 5번째 배출을 받았고 이전의 구독을 모두 놓쳤다는 것을 확인 할 수있다.

#### Subject

Hot Observables를 구현하는 또 다른 좋은 방법은 Subject이다. 

```kotlin
@Test
fun subject() {
    val observable = Observable.interval(100, TimeUnit.MILLISECONDS)
    val subject = PublishSubject.create<Long>()
    observable.subscribe(subject)

    subject.subscribe { println("Received $it") }
    runBlocking { delay(1100) }
//Received 0
//Received 1
//Received 2
//Received 3
//Received 4
//Received 5
//Received 6
//Received 7
//Received 8
//Received 9
//Received 10
}
```
* 옵저저블이 가져야 하는 모든 연산자를 갖고 있다.
* 옵저버와 마찬가지로 배출된 모든 값에 접근할 수 있다.
* Subject가 완료(completed)/오류(errored)구독 해지(unsubscribed)된 후에는 재사용할 수 없다.
* 가장 흥미로운 점은 그 자체로 가치를 전달한다는 것이다. onNext를 사용해서 값을 Subject(Observer) 측에 전달하면 Observable에서 접근이 가능하게 된다.

```kotlin
@Test
fun subject2() {
    val observable = Observable.interval(100, TimeUnit.MILLISECONDS)
    val subject = PublishSubject.create<Long>()
    observable.subscribe(subject)

    subject.subscribe { println("Subscription 1 Received $it") }
    runBlocking { delay(1100) }

    subject.subscribe { println("Subscription 2 Received $it") }
    runBlocking { delay(1100) }
}
```
## 다양한 구독자
* AsyncSubject
* PublishSubject
* BehaviorSubject
* ReplaySubject

### AsyncSubject 이해

```kotlin
@Test
fun `AsyncSubject 이해`() {
    val observable = Observable.just(1, 2, 3, 4)
    val subject = AsyncSubject.create<Int>()

    observable.subscribe(subject)
    subject.subscribe(
        {
            println("Received $it")
        },
        {
            it.printStackTrace()
        },
        {
            println("Done")
        }
    )
//Received 4
//Done
}
```

AsyncSubject는 수신 대기 중인 소스 옵저버블의 마지막 값을 한 번 만 배출한다.

### BehaviorSubject 이해

```kotlin
@Test
fun `BehaviorSubject 이해`() {
    val subject = BehaviorSubject.create<Int>()
    subject.onNext(1)
    subject.onNext(2)
    subject.onNext(3)
    subject.onNext(4) // 마지막 아이템
    subject.subscribe(
        {
            println("S1 Received $it")
        },
        {
            it.printStackTrace()
        },
        {
            println("S1 Completed")
        }
    )
    subject.onNext(5) // 마지막 아이템
    subject.subscribe(
        {
            println("S2 Received $it")
        },
        {
            it.printStackTrace()
        },
        {
            println("S2 Completed")
        }
    )
    subject.onComplete()
// S1 Received 4
// S1 Received 5
// S2 Received 5
// S1 Completed
// S2 Completed
}
```
**BehaviorSubject는 멀티캐스팅으로 동작하는데 구독 전의 마지막 아이템과 구독 후 모든 아이템을 배출한다.**


### ReplaySubject 이해

```kotlin
@Test
fun `ReplaySubject 이해`() {
    val subject = ReplaySubject.create<Int>()
    subject.onNext(1)
    subject.onNext(2)
    subject.onNext(3)
    subject.onNext(4)
    subject.subscribe(
        {
            println("S1 Received $it")
        },
        {
            it.printStackTrace()
        },
        {
            println("S1 Completed")
        }
    )
    subject.onNext(5)
    subject.subscribe(
        {
            println("S2 Received $it")
        },
        {
            it.printStackTrace()
        },
        {
            println("S2 Completed")
        }
    )
    subject.onComplete()
}
//S1 Received 1
//S1 Received 2
//S1 Received 3
//S1 Received 4
//S1 Received 5
//S2 Received 1
//S2 Received 2
//S2 Received 3
//S2 Received 4
//S2 Received 5
//S1 Completed
//S2 Completed
```

# 4장 백프레셔와 플로어블 소개
옵저버블은 추가 처리를 위해 옵저버가 소비할 항목을 배출한다. 그러나 옵저버블이 옵저버 소비할 수의 처리량보다 더 빨리 아이템이 배출되는 상황에서 문제가 발생한다. 4장에서는 디음을 중점으로 살펴본다.

* 백프레셔 이해하기
* 플로어블 및 가입자
* Flowable.create() 플로어블 생성하기
* 옵저저블과 플로어블 동시에 사용하기
* 백프레셔 연산자
* Flowable.generate() 연산자

## 백프레셔 이해

옵저저블의 유일한 문제 상황은 옵저버가 옵저저블 속도에 대처할 수 없는 경우다. 옵저저블은 기본적으로 아이템을 동기적으로 옵저버에 하나씩 푸시해 동작한다. 그러나 옵저버가 시간을 필요로 하는 작업을 처리해야 한다면 그 시간이 옵저저블이 각 항목을 배출하는 간격보다 길어질수 있다.

```kotlin
@Test
fun `백프레셔 이해`() {
    val observable = Observable.just(1, 2, 3, 4, 5, 6, 7, 9) // (1)
    val subject = BehaviorSubject.create<Int>()

    subject.observeOn(Schedulers.computation()) // (2)
        .subscribe {
            println("Sub 1 Received $it")
            runBlocking { delay(200) } // (4)
        }

    subject.observeOn(Schedulers.computation()) // (5)
        .subscribe {// (6)
            println("Sub 2 Received $it")
        }

    observable.subscribe(subject) // (7)
    runBlocking { delay(200) } // (8)
}
```

* (1): 옵저저블을 생성한다
* (2),(3): 구독한다
* (7): BehaviorSubject를 구독한 후에 BehaviorSubject를 사용해 옵저저블을 구독하면 BehaviorSubject의 모든 배출을 받을 수 있게 된다.
* (4): 첫 번쨰 구독 내에서 시간이 오래 걸리게 하기 위해서 delay 메서드를 사용한다
* `subject.observeOn(Schedulers.computation())`메서드는 `observeOn`에서 구독을 실행하는 스레드를 지정하도록 해주고 `Schedulers.computation()`은 계산을 수행할 스레드를 제공한다.
* (8): 실행은 백그라운드(다른 스레드)에서 수행하기 때문에 코드를 확인 하기 위해서 delay를 진행 

```
Sub 2 Received 1
Sub 1 Received 1
Sub 2 Received 2
Sub 2 Received 3
Sub 2 Received 4
Sub 2 Received 5
Sub 2 Received 6
Sub 2 Received 7
Sub 2 Received 9
Sub 1 Received 2
```
해당 출력은 구독을 번갈아가면서 1~9 까지 모든 순자를 출력하지 않는다. 이 프로그램은 **두 옵저버에 한 번만 배출하는 subject인 핫 옵저버블로서의 행동을 멈춘것은 아니다. 그러나 첫 번쨰 옵저버에서 각 계산이 오래 걸렸기 때문에 각 배출들은 대기열로 들어가게 된것이다.** 이것은 OutOfMemoryError 예뢰를 포함해 많은 문제를 일으킬 수 있으므로 좋지 않은 행동이다.


```kotlin
@Test
fun `백프레셔 이해 2`() {
    val observable = Observable.just(1, 2, 3, 4, 5, 6, 7, 9) // (1)

    observable
        .map { MyItem(it) } // (2)
        .observeOn(Schedulers.computation()) // (3)
        .subscribe { // (4)
            println("Received $it")
            runBlocking { delay(200) } // (5)
        }

    runBlocking { delay(2000) } // (6)

}

data class MyItem(val id: Int) {
    init {
        println("MyItem Created $id") // (7)
    }
}
```
`(2)`에서 사용한 map 연산자를 사용해 Int 항목을 MyItem 객체로 변환했다. 예제에서 map은 배출 아이템을 추적하는데 사용 했다. 배출이 발생할 때 마다 즉시 map 연산자로 전달돼 MyItem 클래스의 객체를 생성한다.

```
MyItem Created 1
MyItem Created 2
MyItem Created 3
MyItem Created 4
MyItem Created 5
MyItem Created 6
Received MyItem(id=1)
MyItem Created 7
MyItem Created 9
Received MyItem(id=2)
Received MyItem(id=3)
Received MyItem(id=4)
Received MyItem(id=5)
Received MyItem(id=6)
Received MyItem(id=7)
Received MyItem(id=9)
```
**배출의 출력에서 볼 수 있듯이 MyItem의 생성은 매우 빠르며 컨슈머로 알려진 옵저버가 인쇄를 시작도 하기 전에 완료되었다. 여기서 볼 수 있듯이 문제는 배출이 대기열에 쌓이고 있는데 컨슈머는 이 전의 배출량을 처리하고 있다는 것이다.** 

## 플로어블

플로어블은 옵저버들의 백프레셔 버전이라고 부를 수 있다. 아마 두 가지 유일한 차이점은 플오어블이 백프레셔를 고려한다는 점뿐인데 옵저버들은 가능하지 않다. 그게 전부다. 플로어블은 연산자를 위해 최대 128개의 항목을 가질 수 있는 버퍼를 제공한다. 그래서 컨슈머가 시간이 걸리는 작업을 실행할 때 배출된 항목이 버퍼에 대기할 수있다.

```kotlin
@Test
fun `풀로어블`() {
    Observable.range(1, 1000) //(1)
        .map { MyItem(it) } //(2)
        .observeOn(Schedulers.computation())
        .subscribe( //(3)
            {
                println("Received $it")
                runBlocking { delay(50) } //(4)
            },
            {
                it.printStackTrace() //(5)
            }
        )
    runBlocking { delay(6000) }
}

data class MyItem(val id: Int) {
    init {
        println("MyItem Created $id")
    }
}
```
* Observable.range() 연산자를  사용해서 1에서 1000 사이의 숫자를 배출하는 간단한 코드이다. 
* (2) map 연산자로 Int에서 MyItem3 객체를 생성한다. 
* (3) 옵저버블을 구독했다.
* (4) 시간이 오래 걸리는 구독 코드를 흉내내기 위해 지연 연산을 실행
* (5) 다시 프로그램이 실행을 멈추기 전에 컨슈머가 모든 아이템의 처리를 기다리는 블로킹 코드
 
```
MyItem Created 1
MyItem Created 5
Recived MyItem(id=1)
MyItem Created 6
...
MyItem Created 1000
Recived MyItem(id=2)
...
Recived MyItem(id=113)
Recived MyItem(id=114)
```
옵저버는 그것과 보조를 맞추지 않는다. 옵저저블이 모든 항목을 배출할 동안 옵저버는 첫 번째 항목만을 처리했다. 앞에서 설명한 것처럼 이런 현상은 OutOfMemory 오류를 비롯해 많은 문제를 발생 시킬 수 있다. Flowable로 변경해보자

```kotlin
@Test
fun `flowable`() {
    Flowable.range(1, 1000)
        .map { MyItem(it) }
        .observeOn(Schedulers.io())
        .subscribe(
            {
                println("Receivbed $it")
                runBlocking { delay(50) }
            },
            {
                it.printStackTrace()
            }
        )
    runBlocking { delay(6000) }
}
```

```
MyItem Created 1
MyItem Created 4
Receivbed MyItem(id=1)
MyItem Created 6
..
MyItem Created 128
Receivbed MyItem(id=2)
...
Receivbed MyItem(id=94)
Receivbed MyItem(id=95)
Receivbed MyItem(id=96)
MyItem Created 129
MyItem Created 130
...
MyItem Created 220
MyItem Created 221
MyItem Created 222
MyItem Created 223
MyItem Created 224
Receivbed MyItem(id=97)
...
Receivbed MyItem(id=113)
Receivbed MyItem(id=114)
```

**플오어블은 모든 아이템을 한 번에 배출하지 않고 컨슈머가 처리를 시작할 수 있을 때까지 기다렸다가 다시 배출을 전달하며, 완료될 때까지 이 동작을 반복한다.**

## 플로어블과 옵저버블 사용 구분

지금쯤 플로어블을 사용하기 편리한 도구라고 생각하고 모든 옵저버블은 대체할 수도 있다. **그러나 항상 플로어블이 옵저버블보다 나은 것은아니다. 플로어블은 백프레셔 전략을 제공하지만 옵저버블이 존재하는 데는 이유가 있으며 둘 다 장단점이 있다.**

### 플로어블을 언제 사용할까

다음은 플로어블의 사용을 고려해야 할 상항이다. **플로어블은 옵저버블보다 느리다는 것을 기억하자.**

* 플로어블과 백프레셔는 더 많은 양의 데이터를 처리할 때 도움이 된다. 따라서 원천 데이터 10,000개 이상의 아이템을 배출한다면 플로어블을 사용하자. 특히 원천이 비동기적으로 동작해 필요시에 컨슈머 체인이 생상자에게 배출량을 제한/규제을 요청할 수 있는 경우에 접합하다.
* 파일이나 데이터베이스를 읽거나 파싱하는 경우다.
* 결과를 반환하는 동안 IO 소스의 양을 조절할 수 있는 블로킹을 지원하는 네티워크 IO 작업/스프리밍 API에서 배출할 때 사용한다.


### 옵저저블은 언제 사용할까
* 소량의 데이터(10,000개 미만의 배출)를 다룰 때
* 오로지 동기 방식으로 작업하길 원하거나 또는 제한된 동시성을 가진 작업을 수행할 때
* UI 이벤트를 발생 시킬 때

## 플로어블과 구독자
플로어블은 옵저버 대신 백프레셔 호환이 가능한 구독자를 사용한다. 그러나 람다식을 사용한다면 차이점을 발견 할 수 없을 것이다. 그렇다면 옵저버 대신 구독자를 사용해야 하는 이유는 무엇일까? 왜냐하면 구독자가 일부 추가 기능과 빽프레셔를 동시에 지원하기 때문이다. 예를 들어 얼마나 많은 아이템을 받기를 원하는지 메시지로 전당할 수 있다. 아니면 구독자를 사용하는 동안 업스트림에서 수신하고자 하는 항목의 수를 지정하도록 할 수 있는데 아무값도 지정하지 안흥면 어떤 배출도 수신하리 못할 것이다.

코드에서 원하는 배출량을 지정하지 않았지만 내부적으로 배출량을 무제한으로 요청했기 때문에 배출된 모든 아이템을 수신할 수 있었다.


```kotlin
@Test
fun subscriber() {

    Flowable.range(1, 15)
        .map { MyItem(it) }
        .observeOn(Schedulers.io())
        .subscribe(object : Subscriber<MyItem> {
            lateinit var subscription: Subscription

            override fun onSubscribe(subscription: Subscription) {
                this.subscription = subscription
                subscription.request(5)
            }

            override fun onNext(t: MyItem?) {
                runBlocking { delay(50) }
                println("Subscriber received $t")

                if (t!!.id == 5) {
                    println("Request two more")
                    subscription.request(2)
                }
            }

            override fun onError(t: Throwable) = t.printStackTrace()

            override fun onComplete() = println("Done")
        })

    runBlocking { delay(10000) }
}

```

```
MyItem Created 1
MyItem Created 2
...
MyItem Created 15
Subscriber received MyItem(id=1)
Subscriber received MyItem(id=2)
Subscriber received MyItem(id=3)
Subscriber received MyItem(id=4)
Subscriber received MyItem(id=5)
Request two more
Subscriber received MyItem(id=6)
Subscriber received MyItem(id=7)
BUILD SUCCESSFUL in 13s
```

subscription의 구독을 초기에는 5로 서정했고 5게 이상 부터는 2개식 구독을 진행하는 것으로 수정했다. 걀과를 보면 해당 설정을 이해할 수 있다.

## BackpressureStrategy.MISSING 와 onBackpressureXXX()

BackpressureStrategy.MISSING은 backpressure 전략을 구현하지 않으므로 플로어블에게 어떤 전략을 따를지 명시적로 알려줄 필요가 있음을 의미한다. onBackpressureXXX() 연산자를 사용하면 동일한 결과를 얻을 수 있으며 몇 가지 추가 구성을 옵션이 제공된다.

* onBackpressureBuffer() -> 해당 버퍼가 가득 차면 Subscription을 푸쉬를 제한, 버퍼 크기 할당 가능 
* onBackpressureDrop() -> 해당 버퍼가 가득 차면 그 뒤로 부터 모두 Drop, Drop 람다로 캐치 가능
* onBackpressureLatest() -> 해당 버퍼가 가득 차게되면 마지막 버퍼의는 구독 가능

# 07 RxKotlin의 스케줄러를 사용한 동시성과 병렬처리

## 동시성 소개

> 프로그래밍 패러다임으로서, 동시 컴퓨팅은 모듈화 프로그래밍의 한 형태다.즉 전체 계산을 작은 단위의 계산으로 분해해 동시적으로 실핼할 수 있다.

동시성은 전체 작업을 작은 부분으로 나눠 동시에 실행하는 것이다.

## 병렬 시행화 동시성

결론은 동시성은 병렬화를 통해서 이뤄지지만 동일한 것은 아니다. 오히려 동시성을 당성할 수 있는 방법에 관한것이다.

## 스케줄러는 무엇인가

기본적으로 옵저버블과 이에 적영된 연산자 체인은 subscribe가 호출된 동일한 스레드에서 작업을 수행하며, 옵저버가 onComplete 또는 onError 알림을 수신할 때까지 스레드가 차단된다. 스케줄러를 사용하면 이것을 변경할 수 있다.

스케줄러는 스레드 풀로 생각하면 된다. 스케줄러를 사용해 ReactiveX는 스레드풀을 생성하고 스레드를 실행할 수 있다. ReactiveX에서 이것을 기본적으로 멀티 스레딩과 동시성을 추상화한것으로 동시성 구현을 훨씬 쉽게 만들어준다.

### 스케줄러의 종류

#### Schedulers.io() : I/O 연관 스케줄러
 
`Schedulers.io()`는 I/O 관련 스레드를 제공한다. 좀 더 정확하게 말하자면 Schedulers.io()는 I/O 관련 작업을 수행 할 수 있는 무제한의 워커 스레드를 생성하는 스레드풀을 제공한다.

이 풀의 모든 스레드는 블로킹이고 I/O 작업을 더 많이 수행하도록 작성했기 때문에 계산 집약적인 작업보다 CPU 부하는 적지만 대기중인 I/O 작업으로 인해 조금 더 오래걸릴 수 있다. I/O 작업은 파일시스템, 데이터베이스, 서비스 또는 I/O 장치와의 상호작용을 의미한다. 

메모리가 허용하는 무제한의 스레드를 생성해 OutOfMemory 오류를 일으킬 수 있이므로 이 시케줄러를 사용할 떄는 주의해야한다.

#### Schedulers.computation() : CPU 연관 스케줄러

`Schedulers.computation()`는 아마도 프로그래머에게 가장 쥬용한 스케줄러일것이다. 이것은 사용 가능한 CPU 코어와 동일한 수의 스레드를 가지는 제한된 스레드풀을 제공한다. 이 스케줄러는 CPU를 주로 사용하는 작업을 위한 것이다.

`Schedulers.computation()`는 CPU 잡중적인 작업에만 사용해야하며 다른 종류의 작업에 사용해서는 안된다. 그 이유는 이 스케줄러의 스레드가 CPU 코어를 사용 중인 상태로 유지하는데, I/O 관련이나 계산과 관련되지 않은 작업에 사용되는 경우 전체 애플리케이션의 속도를 저하시킬 수 있기 때문이다.

I/O에 관련된 작업에는 `Schedulers.io`를 사용하고 계산 목적에는 `Schedulers.computation()`을 고려해야 하는 주된 이유는 computational() 스레드가 프로세스를 더 잘 활용하며 사용 가능한 CPU 코어보다 더 많은 스레드를 생성하지 않고 스레드를 재사용하기 떄문이다. `Schedulers.io()`는 무제한 스레드를 제공하고 있는데 io 블럭에서 10,000개의 계산 작업을 병렬로 예약하는 경우 10,000 개의 작업은 각각 자체 스데를 가지게 되므로 CPU를 놓고 서로 경쟁하게 된다. 이는 컨텍스트 전환비용을 발생 시킨다.

#### Schedulers.newThread()

`Schedulers.newThread()`는 제공된 각 작업에 대해 새 스레드를 만드느 스케줄러를 제공한다. 언뜻 보기에는 `Schedulers.io()`와 비슷하게 보일 수 있지만 시제로 큰 차이가 있다.

`Schedulers.io()`는 스레드 풀을 사용하고 새로운 작업을 할당 받을 때마다 먼저 스레드풀을 소사해 유휴 스레드가 해당 작업을 실행할 수 있는지를 확인한다. 작업을 시작하기위해 기존의 스레드를 사용할 수 없으므로 새로운 스레드를 생성한다.

그러나 `Schedulers.newThread()`는 스레드 풀을 사용하지 않는다. 대신 모든 요총에 대해 새로운 스레드를 생성하고 그 사실을 잊어버린다.

대부분의 경우 `Schedulers.computation()`을 사용하고 그렇지 않은 경우 `Schedulers.io()`를 고려해야하며 `Schedulers.newThread()`는 사용하지 않는 것이 좋다, 스레드는 매우 비싼 자원이므로 새 스레드를 가능한 많이 만들지 않도록 노력해야한다.

#### Schedulers.single()

`Schedulers.single()`는 하나의 스레드만 포함하는 스케줄러를 제공하고 모든 호출에 대해 단일 인스턴스를 반환한다. 반드시 순차적으로 작업을 실행해야 하는 상황에서 가장 유용한 옵션이다. 하나의 스레드만 제공하믈 여기에 대기죽인 모든 작업은 순차적으로 실행될 수 있다.

#### Schedulers.trampoline()

`Schedulers.single()`, `Schedulers.trampoline()` 두 개의 스케줄러는 모두 순차적으로 실행된다. `Schedulers.single()`는 모든 작업이 순차적으로 실행되도록 보장하지만 호출된 스레드와 병렬로 실행될 수 있다. 그 부분이 `Schedulers.trampoline()`와 다르다.

#### Schedulers.from()

애플리케이션을 개발하는 동안 사용자가 정의한 스케줄러를 원할 수 있다. 이런 경우 `Schedulers.from()`을 사용하면 된다.    


## 스케줄러 사용법: subScribeOn, ObserveOn 연산자

### 구독 시 시르데 변경: subscribeOn 연산자

```kotlin
@Test
fun `subscribeOn 연산자`() {
    listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
        .toObservable()
        .map {
            println("Mapping $it ${Thread.currentThread().name}")
            return@map it.toInt()
        }
        .subscribe {
            println("Recived $it ${Thread.currentThread().name}")
        }
}
```
```
Mapping 1 Test worker
Recived 1 Test worker
Mapping 2 Test worker
Recived 2 Test worker
Mapping 3 Test worker
Recived 3 Test worker
Mapping 4 Test worker
Recived 4 Test worker
Mapping 5 Test worker
Recived 5 Test worker
Mapping 6 Test worker
Recived 6 Test worker
Mapping 7 Test worker
Recived 7 Test worker
Mapping 8 Test worker
Recived 8 Test worker
Mapping 9 Test worker
Recived 9 Test worker
Mapping 10 Test worker
Recived 10 Test worker
```
출력을 확인 해보면 Test workcer 스레드가 전체 구독을 실행하는 것을 확인 할 수 있다.

```kotlin
@Test
fun `subscribeOn 연산자`() {
    listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
        .toObservable()
        .map {
            println("Mapping $it ${Thread.currentThread().name}")
            return@map it.toInt()
        }
        .subscribeOn(Schedulers.computation())
        .subscribe {
            println("Received $it ${Thread.currentThread().name}")
        }

    runBlocking { delay(1000) }
}

```

```
Mapping 1 RxComputationThreadPool-1
Received 1 RxComputationThreadPool-1
Mapping 2 RxComputationThreadPool-1
Received 2 RxComputationThreadPool-1
Mapping 3 RxComputationThreadPool-1
Received 3 RxComputationThreadPool-1
Mapping 4 RxComputationThreadPool-1
Received 4 RxComputationThreadPool-1
Mapping 5 RxComputationThreadPool-1
Received 5 RxComputationThreadPool-1
Mapping 6 RxComputationThreadPool-1
Received 6 RxComputationThreadPool-1
Mapping 7 RxComputationThreadPool-1
Received 7 RxComputationThreadPool-1
Mapping 8 RxComputationThreadPool-1
Received 8 RxComputationThreadPool-1
Mapping 9 RxComputationThreadPool-1
Received 9 RxComputationThreadPool-1
Mapping 10 RxComputationThreadPool-1
Received 10 RxComputationThreadPool-1
```

### 다른 스레드에서 관찰: observeOn 연산자
```kotlin
@Test
fun `subscribeOn 연산자`() {
    listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
        .toObservable()
        .map { item ->
            println("Mapping $item ${Thread.currentThread().name}")
            return@map item.toInt()
        }
        .subscribeOn(Schedulers.computation())
        .observeOn(Schedulers.io())
        .subscribe { item ->
            println("Received $item ${Thread.currentThread().name}")
        }

    runBlocking { delay(1000) }
}
```

```
Mapping 1 RxComputationThreadPool-1
Mapping 2 RxComputationThreadPool-1
Mapping 3 RxComputationThreadPool-1
Mapping 4 RxComputationThreadPool-1
Mapping 5 RxComputationThreadPool-1
Received 1 RxCachedThreadScheduler-1
Mapping 6 RxComputationThreadPool-1
Received 2 RxCachedThreadScheduler-1
Mapping 7 RxComputationThreadPool-1
Received 3 RxCachedThreadScheduler-1
Mapping 8 RxComputationThreadPool-1
Received 4 RxCachedThreadScheduler-1
Mapping 9 RxComputationThreadPool-1
Received 5 RxCachedThreadScheduler-1
Mapping 10 RxComputationThreadPool-1
Received 6 RxCachedThreadScheduler-1
Received 7 RxCachedThreadScheduler-1
Received 8 RxCachedThreadScheduler-1
Received 9 RxCachedThreadScheduler-1
Received 10 RxCachedThreadScheduler-1
```
subscribeOn은 그 자체로 휼륭해 보이지만 어떤 경우엔느 접합하지 않을 수 있다. 예를들어 computation 스레드에서 계산을 수행하고 io 스레드에서 결과를 표시하도록 할 수 있다. subscribeOn 연산자는 이런 요구사항을 위해서 동료가 필요하다. 전체 구독에 대해서 스레드를 지정하지만 특연 연산자에 대한 스레드를 지정하려면 도움이 필요하다. 

subscribeOn 연사자의 완벽한 동료는 observeOn 연산자아다. observeOn 연산자는 그 후에 호출되는 모든 연산자에 스케줄러를 지정한다. 

위 예제는 `subscribeOn(Schedulers.computation())`를 호출해 computation 스레드를 지정했고, 그 결과를 받기 위해 `observeOn(Schedulers.io())`를 호출해 io 스레드로 전달했다.
