package com.example.reactorstudy;

import com.example.reactorstudy.common.Car;
import com.example.reactorstudy.common.CarMaker;
import com.example.reactorstudy.common.SampleData;
import com.example.reactorstudy.util.DateUtil;
import com.example.reactorstudy.util.LogType;
import com.example.reactorstudy.util.Logger;
import com.example.reactorstudy.util.NumberUtil;
import com.example.reactorstudy.util.TimeUtil;
import io.reactivex.BackpressureOverflowStrategy;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeObserver;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;


class ReactorStudyApplicationTests {

    @Test
    void name() {
        Flux.range(1, 100)
            .subscribe(
                data -> System.out.println(MessageFormat.format("onNext:'{'{0}'}'", data)),
                err -> System.out.println("error ignore"),
                () -> System.out.println("onComplete"),
                subscription -> {
                    subscription.request(4);
                    subscription.cancel();
                }
            )
        ;
    }

//    @Test
//    void test01() throws InterruptedException {
//        final Disposable disposable = Flux.interval(Duration.ofMillis(50))
//            .subscribe(
//                data -> System.out.println(MessageFormat.format("onNext:'{'{0}'}'", data))
//            );
//
//        Thread.sleep(200);
//        disposable.dispose();
//    }

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

    @Test
    void 리액티브프로그래밍() throws InterruptedException {
        Observable.just(100, 200, 300, 400, 500)
            .doOnNext(data -> System.out.println(getThreadNAme() + " : doOnNext() :" + data))
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .filter(number -> number > 300)
            .subscribe(num -> System.out.println(getThreadNAme() + " : result : " + num));

//        Thread.sleep(500);
    }

    private static String getThreadNAme() {
        return Thread.currentThread().getName();
    }


    @Test
    void Cold_Publisher_Example() {
        Flowable<Integer> flowable = Flowable.just(1, 3, 4, 7);

        flowable.subscribe(data -> System.out.println("구독자1: " + data));
        flowable.subscribe(data -> System.out.println("구독자2: " + data));
    }

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
    }

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

    @Test
    void back_pressure_drop_latest() {
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

    @Test
    void flow_able_example() throws InterruptedException {
        Flowable<String> flowable = Flowable.create(
            new FlowableOnSubscribe<String>() {
                @Override
                public void subscribe(FlowableEmitter<String> emitter) throws Exception {
                    String[] datas = {"Hello", "RxJava"};
                    for (final String data : datas) {
                        // 구독이 해지되면 처리 중단
                        if (emitter.isCancelled()) {
                            return;
                        }
                        // 데이터 통지
                        emitter.onNext(data);
                    }
                    emitter.onComplete();
                }
            },
            BackpressureStrategy.BUFFER
        );

        flowable.observeOn(Schedulers.computation())
            .subscribe(new Subscriber<String>() {
                // 데이터 개수 요청 및 구독을 취소하기 위한 객체
                private Subscription subscription;

                @Override
                public void onSubscribe(Subscription subscription) {
                    this.subscription = subscription;
                    this.subscription.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(String data) {
                    Logger.log(LogType.ON_NEXT, data);
                }

                @Override
                public void onError(Throwable error) {
                    Logger.log(LogType.ON_ERROR);
                }

                @Override
                public void onComplete() {
                    Logger.log(LogType.ON_COMPLETE);
                }
            });
    }

    @Test
    void observablee_example() throws InterruptedException {
        Observable<String> observablee = Observable.create(
            new ObservableOnSubscribe<String>() {
                @Override
                public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                    String[] datas = {"Hello", "RxJava"};
                    for (final String data : datas) {
                        // 구독 해지가 돠면 처리 중단
                        if (emitter.isDisposed()) {
                            return;
                        }
                        emitter.onNext(data);
                    }
                    emitter.onComplete();
                }
            }
        );

        observablee.observeOn(Schedulers.computation())
            .subscribe(new Observer<String>() {
                @Override
                public void onSubscribe(Disposable d) {
                    // 이무것도 처리하지 않음
                }

                @Override
                public void onNext(String data) {
                    Logger.log(LogType.ON_NEXT, data);
                }

                @Override
                public void onError(Throwable error) {
                    Logger.log(LogType.ON_ERROR, error);
                }

                @Override
                public void onComplete() {
                    Logger.log(LogType.ON_COMPLETE);
                }
            });

        Thread.sleep(500L);
    }

    @Test
    void single_example() {
        Single<String> single = Single.create(
            new SingleOnSubscribe<String>() {
                @Override
                public void subscribe(SingleEmitter<String> emitter) throws Exception {
                    emitter.onSuccess(DateUtil.getNowDate());
                }
            }
        );

        single.subscribe(new SingleObserver<String>() {
            @Override
            public void onSubscribe(Disposable disposable) {
                // 아무것도하지 않음
            }

            @Override
            public void onSuccess(String data) {
                Logger.log(LogType.ON_SUBSCRIBE, "# 날짜시각: " + data);
            }

            @Override
            public void onError(Throwable error) {
                Logger.log(LogType.ON_ERROR, error);
            }
        });
    }

    @Test
    void single_just_example() {

        Single.just(DateUtil.getNowDate())
            .subscribe(
                data -> Logger.log(LogType.ON_SUBSCRIBE, "# 날짜시각: " + data),
                error -> Logger.log(LogType.ON_ERROR, error)
            );
    }

    @Test
    void maybe_example() {
        final Maybe<String> maybe = Maybe.create(
            new MaybeOnSubscribe<String>() {
                @Override
                public void subscribe(MaybeEmitter<String> emitter) throws Exception {
//                    emitter.onSuccess(DateUtil.getNowDate());
                    emitter.onComplete();
                }
            }
        );

        maybe.subscribe(
            new MaybeObserver<String>() {
                @Override
                public void onSubscribe(Disposable disposable) {
                    // 아무것도하지 않음
                }

                @Override
                public void onSuccess(String data) {
                    Logger.log(LogType.ON_SUCCESS, data);
                }

                @Override
                public void onError(Throwable error) {
                    Logger.log(LogType.ON_ERROR, error);
                }

                @Override
                public void onComplete() {
                    Logger.log(LogType.ON_COMPLETE);
                }
            }
        );
    }

    @Test
    void completable_example() {
        final Completable completable = Completable.create(
            new CompletableOnSubscribe() {
                @Override
                public void subscribe(CompletableEmitter emitter) throws Exception {
                    int sum = 0;

                    for (int i = 0; i < 100; i++) {
                        sum = sum + 1;
                    }
                    Logger.log(LogType.PRINT, "# 합계: " + sum);
                    emitter.onComplete();
                }
            }
        );

        completable.subscribe(
            new CompletableObserver() {
                @Override
                public void onSubscribe(final Disposable disposable) {
                    // 아무것도 하지 않음
                }

                @Override
                public void onComplete() {
                    Logger.log(LogType.ON_COMPLETE);
                }

                @Override
                public void onError(final Throwable error) {
                    Logger.log(LogType.ON_ERROR, error);
                }
            }
        );
    }

    @Test
    void observablee_interval() {
        Observable.interval(0, 1000L, TimeUnit.MILLISECONDS)
            .map(num -> num + "count")
            .subscribe(data -> Logger.log(LogType.ON_NEXT, data))
        ;

        TimeUtil.sleep(3000L);
    }

    @Test
    void observablee_range() {
        Observable.range(0, 5)
            .subscribe(num -> Logger.log(LogType.ON_NEXT, num))
        ;
    }

    @Test
    void observablee_timer() {
        Logger.log(LogType.PRINT, "# Start");
        final Observable<String> observablee = Observable.timer(2000, TimeUnit.MILLISECONDS)
            .map(count -> "Do work");

        observablee.subscribe(data -> Logger.log(LogType.ON_NEXT, data));
        TimeUtil.sleep(3000L);
    }

    @Test
    void observablee_defer() {
        final Observable<LocalTime> observablee = Observable.defer(() -> Observable.just(LocalTime.now()));
        final Observable<LocalTime> observableeJust = Observable.just(LocalTime.now());

        observablee.subscribe(timer -> Logger.log(LogType.PRINT, "# defer() 구독1의 구독 시간:" + timer));
        observableeJust.subscribe(timer -> Logger.log(LogType.PRINT, "# just() 구독1의 구독 시간:" + timer));

        TimeUtil.sleep(3000L);

        observablee.subscribe(timer -> Logger.log(LogType.PRINT, "# defer() 구독2의 구독 시간:" + timer));
        observableeJust.subscribe(timer -> Logger.log(LogType.PRINT, "# just() 구독2의 구독 시간:" + timer));
    }

    @Test
    void observablee_iterable() {
        final List<String> countries = Arrays.asList("Korea", "Canada", "USA", "Italy");

        Observable.fromIterable(countries)
            .subscribe(country -> Logger.log(LogType.ON_NEXT, country));
    }

    @Test
    void observablee_fromFuture() {
        Logger.log(LogType.PRINT, "# Start time");

        // 긴 처리 시간이 걸리는 작업
        Future<Double> future = longTimeWork();

        // 짭은 처리 시간이 걸리는 작업
        shortTimeWork();

        Observable.fromFuture(future)
            .subscribe(data -> Logger.log(LogType.PRINT, "# 긴 처리 시간 자업 결과" + data));

        Logger.log(LogType.PRINT, "# End time");
    }

    public static CompletableFuture<Double> longTimeWork() {
        return CompletableFuture.supplyAsync(() -> calculate());
    }

    private static Double calculate() {
        Logger.log(LogType.PRINT, "# 긴 처리 시간이 걸리는 작업 중.........");
        TimeUtil.sleep(6000L);
        return 100000000000000000.0;
    }

    private static void shortTimeWork() {
        TimeUtil.sleep(3000L);
        Logger.log(LogType.PRINT, "# 짧은 처리 시간 작업 완료!");
    }

    @Test
    void observablee_filter() {
        Observable.fromIterable(SampleData.carList)
            .filter(car -> car.getCarMaker() == CarMaker.CHEVROLET)
            .subscribe(car -> Logger.log(LogType.ON_NEXT, car.getCarMaker() + " : " + car.getCarName()));
    }

    @Test
    void observablee_distinct() {
        Observable.fromArray(SampleData.carMakersDuplicated)
            .distinct()
            .subscribe(carMaker -> Logger.log(LogType.ON_NEXT, carMaker));
    }

    @Test
    void observablee_take_개수만큼() {
        Observable.just("a", "b", "c", "d")
            .take(2)
            .subscribe(data -> Logger.log(LogType.ON_NEXT, data));
    }

    @Test
    void observablee_take_지정한_시간() {
        // 1초 간격으로 interval 진행
        Observable.interval(1000L, TimeUnit.MILLISECONDS)
            .take(3500L, TimeUnit.MILLISECONDS)
            .subscribe(data -> Logger.log(LogType.ON_NEXT, data));

        // 3.5초 스레드 슬립이기 때문에, 0 ~ 2 까지 소비한다.
        TimeUtil.sleep(3500L);
    }

    @Test
    void observablee_map() {
        final List<Integer> numbers = Arrays.asList(1, 3, 5, 7);
        Observable.fromIterable(numbers)
            .map(num -> "1을 더한 결과" + (num + 1))
            .subscribe(num -> System.out.println(num));
    }

    @Test
    void observablee_flat_map() {
        Observable.just("Hello")
            .flatMap(hello -> Observable.just("JAVA", "Kotlin", "Spring").map(lang -> hello + ", " + lang))
            .subscribe(data -> System.out.println(data));
    }

    @Test
    void observablee_flat_map_2() {
        Observable.range(2, 1)
            .flatMap(
                num -> Observable.range(1, 9)
                    .map(row -> num + " * " + row + " = " + num * row)
            )
            .subscribe(System.out::println);
    }

    @Test
    void observablee_flat_map_3() {
        Observable.range(2, 1)
            .flatMap(
                data -> Observable.range(1, 9),
                (sourceData, transformedData) -> sourceData + " * " + transformedData + " = "
                    + sourceData * transformedData
            )
            .subscribe(System.out::println);
    }

    @Test
    void observablee_concat_map() {
        TimeUtil.start();
        Observable.interval(100L, TimeUnit.MILLISECONDS)
            .take(4)
            .skip(2)
            .concatMap(
                num -> Observable.interval(200L, TimeUnit.MILLISECONDS)
                    .take(10)
                    .skip(1)
                    .map(row -> num + " * " + row + " = " + num * row)
            )
            .subscribe(
                data -> Logger.log(LogType.ON_NEXT, data),
                error -> {
                },
                () -> {
                    TimeUtil.end();
                    TimeUtil.takeTime();
                }
            );
        TimeUtil.sleep(5000L);
    }

    @Test
    void observablee_flat_map_4() {
        TimeUtil.start();
        Observable.interval(100L, TimeUnit.MILLISECONDS)
            .take(4)
            .skip(2)
            .flatMap(
                num -> Observable.interval(200L, TimeUnit.MILLISECONDS)
                    .take(10)
                    .skip(1)
                    .map(row -> num + " * " + row + " = " + num * row)
            )
            .subscribe(
                data -> Logger.log(LogType.ON_NEXT, data),
                error -> {
                },
                () -> {
                    TimeUtil.end();
                    TimeUtil.takeTime();
                }
            );
        TimeUtil.sleep(5000L);
    }

    @Test
    void void_observablee_switch_map() {
        TimeUtil.start();

        Observable.interval(100L, TimeUnit.MILLISECONDS)
            .take(4)
            .skip(2)
            .doOnNext(data -> Logger.log(LogType.DO_ON_NEXT, data))
            .switchMap(
                num -> Observable.interval(300L, TimeUnit.MILLISECONDS)
                    .take(10)
                    .skip(1)
                    .map(row -> num + " * " + row + " = " + num * row)
            )
            .subscribe(data -> Logger.log(LogType.ON_NEXT, data));

        TimeUtil.sleep(5000L);
    }

    @Test
    void observablee_group_by() {
        Observable<GroupedObservable<CarMaker, Car>> observablee = Observable.fromIterable(SampleData.carList)
            .groupBy(car -> car.getCarMaker());

        observablee.subscribe(
            groupedObservable -> groupedObservable.subscribe(
                car -> Logger.log(
                    LogType.ON_NEXT,
                    "Group: " + groupedObservable.getKey() + "\t Car name: " + car.getCarName()
                )
            )
        );
    }

    @Test
    void observablee_to_list() {
        final Single<List<Integer>> single = Observable.just(1, 3, 5, 7, 9).toList();
        single.subscribe(System.out::println);
    }

    @Test
    void observablee_to_map() {
        final Single<Map<String, String>> single = Observable.just("a-1", "b-1", "c-1", "d-1")
            .toMap(data -> data.split("-")[0]);

        single.subscribe(System.out::println);
    }

    @Test
    void observablee_merge() {
        final Observable<Long> observablee1 = Observable.interval(200L, TimeUnit.MILLISECONDS)
            .take(5);

        final Observable<Long> observablee2 = Observable.interval(400L, TimeUnit.MILLISECONDS)
            .take(5)
            .map(num -> num + 1000);

        Observable.merge(observablee1, observablee2)
            .subscribe(data -> Logger.log(LogType.ON_NEXT, data));

        TimeUtil.sleep(4000);
    }

    @Test
    void observablee_concat() {
        final Observable<Long> observablee1 = Observable.interval(500L, TimeUnit.MILLISECONDS)
            .take(5);

        final Observable<Long> observablee2 = Observable.interval(300L, TimeUnit.MILLISECONDS)
            .take(5)
            .map(num -> num + 1000);

        Observable.concat(observablee1, observablee2)
            .subscribe(data -> Logger.log(LogType.ON_NEXT, data));

        TimeUtil.sleep(4000);
    }

    @Test
    void observable_zip() {
        final Observable<Long> observablee1 = Observable.interval(200L, TimeUnit.MILLISECONDS)
            .take(4);

        final Observable<Long> observablee2 = Observable.interval(400L, TimeUnit.MILLISECONDS)
            .take(6);

        Observable.zip(observablee1, observablee2, (data1, data2) -> data1 + data2)
            .subscribe(data -> Logger.log(LogType.ON_NEXT, data));

        TimeUtil.sleep(4000);
    }

    @Test
    void observablee_combineLatest() {
        final Observable<Long> observablee1 = Observable.interval(500L, TimeUnit.MILLISECONDS)
            .take(4);

        final Observable<Long> observablee2 = Observable.interval(700L, TimeUnit.MILLISECONDS)
            .take(4);

        Observable.combineLatest(observablee1, observablee2, (data1, data2) -> "data1: " + data1 + "\tdata2: " + data2)
            .subscribe(data -> Logger.log(LogType.ON_NEXT, data));

        TimeUtil.sleep(4000);
    }

    @Test
    void try_catch_사용하지_못한다() {
        try {
            Observable.just(2)
                .map(num -> num / 0)
                .subscribe(System.out::println);
        } catch (Exception e) {
            System.out.println("error logging...");
        }
    }

    @Test
    void error_handle() {
        Observable.just(5)
            .flatMap(num -> Observable.interval(200L, TimeUnit.MILLISECONDS)
                .doOnNext(data -> Logger.log(LogType.DO_ON_NEXT, data))
                .take(5)
                .map(i -> num / i))
            .subscribe(
                data -> Logger.log(LogType.ON_NEXT, data),
                error -> Logger.log(LogType.ON_ERROR, error),
                () -> Logger.log(LogType.ON_COMPLETE)
            );

        TimeUtil.sleep(1000L);
    }

    @Test
    void observable_onErrorReturn() {
        Observable.just(5)
            .flatMap(num -> Observable.interval(200L, TimeUnit.MILLISECONDS)
                .doOnNext(data -> Logger.log(LogType.DO_ON_NEXT, data))
                .take(5)
                .map(i -> num / i)
                .onErrorReturn(ex -> {
                    if (ex instanceof ArithmeticException) {
                        Logger.log(LogType.PRINT, "게산 처리 에러 발생" + ex.getMessage());
                    }
                    return -1L;
                })
            )
            .subscribe(
                data -> {
                    if (data < 0) {
                        Logger.log(LogType.PRINT, "예외를 알리는 데이터: " + data);
                    } else {
                        Logger.log(LogType.ON_NEXT, data);
                    }
                },
                error -> Logger.log(LogType.ON_ERROR, error),
                () -> Logger.log(LogType.ON_COMPLETE)
            );

        TimeUtil.sleep(1000L);
    }

    @Test
    void observable_onErrorResumeNext() {
        Observable.just(5)
            .flatMap(num -> Observable.interval(200L, TimeUnit.MILLISECONDS)
                .doOnNext(data -> Logger.log(LogType.DO_ON_NEXT, data))
                .take(5)
                .map(i -> num / i)
                .onErrorResumeNext(throwable -> {
                    Logger.log(LogType.PRINT, "운영제에게 이메일 발송 " + throwable.getMessage());
                    return Observable.interval(200L, TimeUnit.MILLISECONDS).take(5).skip(1).map(i -> num / i);
                })
            ).subscribe(data -> Logger.log(LogType.ON_NEXT, data));

        TimeUtil.sleep(2000L);
    }

    @Test
    void observable_retry() {
        Observable.just(5)
            .flatMap(num -> Observable.interval(200L, TimeUnit.MILLISECONDS)
                .map(i -> {
                    long result;
                    try {
                        result = num / i;
                    } catch (ArithmeticException e) {
                        Logger.log(LogType.PRINT, "error: " + e.getMessage());
                        throw e;
                    }
                    return result;
                })
                .retry(5)
                .onErrorReturn(throwable -> -1L)
            )
            .subscribe(
                data -> Logger.log(LogType.ON_NEXT, data),
                error -> Logger.log(LogType.ON_ERROR, error),
                () -> Logger.log(LogType.ON_COMPLETE)
            );

        TimeUtil.sleep(5000L);
    }

    @Test
    void observable_delay() {
        Logger.log(LogType.PRINT, "실행 시간: " + TimeUtil.getCurrentTimeFormatted());

        Observable.just(1, 3, 4, 6)
            .doOnNext(data -> Logger.log(LogType.DO_ON_NEXT, data))
            .delay(200, TimeUnit.MILLISECONDS)
            .subscribe(data -> Logger.log(LogType.ON_NEXT, data));

        TimeUtil.sleep(2500L);
    }

    @Test
    void observable_delaySubscription() {
        Logger.log(LogType.PRINT, "실행 시간: " + TimeUtil.getCurrentTimeFormatted());

        Observable.just(1, 3, 4, 6)
            .doOnNext(data -> Logger.log(LogType.DO_ON_NEXT, data))
            .delaySubscription(200, TimeUnit.MILLISECONDS)
            .subscribe(data -> Logger.log(LogType.ON_NEXT, data));

        TimeUtil.sleep(2500L);
    }

    @Test
    void observable_timeout() {
        Observable.range(1, 5)
            .map(num -> {
                long time = 1000L;
                if (num == 4) {
                    time = 1500L;
                }
                TimeUtil.sleep(time);
                return num;
            })
            .timeout(1200L, TimeUnit.MICROSECONDS)
            .subscribe(
                data -> Logger.log(LogType.ON_NEXT, data),
                error -> Logger.log(LogType.ON_ERROR, error)
            );

        TimeUtil.sleep(4000L);
    }

    @Test
    void observable_timeInterval() {
        Observable.just(1, 3, 5, 6, 9)
            .delay(item -> {
                TimeUtil.sleep(NumberUtil.randomRange(100, 1000));
                return Observable.just(item);
            })
            .timeInterval()
            .subscribe(
                timed -> Logger
                    .log(LogType.ON_NEXT, "통지하는데 걸리는 시간: " + timed.toString() + "\t 통지된 데이터: " + timed.value())
            );
    }

}
