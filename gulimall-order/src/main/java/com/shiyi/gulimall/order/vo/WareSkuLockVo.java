package com.shiyi.gulimall.order.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author:shiyi
 * @create: 2023-03-09  15:48
 */
@Data
public class WareSkuLockVo {

    private String orderSn;    //订单号

    private List<OrderItemVo> locks;    //所要锁住的所有商品项
}
