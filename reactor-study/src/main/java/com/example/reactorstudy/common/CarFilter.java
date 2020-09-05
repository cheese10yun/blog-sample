package com.example.reactorstudy.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class CarFilter {
    // 사용자 정의 Predicate 사용
    public static List<Car> filterCarByCustomPredicate(List<Car> cars, CarPredicate p) {
        List<Car> resultCars = new ArrayList<>();
        for(Car car : cars)
            if (p.test(car)) {
                resultCars.add(car);
            }

        return resultCars;
    }

    // java.util.function 패키지에 내장된 Predicate 사용
    public static List<Car> filterCarByBuiltinPredicate(List<Car> cars, Predicate<Car> p) {
        List<Car> resultCars = new ArrayList<>();
        for(Car car : cars)
            if (p.test(car)) {
                resultCars.add(car);
            }

        return resultCars;
    }
}
