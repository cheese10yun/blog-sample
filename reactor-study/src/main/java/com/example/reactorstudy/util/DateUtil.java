package com.example.reactorstudy.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtil {
    public static String getNowDate(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(Calendar.getInstance().getTime());
    }
}
