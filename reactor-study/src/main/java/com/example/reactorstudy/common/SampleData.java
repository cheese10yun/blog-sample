package com.example.reactorstudy.common;

import io.reactivex.Observable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SampleData {
    public static CarMaker[] carMakersDuplicated = {
            CarMaker.CHEVROLET,
            CarMaker.HYUNDAE,
            CarMaker.SAMSUNG,
            CarMaker.SSANGYOUNG,
            CarMaker.CHEVROLET,
            CarMaker.HYUNDAE,
            CarMaker.KIA,
            CarMaker.SSANGYOUNG
    };

    public static CarMaker[] carMakers = {
            CarMaker.CHEVROLET,
            CarMaker.HYUNDAE,
            CarMaker.SAMSUNG,
            CarMaker.SSANGYOUNG,
            CarMaker.KIA
    };

    public static List<Car> carList =
            Arrays.asList(
                    new Car(CarMaker.CHEVROLET, "말리부", CarType.SEDAN, 23_000_000),
                    new Car(CarMaker.HYUNDAE, "쏘렌토", CarType.SUV, 33_000_000),
                    new Car(CarMaker.CHEVROLET, "트래버스", CarType.SUV, 50_000_000),
                    new Car(CarMaker.HYUNDAE, "팰리세이드", CarType.SEDAN, 28_000_000),
                    new Car(CarMaker.CHEVROLET, "트랙스", CarType.SUV, 18_000_000),
                    new Car(CarMaker.SSANGYOUNG, "티볼리", CarType.SUV, 23_000_000),
                    new Car(CarMaker.SAMSUNG, "SM6", CarType.SUV, 40_000_000),
                    new Car(CarMaker.SSANGYOUNG, "G4렉스턴", CarType.SUV, 43_000_000),
                    new Car(CarMaker.SAMSUNG, "SM5", CarType.SEDAN, 35_000_000)
            );

    // A, B, C 구간의 차량 속도 데이터
    public static final Integer[] speedOfSectionA = {100, 110, 115, 130, 160};
    public static final Integer[] speedOfSectionB = {85, 90, 100, 110, 105, 113, 125};
    public static final Integer[] speedOfSectionC = {98, 88, 111, 123, 155, 124, 136, 143};

    // 지점 A의 월별 매출
    public static final List<Integer> salesOfBranchA = Arrays.asList(
                15_000_000, 25_000_000, 10_000_000, 35_000_000, 23_000_000, 40_000_000, 50_000_000, 45_000_000,
                35_000_000, 23_000_000, 15_000_000, 10_000_000
            );

    // 지점 B의 월별 매출
    public static final List<Integer> salesOfBranchB = Arrays.asList(
            11_000_000, 23_000_000, 15_000_000, 32_000_000, 13_000_000, 45_000_000, 55_000_000, 43_000_000,
            25_000_000, 28_000_000, 19_000_000, 13_000_000
    );

    // 지점 C의 월별 매출
    public static final List<Integer> salesOfBranchC = Arrays.asList(
            12_000_000, 21_000_000, 19_000_000, 33_000_000, 33_000_000, 41_000_000, 52_000_000, 48_000_000,
            32_000_000, 21_000_000, 18_000_000, 14_000_000
    );


    // 서울의 시간별 미세먼지 농도
    public static final List<Integer> seoulPM10List = Arrays.asList(
      45, 30, 68, 70, 100, 110, 120, 90, 80, 60, 50, 60, 70, 80, 100, 150, 140, 130, 170, 130, 90, 86, 67, 50
    );

    // 부산의 시간별 미세먼지 농도
    public static final List<Integer> busanPM10List = Arrays.asList(
            35, 30, 63, 50, 80, 90, 100, 80, 70, 50, 55, 60, 65, 75, 80, 90, 100, 90, 120, 110, 70, 66, 65, 55
    );

    // 인천의 시간별 미세먼지 농도
    public static final List<Integer> incheonPM10List = Arrays.asList(
            55, 40, 73, 70, 85, 99, 120, 85, 75, 73, 80, 70, 95, 95, 100, 120, 110, 120, 140, 120, 100, 125, 135, 125
    );

    // 1시간 동안 서울의 온도 변화 데이터
    public static Integer[] temperatureOfSeoul = {10, 13, 14, 12, 11, 9};

    // 1시간 동안 서울의 습도 변화 데이터
    public static Integer[] humidityOfSeoul = {45, 35, 33, 43, 32, 62};


    public static Observable<String> getSpeedPerSection(String section, long interval, final Integer[] speedData){
        return Observable.interval(interval, TimeUnit.MILLISECONDS)
                .take(speedData.length)
                .map(Long::intValue)
                .map(i -> section + " 지점의 차량 속도: " + speedData[i]);
    }


}
