package com.subwaydata.subway.service;

import com.subwaydata.subway.kafka.KafkaSender;
import com.subwaydata.subway.thread.ThreadPoolManager;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.*;
import java.net.*;
import java.util.logging.Logger;

/**
 * 服务器端，实现基于UDP的用户登陆
 */
@WebListener
public class UDPServer implements ServletContextListener {
    public static Logger logger = Logger.getLogger(UDPServer.class.getName());
    public static final int MAX_UDP_DATA_SIZE = 42;
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
            logger.info("=======创建数据报，用于接收客户端发送的数据======");
            while (true) {
                byte[] buffer = new byte[MAX_UDP_DATA_SIZE];
                packet = new DatagramPacket(buffer, buffer.length);
                try {
                    logger.info("=======此方法在接收到数据报之前会一直阻塞======");
                    socket.receive(packet);
                    // new Thread(new Process(packet)).start();
                    logger.info("=======接收到的UDP信息======");
                    // 接收到的UDP信息，然后解码
                    byte[] datas = packet.getData();
                    String data = bytes2HexString(datas);
                    kafkaSender.send(data);
                    logger.info("=======Process srt2 UTF-8======" + data);
                    //此后判断数据是否正常
                    if (1 == 1) {
                        logger.info("====向客户端响应数据=====");
                        //1.定义客户端的地址、端口号、数据
                        InetAddress address = packet.getAddress();
                        int port = packet.getPort();
                        byte[] data2 = "EA 6A 11 00 13 60 01 55 26 5F FF 72 0E 01 00 01 xx xx 0D 0A".getBytes();
                        //2.创建数据报，包含响应的数据信息
                        DatagramPacket packet2 = new DatagramPacket(data2, data2.length, address, port);
                        //3.响应客户端
                        socket.send(packet2);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    class Process implements Runnable {
        public Process(DatagramPacket packet) throws UnsupportedEncodingException {
            logger.info("=======接收到的UDP信息======");
            // 接收到的UDP信息，然后解码
            byte[] buffer = packet.getData();
            String srt2 = bytes2HexString(buffer);
            kafkaSender.send(srt2);
            logger.info("=======Process srt2 UTF-8======" + srt2);
        }

        @Override
        public void run() {
            logger.info("====过程运行=====");
            try {
                logger.info("====向客户端响应数据=====");
                //1.定义客户端的地址、端口号、数据
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                byte[] data2 = "{'request':'alive','errcode':'0'}".getBytes();
                //2.创建数据报，包含响应的数据信息
                DatagramPacket packet2 = new DatagramPacket(data2, data2.length, address, port);
                //3.响应客户端
                socket.send(packet2);
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

}
