package com.example.querydsl;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
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

    @Test
    void predicate() {
        Predicate<String> startWithXX = (s) -> s.startsWith("asd");

        final boolean s1 = startWithXX.test("sss");
        final boolean s2 = startWithXX.test("ssss");
    }

    @Test
    void fuction() {

        Function<Integer, String> fuc = String::valueOf;
        final String apply = fuc.apply(1);

    }

    @Test
    void asdasd() {

        Supplier<Greeting> greeting = Greeting::new;

        final Greeting greeting1 = greeting.get();

        UnaryOperator<String> hello = greeting1::hello;

        final String asdasd = hello.apply("asdasd");

        System.out.println(asdasd);

    }
}

class JPlus10 implements Function<Integer, Integer> {

    @Override
    public Integer apply(Integer integer) {
        return integer + 10;
    }
}


class Greeting {

    private String name;

    public Greeting() {
    }

    public Greeting(String name) {
        this.name = name;
    }

    public String hello(String name) {
        return "hello" + name;
    }

    public static String hi(String name) {
        return "hi" + name;
    }


}