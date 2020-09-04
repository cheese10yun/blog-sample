
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

## Cold Publisher & Hot Publisher

### Cold Publisher
![](image/cold-publisher.png)

* 생산자는 소비자가 구독 할떄마다 데이터를 처음부터 새로 통지한다.
* 데이터를 통지하는 새로운 타임 라인이 생성된다.
* 소비자는 구독 시점과 상관없이 통지된 데이터를 처음부더 전달 받을 수 있다.
* Flowable, Observable이 대표적인 Cold Publisher

```java
@Test
void Cold_Publisher_Example() {
    Flowable<Integer> flowable = Flowable.just(1, 3, 4, 7);

    flowable.subscribe(data -> System.out.println("구독자1: " + data));
    flowable.subscribe(data -> System.out.println("구독자2: " + data));
}

// 구독자1: 1
// 구독자1: 3
// 구독자1: 4
// 구독자1: 7
// 구독자2: 1
// 구독자2: 3
// 구독자2: 4
// 구독자2: 7
```
* 구독 순서와 상관 없이 

### Hot Publisher
![](image/hot-publisher.png)

* 생상자는 소비자 수와 상관없이 데이터를 한번만 통지한다
* 즉, 데이터를 통지하는 타임 라인은 하나이다.
* 소비자는 발행된 데이터를 처음부터 전달 받은게 아니라 구독한 시점에 통지된 데이터들만 전달 받을 수있다.

```java
@Test
void Hot_Publisher_Example() {
    PublishProcessor<Integer> processor = PublishProcessor.create();

    processor.subscribe(data -> System.out.println("구독자1: " + data));
    processor.onNext(1);
    processor.onNext(3);

    processor.subscribe(data -> System.out.println("구독자2: " + data));
    processor.onNext(4);
    processor.onNext(7);

    processor.onComplete();

//   구독자1: 1
//   구독자1: 3
//   구독자1: 4
//   구독자2: 4
//   구독자1: 7
//   구독자2: 7
}
```

# Flowable & Observable

| Flowable                                                 | Observable                                            |
| -------------------------------------------------------- | ----------------------------------------------------- |
| Reactive Streams 인터페이스를 구현함                     | Reactive Streams 인터페이스를 구현하지 않음           |
| Subscriber에서 데이터츷 처리한다.                        | Observer에서 데이터를 처리한다.                       |
| 데이터 개수를 제어하는 배압이 가능이 있음                | 데이터 개수를 제어하는 배압이 기능이 없음             |
| Subscription으로 전달 받은 데이터 개수를 제어할 수 있다. | 배갑 기능이 없기 떄문에 데이터 개수를 제어할 수 없다. |
| Subscription으로 구독을 해지한다.                        | Disposable로 구독을해지한다                           |

## 배압(Back Pressure)이란?
![](image/back-pressure.png)

Flowable에서 데이터를 통지하는 속다가 Subscriber에서 통지된 데이터를 전달받아 처리하는 속도 보다 빠를 때 밸런스를 맞추기 위해 데이터 통지량을 제어하는 기능을 말한다.

### 배압 기능이 없는 경우

```java
@Test
void missing_back_pressure() throws InterruptedException {
    Flowable.interval(1L, TimeUnit.MILLISECONDS)
        .doOnNext(data -> Logger.log(LogType.DO_ON_NEXT, data))
        .observeOn(Schedulers.computation())
        .subscribe(
            data -> {
                Logger.log(LogType.PRINT, "# 소비자 처리 대기 중..");
                TimeUtil.sleep(1000L);
                Logger.log(LogType.ON_NEXT, data);
            },
            error -> Logger.log(LogType.ON_ERROR, error),
            () -> Logger.log(LogType.ON_COMPLETE)
        );

    Thread.sleep(2000L);
}
doOnNext() | RxComputationThreadPool-2 | 00:24:14.790 | 0
doOnNext() | RxComputationThreadPool-2 | 00:24:14.794 | 1
doOnNext() | RxComputationThreadPool-2 | 00:24:14.795 | 2
print() | RxComputationThreadPool-1 | 00:24:14.794 | # 소비자 처리 대기 중..
...

onNext() | RxComputationThreadPool-1 | 00:24:15.796 | 0
onERROR() | RxComputationThreadPool-1 | 00:24:15.796 | io.reactivex.exceptions.MissingBackpressureException: Can't deliver value 128 due to lack of requests
```
`doOnNext()`는 `interval()`에서 통지하는 데이터를 처리하는 Callback 함수

생산자 쪽에서 통지한 첫 번쨰 데이터만 처리 `onNext() | RxComputationThreadPool-1 | 00:24:15.796 | 0` 하고 `onERROR() | RxComputationThreadPool-1 | 00:24:15.796 | io.reactivex.exceptions.MissingBackpressureException: Can't deliver value 128 due to lack of requests`예외가 발생, 생사자 쪽에서 통지하는 속도가 소비자에서 처리하는 속도 보다 빠르기 때문에 예외가 발생한다. 이러한 불균형을 처리하기 위해서 배압전략을 지원한다

## 배압 전략
RxJava에서는 BackpressureStrategy를 통해서 Flowable이 통지 대기 중 데이터를 어떻게 다룰지에 대한 배압 전략을 제공한다.

### Missing 전략
* 배압을 적용하지 않는다.
* 나중에 `OnBackpressureXXX()`로 배압 적용을 할 수 있다.

### Error 전략
* 통지된 데이터가 버퍼의 크기를 초과하면 `MissingBackpressureException` 에러를 통지한다.
* 즉, 소비자가 생산자의 ㅗㅇ지 속도를 따라 잡지 못할 떄 발생한다.

### Buffer 전략

#### Drop Latest
![](image/drop-latest.png)
* 버퍼가 가득 찬 시점에 가장 먼저 Drop이 된 데이터를 기억해두었다가 버퍼를 비우게되면 기억해둔 데이터부터 버퍼에 담는다.
* `10` 까지 버퍼에 들어간 경우 `11` 부터 데이터를 `drop` 하게된다. 이후 해당 버퍼가 모두 비워지면 `11`부터(drop된 순서 부터) 차례대로 진행된다.

```java
@Test
void back_pressure_drop() {
    Flowable.interval(1L, TimeUnit.MILLISECONDS)
        .onBackpressureBuffer(
            128,
            () -> Logger.log(LogType.PRINT, "# Overflow 발생!"),
            BackpressureOverflowStrategy.DROP_LATEST
        )
        .doOnNext(data -> Logger.log(LogType.DO_ON_NEXT, data))
        .observeOn(Schedulers.computation())
        .subscribe(
            data -> {
                TimeUtil.sleep(5L);
                Logger.log(LogType.ON_NEXT, data);
            },
            error -> Logger.log(LogType.ON_ERROR, error)
        );

    TimeUtil.sleep(1000L);
}
```
* 생산자 쪽에서는 `interval()`를 통해서 `1 MILLISECONDS` 주기로 0 부터 숫자를 차례대로 빠르게 통지함
* `onBackpressureBuffer()`
    * `128` 버퍼의 사이즈를 의미, Default value 128
    * 버퍼가 가득 찼을때 호출되는 Callback 함수
    * 버퍼가 가득 찼을때 사용할 전략 
* `subscribe()`
    * 구독하는 소비자 쪽에서는 `5 MILLISECONDS` 대기 시간을 갖으면서, 생산자 쪽에서 전달받은 데이터를 처리를 진행한다.
    * 속도의 차이가 있기 떄문에 배압 전략이 필요하다
    
```
doOnNext() | RxComputationThreadPool-2 | 00:43:25.340 | 0
doOnNext() | RxComputationThreadPool-2 | 00:43:25.343 | 1
doOnNext() | RxComputationThreadPool-2 | 00:43:25.343 | 2
...
onNext() | RxComputationThreadPool-1 | 00:43:25.348 | 0
...
onNext() | RxComputationThreadPool-1 | 00:43:25.585 | 40
print() | RxComputationThreadPool-2 | 00:43:25.586 | # Overflow 발생!
print() | RxComputationThreadPool-2 | 00:43:25.587 | # Overflow 발생!
...
onNext() | RxComputationThreadPool-1 | 00:43:25.898 | 95
doOnNext() | RxComputationThreadPool-1 | 00:43:25.898 | 128
```
1. 생산하는 쪽의 속도가 구독하는 쪽의 속도보다 훨씬 빠름, `doOnNext()`은 여러번 호출되지만, `onNext()`는 호출 횟수가 낮음
2. 버퍼가 가득 차는 시점에 `# Overflow 발생!`이 발생, buffe size는 `128`이지만, 버퍼가 비워지는 실제 사이지는 `95`
3. `95`까지 구독 처리를 완료한 이후 버퍼가 지워짐, `buffer size 128`이기 때문에 가장 먼저 drop된 `128`부 buffer에 채워지게 된다. 

#### Drop Oldest
![](image/drop-oldest.png)

* 버퍼가 가득 찬 시점에서 가장 마지막에 Drop된 데이터를 기억해두었다가 버퍼를 비우게되면 기억해둔 데이터부터 버퍼에 담는다.
* `1 ~ 10` 버퍼 크기가 가득 찼다면, 통지된 데이터들은 뒤에 쌓이게 된다.
* 버퍼가 비워질때 까지 기다리다가 버퍼가 비워지면 가장 마지막에 Drop된 `14`번부터 차례대로 버퍼에 담긴다.
* `11 ~ 13`까지 데이터의 손실이 생긴다.

```java
@Test
void back_pressure_drop_oldest() {
    Flowable.interval(1L, TimeUnit.MILLISECONDS)
        .onBackpressureBuffer(
            128,
            () -> Logger.log(LogType.PRINT, "# Overflow 발생!"),
            BackpressureOverflowStrategy.DROP_OLDEST
        )
        .doOnNext(data -> Logger.log(LogType.DO_ON_NEXT, data))
        .observeOn(Schedulers.computation())
        .subscribe(
            data -> {
                TimeUtil.sleep(5L);
                Logger.log(LogType.ON_NEXT, data);
            },
            error -> Logger.log(LogType.ON_ERROR, error)
        );

    TimeUtil.sleep(1000L);
}
```
* 생산자 쪽에서는 `interval()`를 통해서 `1 MILLISECONDS` 주기로 0 부터 숫자를 차례대로 빠르게 통지함
* `onBackpressureBuffer()`를 통해서 `DROP_OLDEST` 전략을 지정, Orverflow가 발생했을 경우 처리할 Callback 메서드 지정
* `doOnNext()` 생산자 쪽에서 통지한 데이터를 그대로 출력
* `subscribe` 생상자 쪽에서 통지한 데이터를 구독해서 처리함

```
doOnNext() | RxComputationThreadPool-2 | 01:17:56.609 | 0
doOnNext() | RxComputationThreadPool-2 | 01:17:56.612 | 1
doOnNext() | RxComputationThreadPool-2 | 01:17:56.612 | 2
...
onNext() | RxComputationThreadPool-1 | 01:17:56.618 | 0
...
doOnNext() | RxComputationThreadPool-2 | 01:17:56.724 | 127
...
print() | RxComputationThreadPool-2 | 01:17:56.853 | # Overflow 발생!
...
onNext() | RxComputationThreadPool-1 | 01:17:57.152 | 95
doOnNext() | RxComputationThreadPool-1 | 01:17:57.152 | 428
doOnNext() | RxComputationThreadPool-1 | 01:17:57.152 | 429
doOnNext() | RxComputationThreadPool-1 | 01:17:57.152 | 430
...
```
1. 통지한 데이터를 출력
2. 버퍼 사이즈를 128으로 지정했기 때문에 `0 ~ 127`까지 버퍼에 채워짐
3. `128` 부터 Overflow가 계속 발 `95`까지 데이터를 통지한 데이터를 구독해서 처리
4. `Drop Oldest`전략이기 떄문에 가장 마지막에 발생한 `Overflow 발생!` 이후 통지한 데이터 `428` 부터 차례대로 버퍼에 쌓기 시작함
5. `127 ~ 427` 통지한 데이터는 유실이 발생

### Drop
![](/image/drop.png)

* 버퍼에 데이터가 모두 채워진 상태가 되면 이후에 생성되는 데이터를 버리고(Drop), 버퍼가 비워지는 시점에 Drop되지 않은 데이터 부터 다시 버퍼에 담는다.
* `1 ~ 10` 버퍼 크기가 가득 찼다면, 통지된 데이터들은 뒤에 쌓이게 된다.
* 버퍼가 비워질때 까지 기다리다가 버퍼가 비워지면 Drop이 데이터는 모두 버리 Drop이 되지 않은 데이터 부터 버퍼에 채운다.
* `Drop Oldest`같은 경우 마지막에 Drop된 데이터 `14`부터 들어가게 되기 떄문에 차이가 있다.

```java
@Test
void back_pressure_drop() {
    Flowable.interval(1L, TimeUnit.MILLISECONDS)
        .onBackpressureDrop(dropData -> Logger.log(LogType.PRINT, "오버플로우 발생! - " + dropData + " Drop!"))
        .doOnNext(data -> Logger.log(LogType.ON_NEXT, data))
        .observeOn(Schedulers.computation())
        .subscribe(
            data -> {
                TimeUtil.sleep(5L);
                Logger.log(LogType.ON_NEXT, data);
            }
        );

    TimeUtil.sleep(1000L);
}
```

```
onNext() | RxComputationThreadPool-2 | 01:44:50.547 | 0
onNext() | RxComputationThreadPool-2 | 01:44:50.550 | 1
onNext() | RxComputationThreadPool-2 | 01:44:50.550 | 2
...
onNext() | RxComputationThreadPool-1 | 01:44:50.556 | 0
...
onNext() | RxComputationThreadPool-2 | 01:44:50.662 | 127
print() | RxComputationThreadPool-2 | 01:44:50.664 | 오버플로우 발생! - 128 Drop!
...

onNext() | RxComputationThreadPool-1 | 01:44:51.095 | 95
onNext() | RxComputationThreadPool-2 | 01:44:51.095 | 560
onNext() | RxComputationThreadPool-2 | 01:44:51.097 | 561
...

```
1. 통지한 데이터를 출력
2. 버퍼 사이즈를 128으로 지정했기 때문에 `0 ~ 127`까지 버퍼에 채워짐
3. `128` 부터 Overflow가 계속 발생 `95`까지 데이터를 통지한 데이터를 구독해서 처리
4. `Drop`전략이기 떄문에 Drop된 데이터는 제거 이후 통지된 데이터 `540` 부터 버퍼에 차례대로 쌓기 시작함

### Latest 전략

![](/image/drop-2.png)

* 버퍼에 데이터가 모두 채워진 상태가 되면 버퍼가 비워질 때 까지 통지된 데이터는 버퍼 밖에서 대기하며 버퍼가 비워지는 시점에 가장 가장 최근 통지된 데이터 부터 버퍼에 담는다.
* `1 ~ 10` 버퍼가 가득찼다면, 가장 나중에 통지된 `20` 데이터 부터 버퍼 채워지게 된다.

```java
@Test
void back_pressure_latest() {
    Flowable.interval(1L, TimeUnit.MILLISECONDS)
        .onBackpressureLatest()
        .doOnNext(data -> Logger.log(LogType.DO_ON_NEXT, data))
        .observeOn(Schedulers.computation())
        .subscribe(
            data -> {
                TimeUtil.sleep(5L);
                Logger.log(LogType.ON_NEXT, data);
            },
            error -> Logger.log(LogType.ON_ERROR, error)
        );

    TimeUtil.sleep(1000L);
}
```

```
doOnNext() | RxComputationThreadPool-2 | 01:55:33.720 | 0
doOnNext() | RxComputationThreadPool-2 | 01:55:33.724 | 1
...
onNext() | RxComputationThreadPool-1 | 01:55:33.729 | 0
...
doOnNext() | RxComputationThreadPool-2 | 01:55:33.836 | 127
...
onNext() | RxComputationThreadPool-1 | 01:55:34.288 | 95
doOnNext() | RxComputationThreadPool-1 | 01:55:34.288 | 579

...
```
* `127`까지 통지하게 되면 버퍼가 가득차게됨
* 소비자 측에서는 `95`까지 전달 받은 데이터를 처리 하고 버퍼가 지워지게
* 버퍼가 지워지는 시점에 가장 마지막에 통지된 데이터가 `579`이기 때문에 처리