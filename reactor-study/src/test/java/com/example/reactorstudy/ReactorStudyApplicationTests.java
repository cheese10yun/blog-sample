package com.example.reactorstudy;

import com.example.reactorstudy.util.LogType;
import com.example.reactorstudy.util.Logger;
import com.example.reactorstudy.util.TimeUtil;
import io.reactivex.BackpressureOverflowStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
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

    @Test
    void test01() throws InterruptedException {
        final Disposable disposable = Flux.interval(Duration.ofMillis(50))
            .subscribe(
                data -> System.out.println(MessageFormat.format("onNext:'{'{0}'}'", data))
            );

        Thread.sleep(200);
        disposable.dispose();
    }

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
}
