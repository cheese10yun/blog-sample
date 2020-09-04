package com.example.reactorstudy.util;

public class NumberUtil {
    public static int randomRange(int min, int max){
        return (int)(Math.random() * (max - min + 1)) + min;
    }
}
