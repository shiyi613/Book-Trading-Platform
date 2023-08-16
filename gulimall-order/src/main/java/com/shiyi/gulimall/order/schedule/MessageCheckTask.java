package com.shiyi.gulimall.order.schedule;

import com.alibaba.fastjson.JSON;
import com.shiyi.gulimall.order.entity.PublishedMsg;
import com.shiyi.gulimall.order.enume.MsgStatusEnum;
import com.shiyi.gulimall.order.rabbitmq.RabbitUtil;
import com.shiyi.gulimall.order.service.IPublishedMsgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Component
public class MessageCheckTask {

    private final Logger log = LoggerFactory.getLogger(MessageCheckTask.class);

    private final Set<PublishedMsg> needUpdateMsgList = new HashSet<>();

    @Autowired
    private IPublishedMsgService publishedMsgService;

    @Autowired
    private RabbitUtil rabbitUtil;

    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    public void messageCheck() throws ClassNotFoundException, IOException {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // TODO:针对延迟消息的优化，新增延迟时间字段，若当前时间 - 创建时间 > 延迟时间，则此条消息可以被重发
        List<PublishedMsg> failedMessages = publishedMsgService.findMessageByLtStatusAndGtDelayTime(MsgStatusEnum.CONSUMED.getCode());
        if(failedMessages == null || failedMessages.size() == 0){
            log.info("时间：[{}]，没有消息错误抵达或者消费失败的消息", df.format(LocalDateTime.now()));
            return;
        }
        log.info("时间：[{}]，发现消息错误抵达或者消费失败的消息，进行重发操作",df.format(LocalDateTime.now()));
        needUpdateMsgList.clear();
        // TODO:可能发生消息堆积问题，消息进行了重发，但是这个消费是要延迟过期后才消费的，就会导致有多条相同的消息
        //  最简单做法应该是：不理它，做好消费者端的消费重复问题，这里我采用了另外一种方案：添加一个字段保存延迟的时间，用来扫描被允许重发的消息
        for (PublishedMsg failedMessage : failedMessages) {
            if(failedMessage.getRetries() == 1){
                failedMessage.setStatus(MsgStatusEnum.ARTIFICIAL_PAY.getCode());
                publishedMsgService.updateById(failedMessage);
                log.info("消息[id:{}，content:{}]达到重发上限，需要进行人工补偿操作",failedMessage.getMessageId(),failedMessage.getContent());
                continue;
            }

            try {
                rabbitUtil.sendMsg(JSON.parseObject(failedMessage.getContent(), Class.forName(failedMessage.getClassType())),
                        failedMessage.getMessageId(), failedMessage.getExchange(), failedMessage.getRoutingKey());
            } catch (ClassNotFoundException e) {
                log.error(failedMessage.getClassType() + "不存在");
                continue;
            }
            // TODO： 注意这里不要更新它的状态，有可能存在数据覆盖问题，在进入此方法，执行该行代码前，消费线程更新其状态为已消费，
            //   由于这里缺少再次检索状态的操作，减少访问数据库的成本，故这里直接不更新它的状态乃为最佳方案
//            failedMessage.setStatus(MsgStatusEnum.SENT.getCode());
            failedMessage.setRetries(failedMessage.getRetries() - 1);
            needUpdateMsgList.add(failedMessage);
            log.info("消息[id:{}，content:{}]进行了重发操作",failedMessage.getMessageId(),failedMessage.getContent());
        }
        if(!needUpdateMsgList.isEmpty()){
            publishedMsgService.updateBatchById(needUpdateMsgList);
        }
    }



}
