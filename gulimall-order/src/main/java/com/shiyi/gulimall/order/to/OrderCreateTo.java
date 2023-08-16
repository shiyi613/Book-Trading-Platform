package com.shiyi.gulimall.order.to;

import com.shiyi.gulimall.order.entity.OrderEntity;
import com.shiyi.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author:shiyi
 * @create: 2023-03-09  13:56
 */
@Data
public class OrderCreateTo {

    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    private BigDecimal payPrice;

    private BigDecimal fare;
}
