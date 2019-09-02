package com.subwaydata.subway.thread;



import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolManager {

    private static final int THREAD_SIZE_HIGH = 200;
    private static final int THREAD_SIZE_NORMAL = 50;
    private static final int THREAD_SIZE_LOW = 10;

    private static BlockingQueue<Runnable> queueHigh = new ArrayBlockingQueue<Runnable>(200);
    private static BlockingQueue<Runnable> queueNormal = new ArrayBlockingQueue<Runnable>(1000);
    private static BlockingQueue<Runnable> queueLow = new ArrayBlockingQueue<Runnable>(200);

    private static final ExecutorService executorHighPriority = new ThreadPoolExecutor(THREAD_SIZE_HIGH, THREAD_SIZE_HIGH, 60L,
            TimeUnit.SECONDS, queueHigh,
            new NamedThreadFactoryHigh());
    private static final ExecutorService executorNormalPriority = new ThreadPoolExecutor(THREAD_SIZE_NORMAL, THREAD_SIZE_NORMAL * 8, 60L,
            TimeUnit.SECONDS, queueNormal,
            new NamedThreadFactory());
    private static final ExecutorService executorLowPriority = new ThreadPoolExecutor(THREAD_SIZE_LOW, THREAD_SIZE_LOW * 2, 120L,
            TimeUnit.SECONDS, queueLow,
            new NamedThreadFactoryLow());


    private static final ScheduledExecutorService scheduledThreadPoolExecutor = Executors.newSingleThreadScheduledExecutor();


    public static void execute(Runnable command) {
        execute(command, Thread.NORM_PRIORITY);
    }

    public static void execute(Runnable command, int priority) {
        try {
            getExecutor(priority).execute(command);
        } catch (RejectedExecutionException e) {
            //Log.error("ThreadPool.schedule" + command.toString(), e);
        } catch (Throwable th) {
           // Log.error("Child Thread Error : " + th.getMessage(), th);
        }
    }

    /**
     * 提交任务 延时执行
     *
     * @param command
     * @param time
     * @param timeUnit
     */
    public static void schedule(Runnable command, long time, TimeUnit timeUnit) {
        try {
            getScheduledThreadPoolExecutor().schedule(command, time, timeUnit);
        } catch (RejectedExecutionException e) {
           // Log.error("ThreadPool.schedule" + command.toString(), e);
        } catch (Throwable th) {
            //Log.error("Child Thread Error : " + th.getMessage(), th);
        }

    }


    public static Future submit(Callable callable) {
        return submit(callable, Thread.NORM_PRIORITY);
    }

    public static Future submit(Callable callable, int priority) {
        Future future = null;
        try {
            future = getExecutor(priority).submit(callable);
        } catch (RejectedExecutionException e) {
           // Log.error("ThreadPool.submit" + callable.toString(), e);
        } catch (Throwable th) {
            //Log.error("Child Thread Error : " + th.getMessage(), th);
        }
        return future;
    }


    private static ExecutorService getExecutor(int priority) {
        if (priority == Thread.MIN_PRIORITY) {
            return executorLowPriority;
        } else if (priority == Thread.MAX_PRIORITY) {
            return executorHighPriority;
        } else {
            return executorNormalPriority;
        }
    }


    public static ScheduledExecutorService getScheduledThreadPoolExecutor() {
        return scheduledThreadPoolExecutor;
    }

}

class NamedThreadFactoryHigh implements ThreadFactory {
    private AtomicInteger tag = new AtomicInteger(0);

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName("ledo-ThreadPool-thread-High：" + tag.getAndIncrement());
        thread.setPriority(Thread.MAX_PRIORITY);
        return thread;
    }
}

class NamedThreadFactory implements ThreadFactory {
    private AtomicInteger tag = new AtomicInteger(0);

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName("ledo-ThreadPool-thread：" + tag.getAndIncrement());
        return thread;
    }
}

class NamedThreadFactoryLow implements ThreadFactory {
    private AtomicInteger tag = new AtomicInteger(0);

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName("ledo-ThreadPool-thread-low：" + tag.getAndIncrement());
        thread.setPriority(Thread.MIN_PRIORITY);
        return thread;
    }
}