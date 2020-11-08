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
MyItem Created 2
MyItem Created 3
MyItem Created 4
MyItem Created 5
Recevied MyItem(id=1)
MyItem Created 6
MyItem Created 7
MyItem Created 8
MyItem Created 9
MyItem Created 10
MyItem Created 11
MyItem Created 12
MyItem Created 13
MyItem Created 14
MyItem Created 15
MyItem Created 16
MyItem Created 17
MyItem Created 18
MyItem Created 19
MyItem Created 20
MyItem Created 21
MyItem Created 22
MyItem Created 23
MyItem Created 24
MyItem Created 25
MyItem Created 26
MyItem Created 27
MyItem Created 28
MyItem Created 29
MyItem Created 30
MyItem Created 31
MyItem Created 32
MyItem Created 33
MyItem Created 34
MyItem Created 35
MyItem Created 36
MyItem Created 37
MyItem Created 38
MyItem Created 39
MyItem Created 40
MyItem Created 41
MyItem Created 42
MyItem Created 43
MyItem Created 44
MyItem Created 45
MyItem Created 46
MyItem Created 47
MyItem Created 48
MyItem Created 49
MyItem Created 50
MyItem Created 51
MyItem Created 52
MyItem Created 53
MyItem Created 54
MyItem Created 55
MyItem Created 56
MyItem Created 57
MyItem Created 58
MyItem Created 59
MyItem Created 60
MyItem Created 61
MyItem Created 62
MyItem Created 63
MyItem Created 64
MyItem Created 65
MyItem Created 66
MyItem Created 67
MyItem Created 68
MyItem Created 69
MyItem Created 70
MyItem Created 71
MyItem Created 72
MyItem Created 73
MyItem Created 74
MyItem Created 75
MyItem Created 76
MyItem Created 77
MyItem Created 78
MyItem Created 79
MyItem Created 80
MyItem Created 81
MyItem Created 82
MyItem Created 83
MyItem Created 84
MyItem Created 85
MyItem Created 86
MyItem Created 87
MyItem Created 88
MyItem Created 89
MyItem Created 90
MyItem Created 91
MyItem Created 92
MyItem Created 93
MyItem Created 94
MyItem Created 95
MyItem Created 96
MyItem Created 97
MyItem Created 98
MyItem Created 99
MyItem Created 100
MyItem Created 101
MyItem Created 102
MyItem Created 103
MyItem Created 104
MyItem Created 105
MyItem Created 106
MyItem Created 107
MyItem Created 108
MyItem Created 109
MyItem Created 110
MyItem Created 111
MyItem Created 112
MyItem Created 113
MyItem Created 114
MyItem Created 115
MyItem Created 116
MyItem Created 117
MyItem Created 118
MyItem Created 119
MyItem Created 120
MyItem Created 121
MyItem Created 122
MyItem Created 123
MyItem Created 124
MyItem Created 125
MyItem Created 126
MyItem Created 127
MyItem Created 128
MyItem Created 129
MyItem Created 130
MyItem Created 131
MyItem Created 132
MyItem Created 133
MyItem Created 134
MyItem Created 135
MyItem Created 136
MyItem Created 137
MyItem Created 138
MyItem Created 139
MyItem Created 140
MyItem Created 141
MyItem Created 142
MyItem Created 143
MyItem Created 144
MyItem Created 145
MyItem Created 146
MyItem Created 147
MyItem Created 148
MyItem Created 149
MyItem Created 150
MyItem Created 151
MyItem Created 152
MyItem Created 153
MyItem Created 154
MyItem Created 155
MyItem Created 156
MyItem Created 157
MyItem Created 158
MyItem Created 159
MyItem Created 160
MyItem Created 161
MyItem Created 162
MyItem Created 163
MyItem Created 164
MyItem Created 165
MyItem Created 166
MyItem Created 167
MyItem Created 168
MyItem Created 169
MyItem Created 170
MyItem Created 171
MyItem Created 172
MyItem Created 173
MyItem Created 174
MyItem Created 175
MyItem Created 176
MyItem Created 177
MyItem Created 178
MyItem Created 179
MyItem Created 180
MyItem Created 181
MyItem Created 182
MyItem Created 183
MyItem Created 184
MyItem Created 185
MyItem Created 186
MyItem Created 187
MyItem Created 188
MyItem Created 189
MyItem Created 190
MyItem Created 191
MyItem Created 192
MyItem Created 193
MyItem Created 194
MyItem Created 195
MyItem Created 196
MyItem Created 197
MyItem Created 198
MyItem Created 199
MyItem Created 200
MyItem Created 201
MyItem Created 202
MyItem Created 203
MyItem Created 204
MyItem Created 205
MyItem Created 206
MyItem Created 207
MyItem Created 208
MyItem Created 209
MyItem Created 210
MyItem Created 211
MyItem Created 212
MyItem Created 213
MyItem Created 214
MyItem Created 215
MyItem Created 216
MyItem Created 217
MyItem Created 218
MyItem Created 219
MyItem Created 220
MyItem Created 221
MyItem Created 222
MyItem Created 223
MyItem Created 224
MyItem Created 225
MyItem Created 226
MyItem Created 227
MyItem Created 228
MyItem Created 229
MyItem Created 230
MyItem Created 231
MyItem Created 232
MyItem Created 233
MyItem Created 234
MyItem Created 235
MyItem Created 236
MyItem Created 237
MyItem Created 238
MyItem Created 239
MyItem Created 240
MyItem Created 241
MyItem Created 242
MyItem Created 243
MyItem Created 244
MyItem Created 245
MyItem Created 246
MyItem Created 247
MyItem Created 248
MyItem Created 249
MyItem Created 250
MyItem Created 251
MyItem Created 252
MyItem Created 253
MyItem Created 254
MyItem Created 255
MyItem Created 256
MyItem Created 257
MyItem Created 258
MyItem Created 259
MyItem Created 260
MyItem Created 261
MyItem Created 262
MyItem Created 263
MyItem Created 264
MyItem Created 265
MyItem Created 266
MyItem Created 267
MyItem Created 268
MyItem Created 269
MyItem Created 270
MyItem Created 271
MyItem Created 272
MyItem Created 273
MyItem Created 274
MyItem Created 275
MyItem Created 276
MyItem Created 277
MyItem Created 278
MyItem Created 279
MyItem Created 280
MyItem Created 281
MyItem Created 282
MyItem Created 283
MyItem Created 284
MyItem Created 285
MyItem Created 286
MyItem Created 287
MyItem Created 288
MyItem Created 289
MyItem Created 290
MyItem Created 291
MyItem Created 292
MyItem Created 293
MyItem Created 294
MyItem Created 295
MyItem Created 296
MyItem Created 297
MyItem Created 298
MyItem Created 299
MyItem Created 300
MyItem Created 301
MyItem Created 302
MyItem Created 303
MyItem Created 304
MyItem Created 305
MyItem Created 306
MyItem Created 307
MyItem Created 308
MyItem Created 309
MyItem Created 310
MyItem Created 311
MyItem Created 312
MyItem Created 313
MyItem Created 314
MyItem Created 315
MyItem Created 316
MyItem Created 317
MyItem Created 318
MyItem Created 319
MyItem Created 320
MyItem Created 321
MyItem Created 322
MyItem Created 323
MyItem Created 324
MyItem Created 325
MyItem Created 326
MyItem Created 327
MyItem Created 328
MyItem Created 329
MyItem Created 330
MyItem Created 331
MyItem Created 332
MyItem Created 333
MyItem Created 334
MyItem Created 335
MyItem Created 336
MyItem Created 337
MyItem Created 338
MyItem Created 339
MyItem Created 340
MyItem Created 341
MyItem Created 342
MyItem Created 343
MyItem Created 344
MyItem Created 345
MyItem Created 346
MyItem Created 347
MyItem Created 348
MyItem Created 349
MyItem Created 350
MyItem Created 351
MyItem Created 352
MyItem Created 353
MyItem Created 354
MyItem Created 355
MyItem Created 356
MyItem Created 357
MyItem Created 358
MyItem Created 359
MyItem Created 360
MyItem Created 361
MyItem Created 362
MyItem Created 363
MyItem Created 364
MyItem Created 365
MyItem Created 366
MyItem Created 367
MyItem Created 368
MyItem Created 369
MyItem Created 370
MyItem Created 371
MyItem Created 372
MyItem Created 373
MyItem Created 374
MyItem Created 375
MyItem Created 376
MyItem Created 377
MyItem Created 378
MyItem Created 379
MyItem Created 380
MyItem Created 381
MyItem Created 382
MyItem Created 383
MyItem Created 384
MyItem Created 385
MyItem Created 386
MyItem Created 387
MyItem Created 388
MyItem Created 389
MyItem Created 390
MyItem Created 391
MyItem Created 392
MyItem Created 393
MyItem Created 394
MyItem Created 395
MyItem Created 396
MyItem Created 397
MyItem Created 398
MyItem Created 399
MyItem Created 400
MyItem Created 401
MyItem Created 402
MyItem Created 403
MyItem Created 404
MyItem Created 405
MyItem Created 406
MyItem Created 407
MyItem Created 408
MyItem Created 409
MyItem Created 410
MyItem Created 411
MyItem Created 412
MyItem Created 413
MyItem Created 414
MyItem Created 415
MyItem Created 416
MyItem Created 417
MyItem Created 418
MyItem Created 419
MyItem Created 420
MyItem Created 421
MyItem Created 422
MyItem Created 423
MyItem Created 424
MyItem Created 425
MyItem Created 426
MyItem Created 427
MyItem Created 428
MyItem Created 429
MyItem Created 430
MyItem Created 431
MyItem Created 432
MyItem Created 433
MyItem Created 434
MyItem Created 435
MyItem Created 436
MyItem Created 437
MyItem Created 438
MyItem Created 439
MyItem Created 440
MyItem Created 441
MyItem Created 442
MyItem Created 443
MyItem Created 444
MyItem Created 445
MyItem Created 446
MyItem Created 447
MyItem Created 448
MyItem Created 449
MyItem Created 450
MyItem Created 451
MyItem Created 452
MyItem Created 453
MyItem Created 454
MyItem Created 455
MyItem Created 456
MyItem Created 457
MyItem Created 458
MyItem Created 459
MyItem Created 460
MyItem Created 461
MyItem Created 462
MyItem Created 463
MyItem Created 464
MyItem Created 465
MyItem Created 466
MyItem Created 467
MyItem Created 468
MyItem Created 469
MyItem Created 470
MyItem Created 471
MyItem Created 472
MyItem Created 473
MyItem Created 474
MyItem Created 475
MyItem Created 476
MyItem Created 477
MyItem Created 478
MyItem Created 479
MyItem Created 480
MyItem Created 481
MyItem Created 482
MyItem Created 483
MyItem Created 484
MyItem Created 485
MyItem Created 486
MyItem Created 487
MyItem Created 488
MyItem Created 489
MyItem Created 490
MyItem Created 491
MyItem Created 492
MyItem Created 493
MyItem Created 494
MyItem Created 495
MyItem Created 496
MyItem Created 497
MyItem Created 498
MyItem Created 499
MyItem Created 500
MyItem Created 501
MyItem Created 502
MyItem Created 503
MyItem Created 504
MyItem Created 505
MyItem Created 506
MyItem Created 507
MyItem Created 508
MyItem Created 509
MyItem Created 510
MyItem Created 511
MyItem Created 512
MyItem Created 513
MyItem Created 514
MyItem Created 515
MyItem Created 516
MyItem Created 517
MyItem Created 518
MyItem Created 519
MyItem Created 520
MyItem Created 521
MyItem Created 522
MyItem Created 523
MyItem Created 524
MyItem Created 525
MyItem Created 526
MyItem Created 527
MyItem Created 528
MyItem Created 529
MyItem Created 530
MyItem Created 531
MyItem Created 532
MyItem Created 533
MyItem Created 534
MyItem Created 535
MyItem Created 536
MyItem Created 537
MyItem Created 538
MyItem Created 539
MyItem Created 540
MyItem Created 541
MyItem Created 542
MyItem Created 543
MyItem Created 544
MyItem Created 545
MyItem Created 546
MyItem Created 547
MyItem Created 548
MyItem Created 549
MyItem Created 550
MyItem Created 551
MyItem Created 552
MyItem Created 553
MyItem Created 554
MyItem Created 555
MyItem Created 556
MyItem Created 557
MyItem Created 558
MyItem Created 559
MyItem Created 560
MyItem Created 561
MyItem Created 562
MyItem Created 563
MyItem Created 564
MyItem Created 565
MyItem Created 566
MyItem Created 567
MyItem Created 568
MyItem Created 569
MyItem Created 570
MyItem Created 571
MyItem Created 572
MyItem Created 573
MyItem Created 574
MyItem Created 575
MyItem Created 576
MyItem Created 577
MyItem Created 578
MyItem Created 579
MyItem Created 580
MyItem Created 581
MyItem Created 582
MyItem Created 583
MyItem Created 584
MyItem Created 585
MyItem Created 586
MyItem Created 587
MyItem Created 588
MyItem Created 589
MyItem Created 590
MyItem Created 591
MyItem Created 592
MyItem Created 593
MyItem Created 594
MyItem Created 595
MyItem Created 596
MyItem Created 597
MyItem Created 598
MyItem Created 599
MyItem Created 600
MyItem Created 601
MyItem Created 602
MyItem Created 603
MyItem Created 604
MyItem Created 605
MyItem Created 606
MyItem Created 607
MyItem Created 608
MyItem Created 609
MyItem Created 610
MyItem Created 611
MyItem Created 612
MyItem Created 613
MyItem Created 614
MyItem Created 615
MyItem Created 616
MyItem Created 617
MyItem Created 618
MyItem Created 619
MyItem Created 620
MyItem Created 621
MyItem Created 622
MyItem Created 623
MyItem Created 624
MyItem Created 625
MyItem Created 626
MyItem Created 627
MyItem Created 628
MyItem Created 629
MyItem Created 630
MyItem Created 631
MyItem Created 632
MyItem Created 633
MyItem Created 634
MyItem Created 635
MyItem Created 636
MyItem Created 637
MyItem Created 638
MyItem Created 639
MyItem Created 640
MyItem Created 641
MyItem Created 642
MyItem Created 643
MyItem Created 644
MyItem Created 645
MyItem Created 646
MyItem Created 647
MyItem Created 648
MyItem Created 649
MyItem Created 650
MyItem Created 651
MyItem Created 652
MyItem Created 653
MyItem Created 654
MyItem Created 655
MyItem Created 656
MyItem Created 657
MyItem Created 658
MyItem Created 659
MyItem Created 660
MyItem Created 661
MyItem Created 662
MyItem Created 663
MyItem Created 664
MyItem Created 665
MyItem Created 666
MyItem Created 667
MyItem Created 668
MyItem Created 669
MyItem Created 670
MyItem Created 671
MyItem Created 672
MyItem Created 673
MyItem Created 674
MyItem Created 675
MyItem Created 676
MyItem Created 677
MyItem Created 678
MyItem Created 679
MyItem Created 680
MyItem Created 681
MyItem Created 682
MyItem Created 683
MyItem Created 684
MyItem Created 685
MyItem Created 686
MyItem Created 687
MyItem Created 688
MyItem Created 689
MyItem Created 690
MyItem Created 691
MyItem Created 692
MyItem Created 693
MyItem Created 694
MyItem Created 695
MyItem Created 696
MyItem Created 697
MyItem Created 698
MyItem Created 699
MyItem Created 700
MyItem Created 701
MyItem Created 702
MyItem Created 703
MyItem Created 704
MyItem Created 705
MyItem Created 706
MyItem Created 707
MyItem Created 708
MyItem Created 709
MyItem Created 710
MyItem Created 711
MyItem Created 712
MyItem Created 713
MyItem Created 714
MyItem Created 715
MyItem Created 716
MyItem Created 717
MyItem Created 718
MyItem Created 719
MyItem Created 720
MyItem Created 721
MyItem Created 722
MyItem Created 723
MyItem Created 724
MyItem Created 725
MyItem Created 726
MyItem Created 727
MyItem Created 728
MyItem Created 729
MyItem Created 730
MyItem Created 731
MyItem Created 732
MyItem Created 733
MyItem Created 734
MyItem Created 735
MyItem Created 736
MyItem Created 737
MyItem Created 738
MyItem Created 739
MyItem Created 740
MyItem Created 741
MyItem Created 742
MyItem Created 743
MyItem Created 744
MyItem Created 745
MyItem Created 746
MyItem Created 747
MyItem Created 748
MyItem Created 749
MyItem Created 750
MyItem Created 751
MyItem Created 752
MyItem Created 753
MyItem Created 754
MyItem Created 755
MyItem Created 756
MyItem Created 757
MyItem Created 758
MyItem Created 759
MyItem Created 760
MyItem Created 761
MyItem Created 762
MyItem Created 763
MyItem Created 764
MyItem Created 765
MyItem Created 766
MyItem Created 767
MyItem Created 768
MyItem Created 769
MyItem Created 770
MyItem Created 771
MyItem Created 772
MyItem Created 773
MyItem Created 774
MyItem Created 775
MyItem Created 776
MyItem Created 777
MyItem Created 778
MyItem Created 779
MyItem Created 780
MyItem Created 781
MyItem Created 782
MyItem Created 783
MyItem Created 784
MyItem Created 785
MyItem Created 786
MyItem Created 787
MyItem Created 788
MyItem Created 789
MyItem Created 790
MyItem Created 791
MyItem Created 792
MyItem Created 793
MyItem Created 794
MyItem Created 795
MyItem Created 796
MyItem Created 797
MyItem Created 798
MyItem Created 799
MyItem Created 800
MyItem Created 801
MyItem Created 802
MyItem Created 803
MyItem Created 804
MyItem Created 805
MyItem Created 806
MyItem Created 807
MyItem Created 808
MyItem Created 809
MyItem Created 810
MyItem Created 811
MyItem Created 812
MyItem Created 813
MyItem Created 814
MyItem Created 815
MyItem Created 816
MyItem Created 817
MyItem Created 818
MyItem Created 819
MyItem Created 820
MyItem Created 821
MyItem Created 822
MyItem Created 823
MyItem Created 824
MyItem Created 825
MyItem Created 826
MyItem Created 827
MyItem Created 828
MyItem Created 829
MyItem Created 830
MyItem Created 831
MyItem Created 832
MyItem Created 833
MyItem Created 834
MyItem Created 835
MyItem Created 836
MyItem Created 837
MyItem Created 838
MyItem Created 839
MyItem Created 840
MyItem Created 841
MyItem Created 842
MyItem Created 843
MyItem Created 844
MyItem Created 845
MyItem Created 846
MyItem Created 847
MyItem Created 848
MyItem Created 849
MyItem Created 850
MyItem Created 851
MyItem Created 852
MyItem Created 853
MyItem Created 854
MyItem Created 855
MyItem Created 856
MyItem Created 857
MyItem Created 858
MyItem Created 859
MyItem Created 860
MyItem Created 861
MyItem Created 862
MyItem Created 863
MyItem Created 864
MyItem Created 865
MyItem Created 866
MyItem Created 867
MyItem Created 868
MyItem Created 869
MyItem Created 870
MyItem Created 871
MyItem Created 872
MyItem Created 873
MyItem Created 874
MyItem Created 875
MyItem Created 876
MyItem Created 877
MyItem Created 878
MyItem Created 879
MyItem Created 880
MyItem Created 881
MyItem Created 882
MyItem Created 883
MyItem Created 884
MyItem Created 885
MyItem Created 886
MyItem Created 887
MyItem Created 888
MyItem Created 889
MyItem Created 890
MyItem Created 891
MyItem Created 892
MyItem Created 893
MyItem Created 894
MyItem Created 895
MyItem Created 896
MyItem Created 897
MyItem Created 898
MyItem Created 899
MyItem Created 900
MyItem Created 901
MyItem Created 902
MyItem Created 903
MyItem Created 904
MyItem Created 905
MyItem Created 906
MyItem Created 907
MyItem Created 908
MyItem Created 909
MyItem Created 910
MyItem Created 911
MyItem Created 912
MyItem Created 913
MyItem Created 914
MyItem Created 915
MyItem Created 916
MyItem Created 917
MyItem Created 918
MyItem Created 919
MyItem Created 920
MyItem Created 921
MyItem Created 922
MyItem Created 923
MyItem Created 924
MyItem Created 925
MyItem Created 926
MyItem Created 927
MyItem Created 928
MyItem Created 929
MyItem Created 930
MyItem Created 931
MyItem Created 932
MyItem Created 933
MyItem Created 934
MyItem Created 935
MyItem Created 936
MyItem Created 937
MyItem Created 938
MyItem Created 939
MyItem Created 940
MyItem Created 941
MyItem Created 942
MyItem Created 943
MyItem Created 944
MyItem Created 945
MyItem Created 946
MyItem Created 947
MyItem Created 948
MyItem Created 949
MyItem Created 950
MyItem Created 951
MyItem Created 952
MyItem Created 953
MyItem Created 954
MyItem Created 955
MyItem Created 956
MyItem Created 957
MyItem Created 958
MyItem Created 959
MyItem Created 960
MyItem Created 961
MyItem Created 962
MyItem Created 963
MyItem Created 964
MyItem Created 965
MyItem Created 966
MyItem Created 967
MyItem Created 968
MyItem Created 969
MyItem Created 970
MyItem Created 971
MyItem Created 972
MyItem Created 973
MyItem Created 974
MyItem Created 975
MyItem Created 976
MyItem Created 977
MyItem Created 978
MyItem Created 979
MyItem Created 980
MyItem Created 981
MyItem Created 982
MyItem Created 983
MyItem Created 984
MyItem Created 985
MyItem Created 986
MyItem Created 987
MyItem Created 988
MyItem Created 989
MyItem Created 990
MyItem Created 991
MyItem Created 992
MyItem Created 993
MyItem Created 994
MyItem Created 995
MyItem Created 996
MyItem Created 997
MyItem Created 998
MyItem Created 999
MyItem Created 1000
Recevied MyItem(id=2)
Recevied MyItem(id=3)
Recevied MyItem(id=4)
Recevied MyItem(id=5)
Recevied MyItem(id=6)
Recevied MyItem(id=7)
Recevied MyItem(id=8)
Recevied MyItem(id=9)
Recevied MyItem(id=10)
Recevied MyItem(id=11)
Recevied MyItem(id=12)
Recevied MyItem(id=13)
Recevied MyItem(id=14)
Recevied MyItem(id=15)
Recevied MyItem(id=16)
Recevied MyItem(id=17)
Recevied MyItem(id=18)
Recevied MyItem(id=19)
Recevied MyItem(id=20)
Recevied MyItem(id=21)
Recevied MyItem(id=22)
Recevied MyItem(id=23)
Recevied MyItem(id=24)
Recevied MyItem(id=25)
Recevied MyItem(id=26)
Recevied MyItem(id=27)
Recevied MyItem(id=28)
Recevied MyItem(id=29)
Recevied MyItem(id=30)
Recevied MyItem(id=31)
Recevied MyItem(id=32)
Recevied MyItem(id=33)
Recevied MyItem(id=34)
Recevied MyItem(id=35)
Recevied MyItem(id=36)
Recevied MyItem(id=37)
Recevied MyItem(id=38)
Recevied MyItem(id=39)
Recevied MyItem(id=40)
Recevied MyItem(id=41)
Recevied MyItem(id=42)
Recevied MyItem(id=43)
Recevied MyItem(id=44)
Recevied MyItem(id=45)
Recevied MyItem(id=46)
Recevied MyItem(id=47)
Recevied MyItem(id=48)
Recevied MyItem(id=49)
Recevied MyItem(id=50)
Recevied MyItem(id=51)
Recevied MyItem(id=52)
Recevied MyItem(id=53)
Recevied MyItem(id=54)
Recevied MyItem(id=55)
Recevied MyItem(id=56)
Recevied MyItem(id=57)
Recevied MyItem(id=58)
Recevied MyItem(id=59)
Recevied MyItem(id=60)
Recevied MyItem(id=61)
Recevied MyItem(id=62)
Recevied MyItem(id=63)
Recevied MyItem(id=64)
Recevied MyItem(id=65)
Recevied MyItem(id=66)
Recevied MyItem(id=67)
Recevied MyItem(id=68)
Recevied MyItem(id=69)
Recevied MyItem(id=70)
Recevied MyItem(id=71)
Recevied MyItem(id=72)
Recevied MyItem(id=73)
Recevied MyItem(id=74)
Recevied MyItem(id=75)
Recevied MyItem(id=76)
Recevied MyItem(id=77)
Recevied MyItem(id=78)
Recevied MyItem(id=79)
Recevied MyItem(id=80)
Recevied MyItem(id=81)
Recevied MyItem(id=82)
Recevied MyItem(id=83)
Recevied MyItem(id=84)
Recevied MyItem(id=85)
Recevied MyItem(id=86)
Recevied MyItem(id=87)
Recevied MyItem(id=88)
Recevied MyItem(id=89)
Recevied MyItem(id=90)
Recevied MyItem(id=91)
Recevied MyItem(id=92)
Recevied MyItem(id=93)
Recevied MyItem(id=94)
Recevied MyItem(id=95)
Recevied MyItem(id=96)
Recevied MyItem(id=97)
Recevied MyItem(id=98)
Recevied MyItem(id=99)
Recevied MyItem(id=100)
Recevied MyItem(id=101)
Recevied MyItem(id=102)
Recevied MyItem(id=103)
Recevied MyItem(id=104)
Recevied MyItem(id=105)
Recevied MyItem(id=106)
Recevied MyItem(id=107)
Recevied MyItem(id=108)
Recevied MyItem(id=109)
Recevied MyItem(id=110)
Recevied MyItem(id=111)
Recevied MyItem(id=112)
Recevied MyItem(id=113)
Recevied MyItem(id=114)
```

