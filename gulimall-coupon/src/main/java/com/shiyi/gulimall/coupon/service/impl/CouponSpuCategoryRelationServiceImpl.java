package com.shiyi.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.Query;
import com.shiyi.gulimall.coupon.dao.CouponSpuCategoryRelationDao;
import com.shiyi.gulimall.coupon.entity.CouponSpuCategoryRelationEntity;
import com.shiyi.gulimall.coupon.service.CouponSpuCategoryRelationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("couponSpuCategoryRelationService")
public class CouponSpuCategoryRelationServiceImpl extends ServiceImpl<CouponSpuCategoryRelationDao, CouponSpuCategoryRelationEntity> implements CouponSpuCategoryRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CouponSpuCategoryRelationEntity> page = this.page(
                new Query<CouponSpuCategoryRelationEntity>().getPage(params),
                new QueryWrapper<CouponSpuCategoryRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CouponSpuCategoryRelationEntity> getCategoryByCouponId(Long coupunId) {
        return this.baseMapper.selectList(new QueryWrapper<CouponSpuCategoryRelationEntity>().eq("coupon_id", coupunId));
    }

}