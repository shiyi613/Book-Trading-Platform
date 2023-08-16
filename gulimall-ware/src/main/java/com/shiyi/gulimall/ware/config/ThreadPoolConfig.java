package com.shiyi.gulimall.ware.config;

import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Setter
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    private int corePoolSize = 20;
    private int maxPoolSize = 100;
    private int queueCapacity = 500;
    private int keepAliveSeconds = 300;
    private String threadNamePrefix = "task-executor-";

    @Bean(value = "threadPoolRetryTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor(){
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setThreadNamePrefix("retry-executor-");
        threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
        threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
        threadPoolTaskExecutor.setKeepAliveSeconds(keepAliveSeconds);
        return threadPoolTaskExecutor;
    }

    @Bean(value = "threadPoolConsumeTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolConsumeTaskExecutor(){
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setThreadNamePrefix("consume-executor-");
        threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
        threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
        threadPoolTaskExecutor.setKeepAliveSeconds(keepAliveSeconds);
        return threadPoolTaskExecutor;
    }

    @Bean(value = "threadPoolStockUnLockTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolStockUnLockTaskExecutor(){
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setThreadNamePrefix("stock-unlock-executor-");
        threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
        threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
        threadPoolTaskExecutor.setKeepAliveSeconds(keepAliveSeconds);
        return threadPoolTaskExecutor;
    }



}
