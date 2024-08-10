package com.shiyi.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.gulimall.coupon.entity.CouponSpuRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 优惠券与产品关联
 *
 * @author shiyi
 * @email 511665483@qq.com
 * @date 2023-02-21 16:06:49
 */
public interface CouponSpuRelationService extends IService<CouponSpuRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CouponSpuRelationEntity> getSpuByCouponId(Long couponId);
}

