package com.shiyi.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author:shiyi
 * @create: 2023-03-09  12:01
 */
@Data
public class FareVo {

    private MemberAddressVo address;
    private BigDecimal fare;
}
