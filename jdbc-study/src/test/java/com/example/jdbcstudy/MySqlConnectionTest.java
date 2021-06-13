package com.example.jdbcstudy;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class MySqlConnectionTest {

    @Test
    public void name() {

        final HashMap<String, String> map = new HashMap<>();

        map.put("useCursorFetch", "true");
        map.put("defaultFetchSize", "10000");


        final String asd = map.entrySet().stream()
            .map(Objects::toString)
            .collect(Collectors.joining("&"));

        System.out.println(asd);
    }
}

//useCursorFetch=true&defaultFetchSize=10000
//useCursorFetch=true&defaultFetchSize=10000
