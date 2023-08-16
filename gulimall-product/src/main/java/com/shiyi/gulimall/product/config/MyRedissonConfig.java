package com.shiyi.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author:shiyi
 * @create: 2023-03-01  19:27
 */
@Configuration
public class MyRedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson(){

        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.23.134:6379");

        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
