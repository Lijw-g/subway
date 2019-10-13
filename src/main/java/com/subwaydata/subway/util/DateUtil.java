package com.subwaydata.subway.util;

/**
 * @program: subway
 * @description: 时间工具
 * @author: lijiwen
 * @create: 2019-10-06 16:58
 **/
public class DateUtil {
    public static String getOther(int date) {
        StringBuilder data = new StringBuilder();
        String string = Long.toHexString(Long.valueOf(date));
        if (string.length() < 2) {
            data.append(0 + string);
        } else {
            data.append(string);
        }
        return data.toString().toUpperCase();

    }

    public static String getYearToHex(String year) {
        StringBuilder years = new StringBuilder();
        String string = Long.toHexString(Long.valueOf(year));
        if (string.length() < 4) {
            string = 0 + string;
        }
        years.append(string.substring(2, 4)).append(string.substring(0, 2));
        return years.toString().toUpperCase();

    }
}
