package com.shiyi.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.shiyi.common.utils.R;
import com.shiyi.gulimall.product.entity.SkuImagesEntity;
import com.shiyi.gulimall.product.entity.SpuInfoDescEntity;
import com.shiyi.gulimall.product.feign.SeckillFeignService;
import com.shiyi.gulimall.product.service.*;
import com.shiyi.gulimall.product.vo.SeckillSkuInfoVo;
import com.shiyi.gulimall.product.vo.SkuItemVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.Query;

import com.shiyi.gulimall.product.dao.SkuInfoDao;
import com.shiyi.gulimall.product.entity.SkuInfoEntity;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {


    @Autowired
    private SkuImagesService imagesService;

    @Autowired
    private SpuInfoDescService descService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuSaleAttrValueService saleAttrValueService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private SeckillFeignService seckillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(item -> {
                item.eq("sku_id", key).or().like("sku_name", key);
            });
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }

        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            //大于等于
            wrapper.ge("price", min);
        }

        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)) {
            //小于等于
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal("0")) == 1) {
                    wrapper.le("price", max);
                }
            } catch (Exception e) {

            }
        }


        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> skuInfoEntities = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return skuInfoEntities;
    }


    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {

        SkuItemVo skuItemVo = new SkuItemVo();



        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //1、sku的基本信息
            SkuInfoEntity info = getById(skuId);
            skuItemVo.setInfo(info);
            return info;
        }, executor);



        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            //2、sku的图片信息
            List<SkuImagesEntity> images = imagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        }, executor);


        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync(res -> {
            //3、获取spu的销售属性组合
            List<SkuItemVo.SkuItemSaleAttrVo> saleAttr = saleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            skuItemVo.setSaleAttr(saleAttr);
        }, executor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync(res -> {
            //4、spu的介绍
            SpuInfoDescEntity byId = descService.getById(res.getSpuId());
            skuItemVo.setDesp(byId);
        }, executor);

        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync(res -> {
            //5、spu的规格参数
            List<SkuItemVo.SpuItemAttrGroupVo> groupAttrs = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(groupAttrs);
        }, executor);

        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            //6、sku的秒杀优惠信息
            R skuSeckillInfo = seckillFeignService.getSkuSeckillInfo(skuId);
            if (skuSeckillInfo.getCode() == 0) {
                SeckillSkuInfoVo data = skuSeckillInfo.getData(new TypeReference<SeckillSkuInfoVo>() {
                });
                skuItemVo.setSeckillInfo(data);
            }
        }, executor);


        CompletableFuture.allOf(imageFuture, saleAttrFuture, descFuture, baseAttrFuture,seckillFuture).get();

        return skuItemVo;
    }


}