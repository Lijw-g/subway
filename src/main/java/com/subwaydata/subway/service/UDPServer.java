package com.subwaydata.subway.service;

import com.subwaydata.subway.kafka.KafkaSender;
import com.subwaydata.subway.thread.ThreadPoolManager;
import com.subwaydata.subway.util.CRC16Util;
import com.subwaydata.subway.util.DataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import sun.applet.resources.MsgAppletViewer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.*;
import java.net.*;
import java.util.Calendar;
import java.util.logging.Logger;

/**
 * 服务器端，实现基于UDP的用户登陆
 */
@WebListener
public class UDPServer implements ServletContextListener {
    public static Logger logger = Logger.getLogger(UDPServer.class.getName());
    public static final int MAX_UDP_DATA_SIZE = 1024;
    public static final int UDP_PORT = 8081;
    public static DatagramPacket packet = null;
    public static DatagramSocket socket = null;
    @Autowired
    private KafkaSender<String> kafkaSender;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            logger.info("========启动一个线程，监听UDP数据报.PORT:" + UDP_PORT + "=========");
            // 启动一个线程，监听UDP数据报
            //new Thread(new UDPProcess(UDP_PORT)).start();
            ThreadPoolManager.execute(new UDPProcess(UDP_PORT));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    class UDPProcess implements Runnable {

        public UDPProcess(final int port) throws SocketException {
            //创建服务器端DatagramSocket，指定端口
            socket = new DatagramSocket(port);
        }

        @Override
        public void run() {
            try {
                while (true) {

                byte[] buffer = new byte[MAX_UDP_DATA_SIZE];
                packet = new DatagramPacket(buffer, buffer.length);

                    //logger.info("=======此方法在接收到数据报之前会一直阻塞======");
                    socket.receive(packet);
                    // 接收到的UDP信息，然后解码
                    byte[] datas = packet.getData();
                    String data = bytes2HexString(datas);
                    String order = data.substring(22, 26);
                    InetAddress address = packet.getAddress();
                    int port = packet.getPort();
                    if ("720E".equals(order)) {
                        kafkaSender.send(data);
                        String responsData="EA6A110013600155265FFF720E01000100000D0A";
                        byte[] data2 = DataUtil.creatDate(responsData);
                        DatagramPacket packet2 = new DatagramPacket(data2, data2.length, address, port);

                        //3.响应客户端
                        socket.send(packet2);
                        logger.info("向传感器响应的数据是" + packet2.toString());

                    }

                    if ("740C".equals(order)) {
                        /***
                         年( 2 字 节 )
                         前低后高，比如 2019 年表示为 E3 07
                         月( 1 字 节 )
                         比如 12 月表示为 0C
                         日( 1 字 节 )
                         比如 30 日表示为 1E
                         时( 1 字 节 )
                         比如 23 时表示为 17
                         分( 1 字 节 )
                         比如 59 分表示为 3B
                         秒( 1 字 节 )
                         比如 59 秒表示为 3B*/
                        StringBuilder answerSb = new StringBuilder();
                        answerSb.append("EA6A18002306010226FFFF74");
                        Calendar now = Calendar.getInstance();
                        answerSb.append(getYearToHex(String.valueOf(now.get(Calendar.YEAR))));
                        answerSb.append(getOther(now.get(Calendar.MONTH)+1));
                        answerSb.append(getOther(now.get(Calendar.DAY_OF_MONTH)));
                        answerSb.append(getOther(now.get(Calendar.HOUR_OF_DAY)));
                        answerSb.append(getOther(now.get(Calendar.MINUTE)));
                        answerSb.append(getOther(now.get(Calendar.SECOND)));
                        answerSb.append("0000");
                        answerSb.append("0A0D");
                        byte[] answer = DataUtil.creatDate(answerSb.toString());

                        DatagramPacket packet2 = new DatagramPacket(answer, answer.length, address, port);
                        //3.响应客户端
                        String diviceStatus = data.substring(26, 28);
                        if ("00".equals(diviceStatus)) {
                            logger.info("工作状态正常");
                        } else if ("E1".equals(diviceStatus)) {
                            logger.info("采集终端异常");
                        } else if ("E2".equals(diviceStatus)) {
                            logger.info("电压传感器异常");
                        } else if ("E3".equals(diviceStatus)) {
                            logger.info("电流传感器异常");
                        } else {
                            logger.info("未知异常");

                        }
                        socket.send(packet2);
                        logger.info("心跳向传感器响应的数据是" + answerSb.toString());
                    }
                    //当前采集终端编码 0x7211
                    if("7211".equals(order)){
                        String msg="EA6A11002306010226FFFF711100000D0A";
                        byte[] msgs = DataUtil.creatDate(msg);
                        DatagramPacket packet = new DatagramPacket(msgs, msgs.length, address, port);
                        socket.send(packet);
                        logger.info("发送给终端读取终端编码命令为"+msg);
                        logger.info("读取到的信息为"+data);
                        logger.info("接受到的编码为"+data.substring(26,54));
                        //0x23 0x06 0x01 0x02 0x26 0xFF 0xFF (不足 14 位、用“F”补齐，预留)，
                        // 表示为广东省 广州市1号线第2号列车第2节车厢6号门
                        String codeInfo=data.substring(26,54);
                        String pro=codeInfo.substring(0,2);
                        logger.info("省份代码"+pro);
                        String cityId=codeInfo.substring(2,4);
                        logger.info("城市id"+cityId);
                        String line=codeInfo.substring(4,6);
                        logger.info("地铁线"+line);
                        String train=codeInfo.substring(6,8);
                        logger.info("列车号"+train);
                        int part=Integer.valueOf(codeInfo.substring(8,10));
                        logger.info("第"+(part/10)+"节车厢");
                        logger.info((part%10)+"号门");



                    }
                    //设置采集终端时间间隔 0x7212
                    if("7212".equals(order)){
                        String msg="EA6A11002306010226FFFF711200000D0A";
                        byte[] msgs = DataUtil.creatDate(msg);
                        DatagramPacket packet = new DatagramPacket(msgs, msgs.length, address, port);
                        socket.send(packet);
                        logger.info("发送给终端设置终端发送间隔时间的命令为："+msg);
                        logger.info("读取到的信息为"+data);
                        logger.info("接受到的时间间隔"+Long.parseLong(data.substring(26,28),16)+"s");

                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("========UDPListener摧毁=========");
    }

    /**
     * byte[] 转为16进制String
     */
    public static String bytes2HexString(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        // toUpperCase将字符串中的所有字符转换为大写
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        // toCharArray将此字符串转换为一个新的字符数组。
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    //返回匹配字符
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String getYearToHex(String year) {
        logger.info("响应时间"+year);
        StringBuilder years = new StringBuilder();
        String string = Long.toHexString(Long.valueOf(year));
        if (string.length() < 4) {
            string = 0 + string;
        }
        years.append(string.substring(2, 4)).append(string.substring(0, 2));
        return years.toString().toUpperCase();

    }

    public static String getOther(int date) {
        logger.info("响应时间"+date);
        StringBuilder data = new StringBuilder();
        String string = Long.toHexString(Long.valueOf(date));
        if (string.length() < 2) {
            data.append(0 + string);
        } else {
            data.append(string);
        }
        return data.toString().toUpperCase();

    }
}
