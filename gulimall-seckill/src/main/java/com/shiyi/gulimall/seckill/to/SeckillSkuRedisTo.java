package com.shiyi.gulimall.seckill.to;

import com.shiyi.gulimall.seckill.vo.SkuInfoVo;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author:shiyi
 * @create: 2023-03-12  22:51
 */
@Data
public class SeckillSkuRedisTo {


    //sku的详细信息
    private SkuInfoVo skuInfo;

    //sku的秒杀信息
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 商品秒杀随机码
     */
    private String randomCode;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private BigDecimal seckillCount;
    /**
     * 每人限购数量
     */
    private BigDecimal seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;

    private Long startTime;

    private Long endTime;

}
