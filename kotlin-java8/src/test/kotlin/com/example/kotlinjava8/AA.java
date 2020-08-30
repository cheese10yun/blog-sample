package com.example.kotlinjava8;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class AA {

    @Test
    void name() {

        final List<Apple> apples = new ArrayList<>();

        apples.add(new Apple(1, Color.GREEN));

        Apple.Companion.filterApples(apples, (apple -> Color.READ.equals(apple.getColor())));

    }


    @Test
    void asdasdsad() {

        forEach(
            Arrays.asList(1, 2, 3, 4, 5),
            (Integer i) -> System.out.println(i)
        );
    }

    @Test
    void asddd() {
        final List<String> map = map(
            Arrays.asList(1, 2, 3, 4),
            (Integer i) -> i.toString()
        );
    }

    @Test
    void asdddasdsad() {
        final Function<String, Integer> stringIntegerFunction = (String s) -> Integer.parseInt(s);
        final Function<String, Integer> str1 = (Integer::parseInt);
        final Function<String, Integer> str2 = Integer::new;
        final Function<Integer, Integer> str3 = Integer::new;
    }

    @Test
    void asdasdasd() {
        final List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5);
    }

    @Test
    void aaaaaaa() {

        final List<String> listOf = Arrays.asList("1", "2");

        listOf.stream()
            .map(a-> a.split(""))
            .flatMap(Arrays::stream)
            .distinct()
            .collect(Collectors.toList());

    }

    @Test
    void wdkdjd() {
        final List<Integer> listOf = Arrays.asList(1,2);

        listOf.stream()
            .reduce(Integer::max);
    }

    public <T> List<T> filter(List<T> list, Predicate<T> p) {
        return list.stream()
            .filter(p)
            .collect(Collectors.toList());
    }

    public <T> void forEach(List<T> list, Consumer<T> c) {
        for (T t : list) {
            c.accept(t);
        }
    }

    public <T, R> List<R> map(List<T> list, Function<T, R> f) {
        final List<R> result = new ArrayList<>();
        for (T t : list) {
            result.add(f.apply(t));
        }
        return result;
    }


}

class AAA {

    public void asd() throws IOException {

        processFile((BufferedReader br) -> br.readLine());

    }


    public String processFile(BufferedReaderProcessor readerProcessor) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
            return readerProcessor.process(br);
        }
    }
}

@FunctionalInterface
interface BufferedReaderProcessor {

    String process(BufferedReader reader) throws IOException;
}

@FunctionalInterface
interface Consumer<T> {

    void accept(T t);
}
