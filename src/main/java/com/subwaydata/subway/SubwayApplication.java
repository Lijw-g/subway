package com.subwaydata.subway;

import com.subwaydata.subway.handler.YourHandler;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author: Lijiwen
 * Description:
 * @createDate
 **/
@SpringBootApplication(scanBasePackages="com.subwaydata.subway")
@ServletComponentScan
@EnableScheduling
public class SubwayApplication extends SpringBootServletInitializer {
    private Logger logger = LoggerFactory.getLogger(SubwayApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SubwayApplication.class, args);
    }

    @Bean
    CommandLineRunner serverRunner(YourHandler handler) {
        return strings -> {
            createUdpServer(handler);
        };
    }

    private void createUdpServer(YourHandler handler) throws IOException {
        //创建一个UDP的接收器
        NioDatagramAcceptor acceptor = new NioDatagramAcceptor();
        //设置接收器的处理程序
        acceptor.setHandler(handler);
        //建立线程池
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                10,8,10, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(1),
                new ThreadPoolExecutor.DiscardOldestPolicy());
        acceptor.getFilterChain().addLast("exector", new ExecutorFilter(threadPool));
        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
       // acceptor.getFilterChain().addLast("toMessageTyep" ,  new  MyMessageEn_Decoder());;
        //建立连接的配置文件
        DatagramSessionConfig dcfg = acceptor.getSessionConfig();
        //设置接收最大字节默认2048
        dcfg.setReadBufferSize(4096);
        //设置输入缓冲区的大小
        dcfg.setReceiveBufferSize(1024);
        //设置输出缓冲区的大小
        dcfg.setSendBufferSize(1024);
        //设置每一个非主监听连接的端口可以重用
        dcfg.setReuseAddress(true);
        //绑定端口
        acceptor.bind(new InetSocketAddress(8002));

    }


}
