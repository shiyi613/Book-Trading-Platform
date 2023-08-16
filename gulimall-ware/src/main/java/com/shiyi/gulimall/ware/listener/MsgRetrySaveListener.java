package com.shiyi.gulimall.ware.listener;

import com.rabbitmq.client.Channel;
import com.shiyi.gulimall.ware.entity.PublishedMsg;
import com.shiyi.gulimall.ware.rabbitmq.RabbitUtil;
import com.shiyi.gulimall.ware.service.IPublishedMsgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MsgRetrySaveListener {

    private final Logger log = LoggerFactory.getLogger(MsgRetrySaveListener.class);

    private static final Set<String> correlationIdSet = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Autowired
    @Qualifier(value = "threadPoolConsumeTaskExecutor")
    private ThreadPoolTaskExecutor threadPoolConsumeTaskExecutor;

    @Autowired
    private IPublishedMsgService publishedMsgService;

    @Autowired
    private RabbitUtil rabbitUtil;

//    @RabbitListener(queues = {"shiyi.test.queue"},concurrency = "10")
//    public void getMsg(@Payload User msg, Message message, Channel channel) throws IOException {
//
//        threadPoolConsumeTaskExecutor.execute(() -> {
//            try{
//                String correlationId = message.getMessageProperties().getCorrelationId();
//                if(correlationIdSet.contains(correlationId)){
//                    log.error("消息[id:{}，content:{}]已经消费过了，不能重复消费",correlationId,msg.toString());
//
//                    channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
//                    return;
//                }
//                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//                LambdaQueryWrapper<PublishedMsg> wrapper = Wrappers.<PublishedMsg>lambdaQuery().eq(PublishedMsg::getMessageId, correlationId);
//                PublishedMsg publishedMsg = publishedMsgService.getOne(wrapper);
//                publishedMsg.setStatus(MsgStatusEnum.CONSUMED.getCode());
//                publishedMsgService.updateById(publishedMsg);
//                log.info("channel{}，消费消息时间：{}  ",channel.getChannelNumber(),df.format(LocalDateTime.now()));
//                log.info("user:{}，mgsId:{}，deliverTag:{}" ,msg, message.getMessageProperties().getCorrelationId(),message.getMessageProperties().getDeliveryTag());
//                correlationIdSet.add(correlationId);
//                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
//            }catch (Exception e){
//                log.error("消息[{}]消费失败!",message);
//                try {
//                    channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
//                } catch (IOException ioException) {
//                    log.error(ioException.getMessage() + ioException.getCause());
//                }
//            }
//        });
//    }

    @RabbitListener(queues = {"message.retry.queue"})
    public void retrySaveMsgAndSend(@Payload PublishedMsg msg, Message message, Channel channel) throws IOException {
        try{
            log.info("MQ接受到重试消息[{}]，开始入库重试",msg.toString());
            boolean isSaved = publishedMsgService.save(msg);
            if(isSaved){
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
                rabbitUtil.sendMsg(msg.getContent(),msg.getMessageId(), msg.getExchange(), msg.getRoutingKey());
            }else{
                channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
            }
        } catch (IOException e) {
            log.error("MQ入库重试失败," + e);
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }


}
