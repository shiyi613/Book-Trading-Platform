package com.shiyi.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiyi.common.constant.ProductConstant;
import com.shiyi.common.to.SkuHasStockVo;
import com.shiyi.common.to.es.SkuEsModel;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.Query;
import com.shiyi.common.utils.R;
import com.shiyi.gulimall.product.dao.SpuInfoDao;
import com.shiyi.gulimall.product.entity.*;
import com.shiyi.gulimall.product.feign.CouponFeignService;
import com.shiyi.gulimall.product.feign.SearchFeignService;
import com.shiyi.gulimall.product.feign.WareFeignService;
import com.shiyi.gulimall.product.service.*;
import com.shiyi.gulimall.product.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SearchFeignService searchFeignService;

    @Autowired
    private SkuExtendInfoService skuExtendInfoService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {

        //1、保存spu基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo,spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        //2、保存spu的描述图片 pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",",decript));
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);

        //3、保存spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveSpuInfoImgs(spuInfoEntity.getId(),images);

        //4、保存spu的规格参数 pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map((item) -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setAttrId(item.getAttrId());
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());
            AttrEntity attrEntity = attrService.getById(item.getAttrId());
            productAttrValueEntity.setAttrName(attrEntity.getAttrName());
            productAttrValueEntity.setAttrValue(item.getAttrValues());
            productAttrValueEntity.setQuickShow(item.getShowDesc());

            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(collect);

        //5、保存spu的积分信息 gulimall_sms-》sms_spu_bounds
//        Bounds bounds = vo.getBounds();
//        SpuBoundTo spuBoundTo = new SpuBoundTo();
//        BeanUtils.copyProperties(bounds,spuBoundTo);
//        spuBoundTo.setSpuId(spuInfoEntity.getId());
//        R r = couponFeignService.saveSpuBounds(spuBoundTo);
//        if(r.getCode() != 0){
//            log.error("远程保存spu积分信息失败");
//        }

        //6、保存spu对应sku信息
        //6.1、保存sku的基本信息 pms_sku_info
        List<Skus> skus = vo.getSkus();
        if(skus != null && skus.size() > 0){
            skus.forEach((item)->{
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    if(image.getDefaultImg() == 1){
                        defaultImg = image.getImgUrl();
                    }
                }

                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item,skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                //6.2、保存sku的图片信息 pms_sku_images
                List<SkuImagesEntity> skuImagesEntityList = item.getImages().stream().map((img) -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity->{
                    //返回true就是需要，false就是剔除
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntityList);

                //6.3、保存sku的销售属性信息 pms_sku_sale_attr_value
                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntityList = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntityList);

                //6.4、保存sku的打折、满减信息、会员价格等 gulimall_sms-》sms_sku_ladder、sms_sku_full_reduction、sms_member_price
//                SkuReductionTo skuReductionTo = new SkuReductionTo();
//                BeanUtils.copyProperties(item,skuReductionTo);
//                skuReductionTo.setSkuId(skuId);
//                if(skuReductionTo.getFullCount() > 0 ||
//                skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1){
//                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
//                    if(r1.getCode() != 0){
//                        log.error("远程保存sku优惠信息失败");
//                    }
//                }

                // 6.5 保存sku的扩展信息
                SkuExtendInfoEntity skuExtendInfoEntity = new SkuExtendInfoEntity();
                skuExtendInfoEntity.setId(skuId);
                skuExtendInfoEntity.setBrandImg(brandService.getBrandImgById(skuInfoEntity.getBrandId()));
                skuExtendInfoEntity.setAttrs(JSON.toJSONString(item.getAttr()));
                SkuHasStockDto data = wareFeignService.getSkuHasStock(skuId).getData(new TypeReference<SkuHasStockDto>() {});
                skuExtendInfoEntity.setHasStock((byte) (data.getHasStock() ? 1 : 0));
                skuExtendInfoEntity.setHotScore(0L);
                skuExtendInfoService.save(skuExtendInfoEntity);
            });

        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String)params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and(item->{
                item.eq("id",key).or().like("spu_name",key);
            });
        }

        String status = (String)params.get("status");
        if(!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status",status);
        }

        String brandId = (String)params.get("brandId");
        if(!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }

        String catelogId = (String)params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)){
            wrapper.eq("catalog_id",catelogId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        //1、查出当前spuId对应的所有sku信息，品牌的名字
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIds = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        //查询当前sku的所有可以被用来检索的规格属性
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.baseAttrListforSpu(spuId);
        List<Long> attrIds = productAttrValueEntities.stream().map(item -> item.getAttrId()).collect(Collectors.toList());
        List<Long> attrSearchIds = attrService.selectSearchAttrs(attrIds);
        Set<Long> attrIdSet = new HashSet<>(attrSearchIds);
        List<SkuEsModel.Attrs> attrSearchableList = productAttrValueEntities.stream().filter(item -> {
            return attrIdSet.contains(item.getAttrId());
        }).map(item -> {
            SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs);
            return attrs;
        }).collect(Collectors.toList());
        //1、远程调用，库存系统是否有库存
        Map<Long, Boolean> stockMap = null;
        try{
            R skuHasStock = wareFeignService.getSkuHasStock(skuIds);
            //匿名内部类
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>() {};
            stockMap = skuHasStock.getData(typeReference).stream()
                    .collect(Collectors.toMap(SkuHasStockVo::getSkuId, item -> item.getHasStock()));
        }catch (Exception e){
            log.error("库存服务查询异常：原因{}",e);
        }
        //2、封装信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> skuEsModelList = skuInfoEntities.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();    skuEsModel.setSkuId(sku.getSkuId());
            skuEsModel.setSpuId(sku.getSpuId());   skuEsModel.setSkuTitle(sku.getSkuTitle());
            skuEsModel.setSkuPrice(sku.getPrice());  skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            skuEsModel.setSaleCount(sku.getSaleCount()); skuEsModel.setBrandId(sku.getBrandId());
            skuEsModel.setCatalogId(sku.getCatalogId());
            //1、设置是否有库存
            if (finalStockMap == null) {
                skuEsModel.setHasStock(true);
            } else {
                skuEsModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }
            //2、热度评分 0
            skuEsModel.setHotScore(0L);
            //3、查询品牌和分类的名字信息
            BrandEntity brandEntity = brandService.getById(sku.getBrandId());
            skuEsModel.setBrandName(brandEntity.getName());
            skuEsModel.setBrandImg(brandEntity.getLogo());
            CategoryEntity categoryEntity = categoryService.getById(sku.getCatalogId());
            skuEsModel.setCatalogName(categoryEntity.getName());
            //4、设置检索属性
            skuEsModel.setAttrs(attrSearchableList);
            return skuEsModel;
        }).collect(Collectors.toList());

        //3、将封装对象发送给elasticsearch
        R r = searchFeignService.UpProduct(skuEsModelList);
        if(r.getCode() == 0){
            //远程调用成功
            //需要修改Spu的状态
            this.baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.UP_SPU.getCode());
        }else{
            //远程调用失败

        }

    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {

        //先根据skuId查出其属于哪个spuId，再根据spuId查出商品的SPU信息
        SkuInfoEntity skuInfoEntity = skuInfoService.getById(skuId);
        return this.baseMapper.selectById(skuInfoEntity.getSpuId());
    }


}