package com.primihub.biz.config.thread;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ThreadPoolConfig {

    @Bean("primaryThreadPool")
    public ThreadPoolTaskExecutor simpleThreadPool(){
        ThreadPoolTaskExecutor primaryThreadPool = new ThreadPoolTaskExecutor();
        primaryThreadPool.setCorePoolSize(20);
        primaryThreadPool.setMaxPoolSize(100);
        primaryThreadPool.setQueueCapacity(100);
        primaryThreadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        primaryThreadPool.initialize();
        return primaryThreadPool;
    }

}
