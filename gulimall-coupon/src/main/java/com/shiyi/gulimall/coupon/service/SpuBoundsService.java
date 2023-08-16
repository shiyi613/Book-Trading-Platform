package com.shiyi.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.gulimall.coupon.entity.SpuBoundsEntity;

import java.util.Map;

/**
 * 商品spu积分设置
 *
 * @author shiyi
 * @email 511665483@qq.com
 * @date 2023-02-21 16:06:49
 */
public interface SpuBoundsService extends IService<SpuBoundsEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

