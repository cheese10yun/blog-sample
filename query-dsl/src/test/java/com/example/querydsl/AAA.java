package com.example.querydsl;

import java.util.function.Function;
import org.junit.jupiter.api.Test;

public class AAA {

    @Test
    void name() {

        Function<Integer, Integer> plus10 = (i) -> i + 10;
        Function<Integer, Integer> plus20 = (i) -> i + 20;

        final Function<Integer, Integer> compose = plus10.compose(plus20);

        final Integer apply = compose.apply(10);

        System.out.println(apply);


    }
}

class JPlus10 implements Function<Integer, Integer> {

    @Override
    public Integer apply(Integer integer) {
        return integer + 10;
    }
}