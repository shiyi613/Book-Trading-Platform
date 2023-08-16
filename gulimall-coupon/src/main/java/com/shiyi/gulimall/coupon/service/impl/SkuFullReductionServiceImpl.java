package com.shiyi.gulimall.coupon.service.impl;

import com.shiyi.common.to.MemberPrice;
import com.shiyi.common.to.SkuReductionTo;
import com.shiyi.gulimall.coupon.entity.MemberPriceEntity;
import com.shiyi.gulimall.coupon.entity.SkuLadderEntity;
import com.shiyi.gulimall.coupon.service.MemberPriceService;
import com.shiyi.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.Query;

import com.shiyi.gulimall.coupon.dao.SkuFullReductionDao;
import com.shiyi.gulimall.coupon.entity.SkuFullReductionEntity;
import com.shiyi.gulimall.coupon.service.SkuFullReductionService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    SkuLadderService skuLadderService;

    @Autowired
    MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        //设置打折
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
        skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
        skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
        if(skuReductionTo.getFullCount() > 0){
            skuLadderService.save(skuLadderEntity);
        }

        //设置满减
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
//        skuFullReductionEntity.setSkuId(skuReductionTo.getSkuId());
//        skuFullReductionEntity.setFullPrice(skuReductionTo.getFullPrice());
//        skuFullReductionEntity.setReducePrice(skuReductionTo.getReducePrice());
//        skuFullReductionEntity.setAddOther(skuReductionTo.getCountStatus());
        BeanUtils.copyProperties(skuReductionTo,skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(skuReductionTo.getPriceStatus());
        if(skuFullReductionEntity.getFullPrice().compareTo(new BigDecimal("0"))==1){
            this.save(skuFullReductionEntity);
        }

        //设置会员价
        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        if(memberPrice != null || memberPrice.size() > 0 ){
            List<MemberPriceEntity> priceEntityList = memberPrice.stream().map(item -> {
                MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                memberPriceEntity.setSkuId(skuReductionTo.getSkuId());
                memberPriceEntity.setMemberLevelId(item.getId());
                memberPriceEntity.setMemberLevelName(item.getName());
                memberPriceEntity.setMemberPrice(item.getPrice());
                memberPriceEntity.setAddOther(1);
                return memberPriceEntity;
            }).filter(item->{
                return item.getMemberPrice().compareTo(new BigDecimal("0")) == 1;
            }).collect(Collectors.toList());

            memberPriceService.saveBatch(priceEntityList);
        }
    }

}