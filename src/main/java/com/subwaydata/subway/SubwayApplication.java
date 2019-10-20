package com.subwaydata.subway;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author: Lijiwen
 * Description:
 * @createDate
 **/
@SpringBootApplication(scanBasePackages = "com.subwaydata.subway")
@ServletComponentScan
@EnableScheduling
public class SubwayApplication extends SpringBootServletInitializer {

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
        /**
         * 手动创建线程池
         * 创建线程工厂
         */
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("xxx-pool-%d")
                .build();
        // 创建通用线程池
        /**
         * 参数含义：
         *      corePoolSize : 线程池中常驻的线程数量。核心线程数，默认情况下核心线程会一直存活，即使处于闲置状态也不会受存keepAliveTime限制。除非将allowCoreThreadTimeOut设置为true。
         *      maximumPoolSize : 线程池所能容纳的最大线程数。超过这个数的线程将被阻塞。当任务队列为没有设置大小的LinkedBlockingDeque时，这个值无效。
         *      keepAliveTime : 当线程数量多于 corePoolSize 时，空闲线程的存活时长，超过这个时间就会被回收
         *      unit : keepAliveTime 的时间单位
         *      workQueue : 存放待处理任务的队列，该队列只接收 Runnable 接口
         *      threadFactory : 线程创建工厂
         *      handler : 当线程池中的资源已经全部耗尽，添加新线程被拒绝时，会调用RejectedExecutionHandler的rejectedExecution方法，参考 ThreadPoolExecutor 类中的内部策略类
         */
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(5, 200, 0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024),
                threadFactory,
                new ThreadPoolExecutor.AbortPolicy());
        acceptor.getFilterChain().addLast("exector", new ExecutorFilter(threadPool));
        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
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
