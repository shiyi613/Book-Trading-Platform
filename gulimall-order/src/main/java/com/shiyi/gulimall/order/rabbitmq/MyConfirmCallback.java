package com.shiyi.gulimall.order.rabbitmq;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shiyi.gulimall.order.entity.PublishedMsg;
import com.shiyi.gulimall.order.enume.MsgStatusEnum;
import com.shiyi.gulimall.order.service.IPublishedMsgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@link RabbitTemplate.ConfirmCallback}的触发时机问题：
 * 这个回调是在生产者将消息发送到RabbitMQ Broker后，等待Broker返回确认消息后才会触发
 * 有可能这个回调触发的时间会比消费者消费消息要晚，所以对于实时性要求较高的应用来说要考虑到这一点！
 */
@Component
public class MyConfirmCallback implements RabbitTemplate.ConfirmCallback {

    private final Logger log = LoggerFactory.getLogger(MyConfirmCallback.class);

    @Autowired
    private IPublishedMsgService<PublishedMsg> publishedMsgService;

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        LambdaQueryWrapper<PublishedMsg> wrapper = Wrappers.<PublishedMsg>lambdaQuery().eq(PublishedMsg::getMessageId, correlationData.getId());
        PublishedMsg msg = publishedMsgService.getOne(wrapper);
        if(ack){
            log.info("消息正确抵达MQ Broker, correlationData:{}",correlationData);
            // TODO：解决confirmCallBack触发比消费者消费消息晚，避免修改了消费成功状态，导致重发消息
//            msg.setStatus(MsgStatusEnum.SUCCESS_REACHED.getCode());
        }else{
            log.info("消息错误抵达MQ Broker, correlationData:{}",correlationData);
            msg.setStatus(MsgStatusEnum.ERROR_REACHED.getCode());
            publishedMsgService.updateById(msg);
        }
    }
}
