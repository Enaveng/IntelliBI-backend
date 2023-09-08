package com.dlwlrma.IntelliBi.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolExecutorConfig {
    /**
     * 参数列表:
     * int corePoolSize     核心线程数 指在正常情况下我们系统应该可以同时工作的线程数 就是在线程池中一直存在的线程数
     * int maximumPoolSize  最大线程数 在极限情况下我们的线程池可以存在的最大线程数 大于核心线程数的部分即为非核心线程数
     * long keepAliveTime  （空闲线程存活时间）非核心线程在没有任务的情况下，过多久要删除（理解为开除临时工），从而释放无用的线程资源。非核心线程的空闲线程存活时间，单位：毫秒。
     * TimeUnit unit        （空闲线程存活时间的单位）存活时间的单位，可选的单位包括：`TimeUnit.MILLISECONDS、TimeUnit.SECONDS、TimeUnit.MINUTES、TimeUnit.HOURS、TimeUnit.DAYS`（时分秒）等等。
     * BlockingQueue<Runnable> workQueue （工作队列）==用于存放给线程执行的任务，存在一个队列的长度（一定要设置，不要说队列长度无限，因为也会占用资源)==。
     * hreadFactory threadFactory,        （线程工厂）线程创建工厂，==控制每个线程的生成、线程的属性（比如线程名）==，用于创建新的线程，可以自定义ThreadFactory。
     * RejectedExecutionHandler handler)   （拒绝策略）线程池拒绝策略，==任务队列满的时候，我们采取什么措施，比如抛异常、不抛异常、自定义策略==。
     *
     * @return
     */
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {

        //创建线程工厂
        ThreadFactory threadFactory = new ThreadFactory() {

            private int count = 1;

            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("线程:" + count);
                count++;
                return thread;
            }
        };
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 4, 100, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100), threadFactory);
        return threadPoolExecutor;
    }
}
