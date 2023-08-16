package com.shiyi.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiyi.common.to.SecKillOrderTo;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.gulimall.order.entity.OrderEntity;
import com.shiyi.gulimall.order.vo.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author shiyi
 * @email 511665483@qq.com
 * @date 2023-02-21 16:27:39
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder();

    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) throws IOException, ExecutionException, InterruptedException;

    OrderEntity getOrderStatus(String orderSn);

    void closeOrder(OrderEntity entity) throws IOException;

    PayVo getOrderPay(String orderSn);

    PageUtils queryPageWithItem(Map<String, Object> params);

    String handlePayResult(PayAsyncVo vo);

    void createSeckillOrder(SecKillOrderTo to);
}

