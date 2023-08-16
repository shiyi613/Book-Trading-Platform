package com.shiyi.gulimall.ware.rabbitmq;

import com.shiyi.gulimall.ware.constants.RabbitConstant;
import com.shiyi.gulimall.ware.entity.PublishedMsg;
import com.shiyi.gulimall.ware.event.RabbitApplicationEvent;
import com.shiyi.gulimall.ware.service.IPublishedMsgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class RabbitUtil {

    private static final Logger log = LoggerFactory.getLogger(RabbitUtil.class);

    @Autowired
    private RabbitTemplateFactory rabbitTemplateFactory;

    @Autowired
    private MsgPostProcessor msgPostProcessor;

    @Autowired
    private RabbitUtil rabbitUtil;

    @Autowired
    private IPublishedMsgService publishedMsgService;

    public <T> void sendMsg(T obj, String msgId, String exchangeName, String routingKey){
        RabbitTemplate rabbitTemplate = rabbitTemplateFactory.getRabbitTemplate(exchangeName);
        CorrelationData correlationData = new CorrelationData(msgId);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, obj, msgPostProcessor, correlationData);
        log.info("消息[id:[{}]，content:[{}]]发送到交换机[{}]，路由键[{}]",msgId,obj,exchangeName,routingKey);
    }

    public void retryStorageLimitTimes(PublishedMsg msg, RabbitApplicationEvent<?> event){
        retryStorageLimitTimes(msg, RabbitConstant.DEFAULT_RETRIES_TIMES, event);
    }

    public void retryStorageLimitTimes(PublishedMsg msg,int times, RabbitApplicationEvent<?> event){
        int originTimes = times + 1;
        long startTime = System.currentTimeMillis();
        while(times >= 0){
            log.info("第{}次进行入库重试，尝试入库的消息[{}]",originTimes - times,msg.toString());
            boolean isSaved = publishedMsgService.save(msg);
            if(isSaved){
                log.info("第{}次入库重试成功，入库的消息[{}]",originTimes - times,msg.toString());
                CompletableFuture.runAsync(() -> {
                    rabbitUtil.sendMsg(event.getData(),event.getMessageId(),event.getExchangeName(),event.getRoutingKey());
                });
                return;
            }else{
                log.info("第{}次入库重试失败，入库的消息[{}]",originTimes - times,msg.toString());
                long endTime = System.currentTimeMillis();
                if((endTime - startTime) >= RabbitConstant.DEFAULT_RETRIES_TIMEOUT){
                    log.error("消息[{}]入库重试超时，将进行后续补偿措施",msg.toString());
                    break;
                }
                times--;
            }
        }

        // 次数达到上限 / 重试入库操作超时，发送一条消息到MQ
        sendMsg(msg,msg.getMessageId(), "message-event-exchange","retry");
    }
}
