package com.shiyi.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author:shiyi
 * @create: 2023-02-24  16:19
 */
@Data
public class SpuBoundTo {

    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
