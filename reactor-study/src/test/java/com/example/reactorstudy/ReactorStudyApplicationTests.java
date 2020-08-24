package com.example.reactorstudy;

import java.text.MessageFormat;
import java.time.Duration;
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
}
