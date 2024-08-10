package com.shiyi.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.gulimall.coupon.entity.CouponSpuCategoryRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 优惠券分类关联
 *
 * @author shiyi
 * @email 511665483@qq.com
 * @date 2023-02-21 16:06:49
 */
public interface CouponSpuCategoryRelationService extends IService<CouponSpuCategoryRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CouponSpuCategoryRelationEntity> getCategoryByCouponId(Long coupunId);
}

