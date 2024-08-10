package com.shiyi.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.Query;
import com.shiyi.gulimall.coupon.dao.CouponSpuRelationDao;
import com.shiyi.gulimall.coupon.entity.CouponSpuRelationEntity;
import com.shiyi.gulimall.coupon.service.CouponSpuRelationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("couponSpuRelationService")
public class CouponSpuRelationServiceImpl extends ServiceImpl<CouponSpuRelationDao, CouponSpuRelationEntity> implements CouponSpuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CouponSpuRelationEntity> page = this.page(
                new Query<CouponSpuRelationEntity>().getPage(params),
                new QueryWrapper<CouponSpuRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CouponSpuRelationEntity> getSpuByCouponId(Long couponId) {
        return this.baseMapper.selectList(new QueryWrapper<CouponSpuRelationEntity>().eq("coupon_id", couponId));
    }

}