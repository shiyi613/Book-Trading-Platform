package com.shiyi.gulimall.ware.listener;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.rabbitmq.client.Channel;
import com.shiyi.common.to.StockLockedTo;
import com.shiyi.common.vo.OrderVo;
import com.shiyi.gulimall.ware.entity.PublishedMsg;
import com.shiyi.gulimall.ware.enums.MsgStatusEnum;
import com.shiyi.gulimall.ware.service.IPublishedMsgService;
import com.shiyi.gulimall.ware.service.WareSkuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author:shiyi
 * @create: 2023-03-10  9:44
 */

@RabbitListener(queues = "stock.release.stock.queue",concurrency = "10")
@Service
public class StockReleaseListener {

    private static final Logger log = LoggerFactory.getLogger(StockReleaseListener.class);

    // value为订单号，不能为消息ID，因为订单关闭发送的库存解锁消息和提交订单库存解锁消息的ID是不同的
    private static final Set<String> correlationIdSet = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Autowired
    private WareSkuService wareSkuService;

    @Autowired
    private IPublishedMsgService publishedMsgService;

    /**
     * 监听到订单关闭的消息，被动解锁库存逻辑
     * @param orderEntity
     * @param message
     * @param channel
     */
    @RabbitHandler
    public void handleOrderCloseRelease(OrderVo orderEntity, Message message, Channel channel) throws IOException {
        String orderSn = orderEntity.getOrderSn();
        String correlationId = message.getMessageProperties().getCorrelationId();
        boolean isRepeated = repeatCheckBeforeStockLockedRelease(orderSn, correlationId);
        if(isRepeated){
            log.warn("消息[id:{}，content:{}]已经消费过了，不能重复消费",correlationId,orderEntity.toString());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return;
        }
        try{
            log.info(">>>>> 订单[{}]关闭【被动】触发解锁库存逻辑",orderEntity.getOrderSn());
            wareSkuService.unlockStock(orderEntity);
            correlationIdSet.add(orderSn);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

    /**
     * 防止订单关闭发送的库存解锁消息延迟，这里是主动触发库存解锁逻辑（补偿措施）：
     * @param to
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        String orderSn = to.getOrderSn();
        String correlationId = message.getMessageProperties().getCorrelationId();
        boolean isRepeated = repeatCheckBeforeStockLockedRelease(orderSn, correlationId);
        if(isRepeated){
            log.warn("消息[id:{}，content:{}]已经消费过了，不能重复消费",correlationId,to.toString());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return;
        }
        try{
            log.info(">>>>> 订单[{}]【主动】触发库存解锁逻辑",to.getOrderSn());
            wareSkuService.unlockStock(to);
            correlationIdSet.add(orderSn);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

    private boolean repeatCheckBeforeStockLockedRelease(String orderSn, String correlationId){
        if(correlationIdSet.contains(orderSn)){
            LambdaUpdateWrapper<PublishedMsg> wrapper = Wrappers.<PublishedMsg>lambdaUpdate()
                    .eq(PublishedMsg::getMessageId, correlationId)
                    .set(PublishedMsg::getStatus, MsgStatusEnum.CONSUMED.getCode());
            publishedMsgService.update(wrapper);
            return true;
        }
        return false;
    }

}
