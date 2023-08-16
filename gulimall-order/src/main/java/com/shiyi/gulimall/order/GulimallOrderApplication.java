package com.shiyi.gulimall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@EnableRedisHttpSession
@EnableFeignClients
@EnableDiscoveryClient
@EnableRabbit
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class GulimallOrderApplication {


    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);

    }

}
