package com.shiyi.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author:shiyi
 * @create: 2023-02-24  16:30
 */
@Data
public class SkuReductionTo {

    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;
}
