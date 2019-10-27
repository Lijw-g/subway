package com.subwaydata.subway.util;


public class CRC16Util {

    public static int get_crc16(int[] bufData, int buflen) {
        int ret = 0;
        int CRC = 0x0000ffff;
        int POLYNOMIAL = 0x0000a001;
        int i, j;


        if (buflen == 0) {
            return ret;
        }
        for (i = 0; i < buflen; i++) {
            CRC ^= ((int) bufData[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }

        System.out.println(Integer.toHexString(CRC));
        return CRC;
    }

     public  static  String creatCrc16_s(String data){
         int [] datas=DataUtil.creatDateInt(data);
         int crcResult = get_crc16(datas, datas.length);
         StringBuilder sb = new StringBuilder();
         sb.append(Integer.toHexString( crcResult & 0x000000ff));
         sb.append(Integer.toHexString(   crcResult>>8));
         return sb.toString();
     }
    /**
     * @param args
     */
    public static void main(String[] args) {
        //EB 6A 2A 00 23 06 01 02 26 FF FF 72 0E 01 39 10 EC 7D 01 6E 35 01 01 21 01 17 02 00 0A 12 00 01 09 01 21 54 00 15 00 00 0D 0A
        String str="EB 6A 2A 00 23 06 01 02 26 FF FF 72 0E 01 39 10 EC 7D 01 6E 35 01 01 21 01 17 02 00 0A 12 00 01 09 01 21 54 00 15";
        int[] mm=DataUtil.creatDateInt(str.replaceAll(" ",""));
        int crc16 = get_crc16(mm, mm.length);
        System.out.println(Integer.toHexString( crc16 & 0x000000ff));
        System.out.println(Integer.toHexString(   crc16>>8));
    }
}
