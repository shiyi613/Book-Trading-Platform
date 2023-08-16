package com.shiyi.gulimall.order.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author:shiyi
 * @create: 2023-03-09  21:33
 */
@Configuration
public class MyRabbitMQConfig {

    /**
     * 容器中的Queue、Exchange、Binding都会自动创建到RabbitMQ，前提是MQ里没有这些
     * @return
     */
    @Bean
    public Queue orderDelayQueue(){
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        arguments.put("x-dead-letter-routing-key","order.release.order");
        arguments.put("x-message-ttl",60000);

        Queue queue = new Queue("order.delay.queue", true, false, false, arguments);
        return queue;
    }


    @Bean
    public Queue orderReleaseOrderQueue(){

        Queue queue = new Queue("order.release.order.queue", true, false, false);
        return queue;
    }

    @Bean
    public Queue orderSecKillOrderQueue(){

        Queue queue = new Queue("order.seckill.order.queue", true, false, false);
        return queue;
    }

    @Bean
    public Exchange orderEventExchange(){
        //String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new TopicExchange("order-event-exchange",true,false);
    }


    @Bean
    public Binding orderCreateOrderBinding(){
        //String destination, DestinationType destinationType, String exchange, String routingKey,
        //Map<String, Object> arguments
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange", "order.create.order",null);
    }

    @Bean
    public Binding orderReleaseOrderBinding(){

        return new Binding("order.release.order.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange", "order.release.order",null);
    }

    /**
     * 订单释放和库存释放绑定
     */
    @Bean
    public Binding orderReleaseWareBinding(){

        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange", "order.release.other.#",null);
    }

    @Bean
    public Binding orderSecKillOrderBinding(){

        return new Binding("order.seckill.order.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange", "order.seckill.order",null);
    }

    @Bean
    public Queue messageRetryQueue(){
        Queue queue = new Queue("message.retry.queue", true, false, false);
        return queue;
    }

    @Bean
    public Exchange messageRetryExchange(){
        return new DirectExchange("message-event-exchange",true,false);
    }

    @Bean
    public Binding messageRetryBinding(){
        return new Binding("message.retry.queue", Binding.DestinationType.QUEUE,"message-event-exchange",
                "retry",null);
    }


    // 配置
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

//    @Bean
//    public RabbitTransactionManager rabbitTransactionManager(CachingConnectionFactory connectionFactory, RabbitTemplate rabbitTemplate) {
//        // channel开启事务支持
//        rabbitTemplate.setChannelTransacted(true);
//
//        return new RabbitTransactionManager(connectionFactory);
//    }


}
