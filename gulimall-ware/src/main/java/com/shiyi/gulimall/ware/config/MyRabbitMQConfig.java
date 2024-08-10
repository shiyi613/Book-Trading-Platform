package com.shiyi.gulimall.ware.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author:shiyi
 * @create: 2023-03-08  15:04
 */
@Configuration
public class MyRabbitMQConfig {

    @Bean
    public MessageConverter  messageConverter(){
        return new Jackson2JsonMessageConverter();
    }


    @Bean
    public Queue stockDelayQueue(){
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        arguments.put("x-dead-letter-routing-key","stock.release");
        // TODO:注意后面修改回来
        arguments.put("x-message-ttl",2000000);

        Queue queue = new Queue("stock.delay.queue", true, false, false, arguments);
        return queue;
    }


    @Bean
    public Queue stockReleaseStockQueue(){

        Queue queue = new Queue("stock.release.stock.queue", true, false, false);
        return queue;
    }

    @Bean
    public Queue stockPayFinishQueue(){

        Queue queue = new Queue("stock.pay.finish.queue", true, false, false);
        return queue;
    }

    @Bean
    public Exchange stockEventExchange(){
        //String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new TopicExchange("stock-event-exchange",true,false);
    }

    @Bean
    public Binding stockReleaseBinding(){
        //String destination, DestinationType destinationType, String exchange, String routingKey,
        //Map<String, Object> arguments
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE,
                "stock-event-exchange", "stock.release.#",null);
    }

    @Bean
    public Binding stockLockedBinding(){

        return new Binding("stock.delay.queue", Binding.DestinationType.QUEUE,
                "stock-event-exchange", "stock.locked",null);
    }


    @Bean
    public Binding stockPayFinishBinding(){

        return new Binding("stock.pay.finish.queue", Binding.DestinationType.QUEUE,
                "stock-event-exchange", "stock.payFinish",null);
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
