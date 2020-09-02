
# 리액티브 프로그래밍 이란?

## 정의
**변화의 전파**와 **데이터 흐름**과 관련된 **선언적 프로그래밍** 패러다임이다.

* 변화의 전파와 데이터 흐름: 데이터가 변경 될 떄마다 이벤트를 발생시켜 데이터를 계속적으로 전달 한다
* 선언적 프로그래밍: 실행할 동작을 구체적으로 명시하는 프로그래밍과 달리 선언형 프로그래밍은 단순히 목표를 선언한다.

```java
class Test {
    @Test
    void 선언형프로그래밍() {
        // List에 있는 숫자들 중에 6보다 큰 홀수들의 합계를 구하시오
        final List<Integer> numbers = Arrays.asList(1, 3, 21, 10, 8, 11);
        int sum = 0;

        for (Integer number : numbers) {
            if (number > 6 && (number % 2 != 0)) {
                sum += number;
            }
        }

        System.out.println("명령형 프로그래밍 사용 : " + sum);
    }

    @Test
    void 선언적프로그래밍() {
        // List에 있는 숫자들 중에 6보다 큰 홀수들의 합계를 구하시오
        final List<Integer> numbers = Arrays.asList(1, 3, 21, 10, 8, 11);

        final int sum = numbers.stream()
            .filter(number -> number > 6 && (number % 2 != 0))
            .mapToInt(number -> number)
            .sum();

        System.out.println("명령형 프로그래밍 사용 : " + sum);
    }
}
```

## 리액티브의 개념이 적용된 예

### Push 방식
데이터의 변화가 발생했을 때 변경이 발생한 곳에서 데이터를 보내주는 방식

* RTC
* 소켓 프로그래밍
* DB Trigger
* Spring ApplicationEvent
* 스마트폰의 Push 메시지

### Pull 방식
변경된 데이터가 있는지 요청을 보내고 질의하고 변경된 데이터를 가져오는 방식
*  클라이언트 요청 & 서버 응답 방식의 애플리케이션
* Java와 같은 절차형 프로그래밍 언어

## 리엑티브 프로그래밍을 위해 알아야 될 것들
* Observable: 데이터 소스
* 리액티브 연산자: 데이터 소스를 처리하는 함수
* 스케줄러: 스레드 관리자
* Subscriber: Observable이 발행하는 데이터를 구독하는 구독자
* 함수형 프로그래밍: RxJava에서 제공하는 연산자 함수를 사


```java
@Test
void 리액티브프로그래밍() throws InterruptedException {
    Observable.just(100, 200, 300, 400, 500)
//        .doOnNext(data -> System.out.println(getThreadNAme() + " : doOnNext() :" + data))
//        .subscribeOn(Schedulers.io())
//        .observeOn(Schedulers.computation())
        .filter(number -> number > 300)
        .subscribe(num -> System.out.println(getThreadNAme() + " : result : " + num));

//    Thread.sleep(500);

// 결과
//Test worker : result : 400
//Test worker : result : 500
}
```
* `just`를 통해서 데이터를 발행한다
* `filter`를 통해서 300 이상인 것을 필터링한다고 선언한다
* `subscribe` 발행된 데이터, 필터링된 데이터를 출력한다.
* 데이터를 발행- > 데이터를 가공 -> 데이터를 구독해서 처리
* `Test worker` 쓰레드에서 실행된다.

```java
@Test
void 리액티브프로그래밍() throws InterruptedException {
    Observable.just(100, 200, 300, 400, 500)
        .doOnNext(data -> System.out.println(getThreadNAme() + " : doOnNext() :" + data))
//            .subscribeOn(Schedulers.io())
//            .observeOn(Schedulers.computation())
        .filter(number -> number > 300)
        .subscribe(num -> System.out.println(getThreadNAme() + " : result : " + num));

    Thread.sleep(500);

//   Test worker : doOnNext() :100
//   Test worker : doOnNext() :200
//   Test worker : doOnNext() :300
//   Test worker : doOnNext() :400
//   Test worker : result : 400
//   Test worker : doOnNext() :500
//   Test worker : result : 500
}
```
* 동일하게 `Test worker` 스레드에서 실
* `doOnNext()` 데이터가 발행될 떄마다 실행된다.

```java
@Test
void 리액티브프로그래밍() throws InterruptedException {
    Observable.just(100, 200, 300, 400, 500)
        .doOnNext(data -> System.out.println(getThreadNAme() + " : doOnNext() :" + data))
        .subscribeOn(Schedulers.io())
//            .observeOn(Schedulers.computation())
        .filter(number -> number > 300)
        .subscribe(num -> System.out.println(getThreadNAme() + " : result : " + num));

//        Thread.sleep(500);
//   RxCachedThreadScheduler-1 : doOnNext() :100
//   RxCachedThreadScheduler-1 : doOnNext() :200
//   RxCachedThreadScheduler-1 : doOnNext() :300
//   RxCachedThreadScheduler-1 : doOnNext() :400
//   RxCachedThreadScheduler-1 : result : 400
//   RxCachedThreadScheduler-1 : doOnNext() :500
//   RxCachedThreadScheduler-1 : result : 500

}
```
* `subscribeOn()`를 실행하게되면 `RxCachedThreadScheduler` 스레드에서 실행된다.

```java
@Test
void 리액티브프로그래밍() throws InterruptedException {
    Observable.just(100, 200, 300, 400, 500)
        .doOnNext(data -> System.out.println(getThreadNAme() + " : doOnNext() :" + data))
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.computation())
        .filter(number -> number > 300)
        .subscribe(num -> System.out.println(getThreadNAme() + " : result : " + num));

//        Thread.sleep(500);
//   RxCachedThreadScheduler-1 : doOnNext() :100
//   RxCachedThreadScheduler-1 : doOnNext() :200
//   RxCachedThreadScheduler-1 : doOnNext() :300
//   RxCachedThreadScheduler-1 : doOnNext() :400
//   RxCachedThreadScheduler-1 : doOnNext() :500
//   RxComputationThreadPool-1 : result : 400
//   RxComputationThreadPool-1 : result : 500
}
```
* `subscribeOn()` 데이터의 발행, 데이터의 흐름을 결정하는 스레드
* `observeOn()` 발행된 데이터를 가공하고, 구독 해서 처리하는 것을 담당하는 스레
* 데이터를 발행 -> 데이터 가공 -> 데이터 구독

# 마블 다이어그램이란?
리액티브 프로그래밍을 통해 발생하는 비동기적인 데이터의 흐름을 시각의 흐름에 따라 시각적으로 표시시한 다어그램

## 마블 다이어그램을 알아야하는 이유
* 문장으로 적혀 있는 리액티브 연산자의 기능을 이해하기 어려움
* 리액티브 연산자의 기능이 시각화 되어 있어서 이해하기 쉬움
* 리액티브 프로그래밍의 핵심 연산자를 사용하기 위한 핵심 도구

# Reactive Streams란 ?

* 리액티브 프로그래밍 라이브러리의 표준 사양이다
* RxJava는 Reactive Streams의 인터페이스들을 구현한 구현체이다.
* Reactive Streams는 Publisher, Subscriber, Subscription, Processor 라는 4개의 인터페이스를 제공한다.
    * Publisher: 데이터를 생성하고 통지한다.
    * Subscriber: 통지된 데이터를 전달받아서 처리한다.
    * Subscription: 전달 받을 데이터의 개수를 요청하고 구독을 해지한다.
    * Processor: Publisher, Subscriber의 기능이 모두 있

## Publisher와 Subscriber간의 프로세스 흐름
```plantuml
Publisher <-- Subscriber: 데이터를 구독한다. Subscribe
Publisher --> Subscriber: 데이터를 통지할 준비가 되었음을 알린다. OnSubscribe
Publisher <-- Subscriber: 전달 받은 통지 데이터 개수를 요청한다. Subscription.request
Publisher --> Publisher: 데이터를 생성한다.
Publisher --> Subscriber: 요청 받은 개수만큼 데이터를 총지한다. OnNext
Publisher --> Publisher: 데이터를 생성한다.
Publisher --> Publisher: 데이터를 생성한다.
Publisher <-- Subscriber: 전달 받을 통지 데이터 개수를 요청한다. Subscription.request
Publisher --> Subscriber: 요청 받은 개수만큼 데이터를 통지한다. OnNext
Publisher --> Subscriber: 데이터 통지가 완료 되었음을 알린다. OnComplete
```
