package com.hiker.app.utils;

import android.os.SystemClock;

public class Utils {
    public static String dateToString(long t) {
        int h = (int)(t /3600000);
        int m = (int)(t - h*3600000)/60000;
        int s = (int)(t - h*3600000- m*60000)/1000 ;
        String hh = Integer.toString(h);
        String mm = m < 10 ? "0" + m: "" + m;
        String ss = s < 10 ? "0" + s: "" + s;
        return (hh + ":" + mm + ":" + ss);
    }
}
