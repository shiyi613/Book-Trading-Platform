package com.shiyi.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.gulimall.order.entity.OrderOperateHistoryEntity;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 订单操作历史记录
 *
 * @author shiyi
 * @email 511665483@qq.com
 * @date 2023-02-21 16:27:39
 */
public interface OrderOperateHistoryService extends IService<OrderOperateHistoryEntity> {


    PageUtils queryPage(Map<String, Object> params);
}

