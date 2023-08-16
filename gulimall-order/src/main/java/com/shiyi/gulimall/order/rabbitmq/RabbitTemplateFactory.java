package com.shiyi.gulimall.order.rabbitmq;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RabbitTemplateFactory {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private MyConfirmCallback myConfirmCallback;

    @Autowired
    private MyReturnCallback myReturnCallback;

    @Autowired
    private Jackson2JsonMessageConverter jackson2JsonMessageConverter;

    //RabbitTemplate池化
    private static final Map<String, RabbitTemplate> rabbitTemplateMap = new ConcurrentHashMap<>();

    public RabbitTemplate getRabbitTemplate(String exchangeName){
        RabbitTemplate rabbitTemplate = rabbitTemplateMap.get(exchangeName);
        if(rabbitTemplate != null){
            return rabbitTemplate;
        }

        rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setExchange(exchangeName);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter);
        rabbitTemplate.setConfirmCallback(myConfirmCallback);
        rabbitTemplate.setReturnCallback(myReturnCallback);
        return rabbitTemplate;
    }



}
