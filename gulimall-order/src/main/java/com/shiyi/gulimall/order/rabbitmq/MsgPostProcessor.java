package com.shiyi.gulimall.order.rabbitmq;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Correlation;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.stereotype.Component;

@Component
public class MsgPostProcessor implements MessagePostProcessor {

    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        return message;
    }

    @Override
    public Message postProcessMessage(Message message, Correlation correlation) {
        MessageProperties messageProperties = message.getMessageProperties();
        if(correlation instanceof CorrelationData){
            String id = ((CorrelationData)correlation).getId();
            messageProperties.setCorrelationId(id);
        }
        return message;
    }

}
