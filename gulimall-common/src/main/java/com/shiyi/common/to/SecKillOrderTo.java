package com.shiyi.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author:shiyi
 * @create: 2023-03-13  19:28
 */
@Data
public class SecKillOrderTo {

    private String orderSn;
    private Long promotionSessionId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private Integer num;
    private Long memberId;
}
