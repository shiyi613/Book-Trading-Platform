package com.shiyi.gulimall.seckill.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author:shiyi
 * @create: 2023-03-13  19:25
 */
@Configuration
public class MyRabbitMqConfig {

    @Bean
    public MessageConverter messageConverter(){

        return new Jackson2JsonMessageConverter();
    }
}
