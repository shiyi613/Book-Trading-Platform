package com.shiyi.gulimall.order.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author:shiyi
 * @create: 2023-03-09  16:09
 */
@Data
public class SkuWareHasStock {
    private Long skuId;
    private List<Long> wareId;
}
