package com.shiyi.gulimall.seckill.service;

import com.shiyi.gulimall.seckill.to.SeckillSkuRedisTo;

import java.util.List;

/**
 * @Author:shiyi
 * @create: 2023-03-12  21:47
 */
public interface SeckillService {
    void uploadSeckillSkuLatest3Days();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    SeckillSkuRedisTo getSkuSeckillInfo(Long skuId);

    String seckill(String killId, String key, Integer num);
}
