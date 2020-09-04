package com.example.reactorstudy.util;

public class Logger {
    public static void log(LogType logType){
        String time = TimeUtil.getCurrentTimeFormatted();
        System.out.println(logType.getLogType() + " | " + Thread.currentThread().getName() + " | " + time);
}

    public static void log(LogType logType, Object obj){
        String time = TimeUtil.getCurrentTimeFormatted();
        System.out.println(logType.getLogType() + " | " + Thread.currentThread().getName() + " | " + time + " | "  +obj);
    }
}
