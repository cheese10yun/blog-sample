package com.example.kotlinjava8;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
