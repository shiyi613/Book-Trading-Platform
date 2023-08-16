package com.shiyi.gulimall.product.vo;

import com.shiyi.gulimall.product.entity.SkuImagesEntity;
import com.shiyi.gulimall.product.entity.SkuInfoEntity;
import com.shiyi.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @Author:shiyi
 * @create: 2023-03-03  12:10
 */
@Data
public class SkuItemVo {

    //1、sku的基本信息
    SkuInfoEntity  info;

    boolean hasStock = true;

    //2、sku的图片信息
    List<SkuImagesEntity> images;

    //3、获取spu的销售属性组合
    List<SkuItemSaleAttrVo> saleAttr;

    //4、spu的介绍
    SpuInfoDescEntity desp;

    //5、spu的规格参数
    List<SpuItemAttrGroupVo> groupAttrs;

    //6、sku的秒杀优惠信息
    SeckillSkuInfoVo seckillInfo;


    @Data
    public static class SkuItemSaleAttrVo{
        private Long attrId;
        private String attrName;
        private List<AttrValueWithSkuIdVo> attrValues;
    }

    @Data
    public static class SpuItemAttrGroupVo{
        private String groupName;
        private List<SpuBaseAttrVo> attrs;
    }

    @Data
    public static class SpuBaseAttrVo{
        private String attrName;
        private String attrValue;
    }
}
