package com.subwaydata.subway.util;

/**
 * @program: subway
 * @description: 组织发送数据
 * @author: lijiwen
 * @create: 2019-09-23 15:58
 **/
public class DataUtil {

    public static byte[] creatDate(String feedback) {
        byte[] bytes = new byte[feedback.length() / 2];
        for (int i = 0; i < feedback.length() / 2; i++) {
            String subStr = feedback.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }
        return bytes;
    }

    /***
     * @author: Lijiwen
     * Description:组织
     * @param feedback
     * @return int[]
     * @createDate
     **/
    public static int[] creatDateInt(String feedback) {
        int[] ints = new int[feedback.length() / 2];
        for (int i = 0; i < feedback.length() / 2; i++) {
            String subStr = feedback.substring(i * 2, i * 2 + 2);
            ints[i] = Integer.parseInt(subStr, 16);
        }
        return ints;
    }

    public static Integer getDataSize(String sizeStrinng) {
        int hight = Integer.parseInt(sizeStrinng.substring(2, 4), 16);
        int low = Integer.parseInt(sizeStrinng.substring(0, 2), 16);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(hight);
        stringBuilder.append(low);
        return Integer.valueOf(stringBuilder.toString());
    }
}
