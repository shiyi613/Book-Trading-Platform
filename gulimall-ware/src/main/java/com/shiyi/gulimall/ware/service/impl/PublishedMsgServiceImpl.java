package com.shiyi.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiyi.common.utils.BaseResult;
import com.shiyi.gulimall.ware.constants.RabbitConstant;
import com.shiyi.gulimall.ware.dao.PublishedMsgMapper;
import com.shiyi.gulimall.ware.entity.PublishedMsg;
import com.shiyi.gulimall.ware.enums.MsgStatusEnum;
import com.shiyi.gulimall.ware.event.RabbitApplicationEvent;
import com.shiyi.gulimall.ware.event.RetryStoreApplicationEvent;
import com.shiyi.gulimall.ware.rabbitmq.SingletonFactory;
import com.shiyi.gulimall.ware.service.IPublishedMsgService;
import com.shiyi.gulimall.ware.utils.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <p>
 * 发布消息表 服务实现类
 * </p>
 *
 * @author shiyi
 * @since 2023-07-17
 */
@Service
public class PublishedMsgServiceImpl<T> extends ServiceImpl<PublishedMsgMapper, PublishedMsg> implements IPublishedMsgService<T>{

    private static final Logger log = LoggerFactory.getLogger(PublishedMsgServiceImpl.class);

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    @Qualifier(value = "threadPoolRetryTaskExecutor")
    private ThreadPoolTaskExecutor threadPoolRetryTaskExecutor;



    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResult<String> saveAndSendMsg(T data, String exchangeName, String routingKey, int delayTime) throws IOException {

        ObjectMapper objectMapper = SingletonFactory.getInstance(ObjectMapper.class);
        PublishedMsg message = null;
        try {
            message = PublishedMsg.builder()
                        .messageId(UUID.randomUUID().toString().substring(0, 8))
                        .content(objectMapper.writeValueAsString(data))
                        .classType(data.getClass().getCanonicalName())
                        .exchange(exchangeName)
                        .routingKey(routingKey)
                        .status(MsgStatusEnum.CREATED.getCode())
                        .retries(RabbitConstant.DEFAULT_RETRIES_TIMES)
                        .delayTime(delayTime)
                        .build();
        } catch (JsonProcessingException e) {
            log.error("对象user{}序列化失败",data);
            throw new IOException("对象user[" + data + "]序列化失败");
        }
        boolean isInserted = this.save(message);
        if(!isInserted){
            log.warn("消息[{}]入库失败，后续进行重试入库操作",message.toString());
            // TODO:异步入库重试
            SpringUtil.publishEvent(new RetryStoreApplicationEvent<T>(message, data, message.getMessageId(), exchangeName, routingKey));
            return BaseResult.buildFailedResult("业务数据入库成功，消息数据入库失败");
        }else{
            log.info("消息[{}]成功入库",message.toString());
            SpringUtil.publishEvent(new RabbitApplicationEvent<T>(data,message.getMessageId(),exchangeName,routingKey));
            return BaseResult.buildSuccessfulResult("success");

            // 方式一：使用事务同步器
//            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
//                @Override
//                public void afterCommit() {
//                    rabbitUtil.sendMsg(user,msg.getMessageId(),RabbitConstant.SHIYI_TEST_EXCHANGE_NAME,RabbitConstant.SHIYI_TEST_EXCHANGE_ROUTING_KEY);
//                }
//            });
        }
    }

    @Override
    public List<PublishedMsg> findMessageByLtStatusAndGtDelayTime(int status) {
        List<PublishedMsg> messageByLtStatus = baseMapper.findMessageByLtStatus(status);
        return messageByLtStatus.stream().filter(item -> {
            LocalDateTime now = LocalDateTime.now();
            Instant instantNow = now.toInstant(ZoneOffset.of("+8"));
            Instant instantCreate = item.getCreateTime().toInstant(ZoneOffset.of("+8"));
            long betweenTime = Duration.between(instantCreate, instantNow).getSeconds() * 1000;
            return betweenTime > item.getDelayTime();
        }).collect(Collectors.toList());
    }

    @Override
    public List<PublishedMsg> findMessageByStatusAndDelayTime(int status) {
        return baseMapper.findMessageByStatusAndDelayTime(status);
    }


}
