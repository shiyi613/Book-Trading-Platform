package com.shiyi.gulimall.ware.dao;

import com.shiyi.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * 商品库存
 * 
 * @author shiyi
 * @email 511665483@qq.com
 * @date 2023-02-21 16:31:11
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum")Integer skuNum);

    Long getSkuStock(@Param("skuId") Long skuId);

    List<Long> listWareIdHasStockBySkuId(@Param("skuId") Long skuId);

    Long lockSkuStock(@Param("skuId") Long skuId,@Param("wareId") Long wareId,@Param("num") Integer num);

    int unLockStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);
}
