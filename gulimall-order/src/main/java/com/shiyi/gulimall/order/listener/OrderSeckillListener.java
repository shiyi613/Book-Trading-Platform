package com.shiyi.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.shiyi.common.to.SecKillOrderTo;
import com.shiyi.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author:shiyi
 * @create: 2023-03-13  19:40
 */
@Slf4j
@Component
@RabbitListener(queues = "order.seckill.order.queue")
public class OrderSeckillListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void listener(SecKillOrderTo to, Channel channel, Message message) {
        log.info("准备秒杀单的信息:{}",to.toString());
        orderService.createSeckillOrder(to);
    }
}
