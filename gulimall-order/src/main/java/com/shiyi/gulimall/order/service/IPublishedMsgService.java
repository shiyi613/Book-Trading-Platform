package com.shiyi.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiyi.common.utils.BaseResult;
import com.shiyi.gulimall.order.entity.PublishedMsg;

import java.io.IOException;
import java.util.List;


/**
 * <p>
 * 发布消息表 服务类
 * </p>
 *
 * @author shiyi
 * @since 2023-07-17
 */
public interface IPublishedMsgService<T> extends IService<PublishedMsg> {

    BaseResult<String> saveAndSendMsg(T data, String exchangeName, String routingKey, int delayTime) throws IOException;

    List<PublishedMsg> findMessageByLtStatusAndGtDelayTime(int status);

    List<PublishedMsg> findMessageByStatusAndDelayTime(int status);




}
