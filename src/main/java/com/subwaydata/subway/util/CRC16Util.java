package com.subwaydata.subway.util;

public class CRC16Util {

    public static int get_crc16 (byte[] bufData, int buflen, byte[] pcrc)
    {
        int ret = 0;
        int CRC = 0x0000ffff;
        int POLYNOMIAL = 0x0000a001;
        int i, j;


        if (buflen == 0)
        {
            return ret;
        }
        for (i = 0; i < buflen; i++)
        {
            CRC ^= ((int)bufData[i] & 0x000000ff);
            for (j = 0; j < 8; j++)
            {
                if ((CRC & 0x00000001) != 0)
                {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                }
                else
                {
                    CRC >>= 1;
                }
            }
        }

        System.out.println(Integer.toHexString(CRC));
        pcrc[0] = (byte)(CRC & 0x00ff);
        pcrc[1] = (byte)(CRC >> 8);

        return ret;
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        byte[] aa = {0x30,0x30,0x34,0x36,0x46,0x44,0x36,0x30,0x30,0x30,0x01,0x72,0x65,
                0x66,0x65,0x72,0x69,0x6E,0x66,0x6F,0x2E,0x63,0x73,0x76,0x00,0x00
                ,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00
                ,0x00,0x00,0x00,0x01,(byte)0xf4,0x01};
        byte[] bb = new byte[2];
        get_crc16(aa, aa.length-4, bb);

        System.out.println(Integer.toHexString((int)bb[0] & 0x000000ff));
        System.out.println(Integer.toHexString((int)bb[1] & 0x000000ff));


    }

}
