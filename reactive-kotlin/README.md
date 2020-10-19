> 해당 내용은 [코틀린 리액티브 프로그래밍](http://www.kyobobook.co.kr/product/detailViewKor.laf?mallGb=KOR&ejkGb=KOR&barcode=9791161752976&orderClick=LA6) 을 보고 정리한 내용 입니다.

# 1장 리액티브 프로그래밍의 소개

## 리액티브 프로그랴밍이란 무엇인가 ?

리액티브 프로그래밍은 데이터 스트림과 변경 사항 전파를 중심으로 하는 비동기 프로그래밍 패러다임이다. 간단하게 말하면 데이터와 데이터 스트림에 영향을 미치는 모든 변경 사항을 관련된 모든 당사자들, 예를 들면 최종 사용자나 컴포넌트, 하위 구성 요소, 또는 어떻게든 연결돼 있는 다른 프로그램 등에게 전파하는 프로그램을 리액티브 프로그래램이라고 한다.


# 3장 옵저버블과 옵저버와 구독자

## 옵저버블

리액티브 프로그래밍에서 옵저버블은 **그 컨슈머가 소비할 수 있는 값을 산출해 내는 기본 계산 작업을 갖고 있다.** 여기서 중요한 것은 컨슈머가 값을 풀 방식으로 접근하지 않는다는 점이다. 오히려 **옵저버블은 선슈머에게 값을 푸쉬하는 역할을 한다. 따라서 옵저버블은 인련의 연산자를 거친 아이템을 최종 옵저버로 보내는 푸시 기반의 조합 가능한 이터레이터다.**

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
