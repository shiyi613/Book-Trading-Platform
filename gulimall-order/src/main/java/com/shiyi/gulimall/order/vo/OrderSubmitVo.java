package com.shiyi.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author:shiyi
 * @create: 2023-03-09  13:22
 */
@Data
public class OrderSubmitVo {

    //收货地址id
    private Long addrId;
    //支付方式
    private Integer payType;
    //无需提交需要购买的商品，去购物车再查一遍

    //防重令牌
    private String orderToken;

    //应付总额
    private BigDecimal payPrice;

    //用户相关信息，直接去session拿

    //订单备注
    private String note;
}
