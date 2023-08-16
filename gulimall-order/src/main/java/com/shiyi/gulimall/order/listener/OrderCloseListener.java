package com.shiyi.gulimall.order.listener;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.rabbitmq.client.Channel;
import com.shiyi.gulimall.order.entity.OrderEntity;
import com.shiyi.gulimall.order.entity.PublishedMsg;
import com.shiyi.gulimall.order.enume.MsgStatusEnum;
import com.shiyi.gulimall.order.service.IPublishedMsgService;
import com.shiyi.gulimall.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author:shiyi
 * @create: 2023-03-10  10:50
 */

@RabbitListener(queues = "order.release.order.queue",concurrency = "10")
@Service
public class OrderCloseListener {

    private static final Logger log = LoggerFactory.getLogger(OrderCloseListener.class);

    private static final Set<String> correlationIdSet = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Autowired
    private OrderService orderService;

    @Autowired
    private IPublishedMsgService publishedMsgService;

    @RabbitHandler
    public void listener(@Payload OrderEntity entity, Message message, Channel channel) throws IOException {
        String correlationId = message.getMessageProperties().getCorrelationId();
        if(correlationIdSet.contains(correlationId)){
            log.warn("消息[id:{}，content:{}]已经消费过了，不能重复消费",correlationId,entity.toString());
            LambdaUpdateWrapper<PublishedMsg> wrapper = Wrappers.<PublishedMsg>lambdaUpdate()
                    .eq(PublishedMsg::getMessageId, correlationId)
                    .set(PublishedMsg::getStatus, MsgStatusEnum.CONSUMED.getCode());
            publishedMsgService.update(wrapper);
            return;
        }
        try{
            log.info("订单[{}]超时主动关闭",entity.getOrderSn());
            orderService.closeOrder(entity);
            publishedMsgService.update(Wrappers.<PublishedMsg>lambdaUpdate()
                    .eq(PublishedMsg::getMessageId,correlationId).set(PublishedMsg::getStatus,MsgStatusEnum.CONSUMED.getCode()));
            correlationIdSet.add(correlationId);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
