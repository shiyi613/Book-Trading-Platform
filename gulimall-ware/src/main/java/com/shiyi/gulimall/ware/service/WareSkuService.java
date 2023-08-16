package com.shiyi.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiyi.common.to.StockLockedTo;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.vo.OrderVo;
import com.shiyi.gulimall.ware.entity.WareSkuEntity;
import com.shiyi.gulimall.ware.vo.SkuHasStockVo;
import com.shiyi.gulimall.ware.vo.WareSkuLockVo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author shiyi
 * @email 511665483@qq.com
 * @date 2023-02-21 16:31:11
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    Boolean getSkuHasStock(Long skuId,int num);

    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVo vo) throws IOException;

    void unlockStock(StockLockedTo to) throws InterruptedException;

    void unlockStock(OrderVo orderEntity) throws InterruptedException;
}

