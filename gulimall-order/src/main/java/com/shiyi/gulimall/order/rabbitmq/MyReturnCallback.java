package com.shiyi.gulimall.order.rabbitmq;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shiyi.gulimall.order.entity.PublishedMsg;
import com.shiyi.gulimall.order.enume.MsgStatusEnum;
import com.shiyi.gulimall.order.service.IPublishedMsgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MyReturnCallback implements RabbitTemplate.ReturnCallback {

    private static Logger log = LoggerFactory.getLogger(MyReturnCallback.class);

    @Autowired
    private IPublishedMsgService<PublishedMsg> publishedMsgService;

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        log.error("消息[{}]转发到指定队列(交换机+路由键)[{} + {}]失败,replyCode:[{}],replyText:[{}]",message,exchange,routingKey,replyCode,replyText);
        String correlationId = message.getMessageProperties().getCorrelationId();
        LambdaQueryWrapper<PublishedMsg> wrapper = Wrappers.<PublishedMsg>lambdaQuery().eq(PublishedMsg::getMessageId, correlationId);
        PublishedMsg msg = publishedMsgService.getOne(wrapper);
        msg.setStatus(MsgStatusEnum.ERROR_REACHED.getCode());
        publishedMsgService.updateById(msg);
    }
}
