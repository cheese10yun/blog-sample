package com.example.reactorstudy;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
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
        Observable.just(100, 200, 300, 500)
            .doOnNext(data -> System.out.println(getThreadNAme() + " : doOnNext() :" + data))
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .filter(number -> number > 300)
            .subscribe(num -> System.out.println(getThreadNAme() + " : result : " + num));

        Thread.sleep(500);
    }

    private static String getThreadNAme() {
        return Thread.currentThread().getName();
    }
}
