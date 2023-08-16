package com.shiyi.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author:shiyi
 * @create: 2023-03-09  14:09
 */
@Data
public class FareVo {

    private MemberAddressVo address;
    private BigDecimal fare;
}
