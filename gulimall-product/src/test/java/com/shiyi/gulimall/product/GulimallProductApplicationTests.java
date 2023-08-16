package com.shiyi.gulimall.product;

import com.shiyi.gulimall.product.entity.SkuExtendInfoEntity;
import com.shiyi.gulimall.product.service.SkuExtendInfoService;
import com.shiyi.gulimall.product.service.SpuInfoService;
import com.shiyi.gulimall.product.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;

@Slf4j
@Service
@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    private SkuExtendInfoService skuExtendInfoService;

    @Autowired
    private SpuInfoService spuInfoService;


    @Test
    void test(){
        SkuExtendInfoEntity skuExtendInfoEntity = new SkuExtendInfoEntity();
        skuExtendInfoEntity.setId(12L);
        skuExtendInfoEntity.setHasStock((byte) 0);
        skuExtendInfoService.save(skuExtendInfoEntity);
    }

    @Test
    void contextLoads() {
        SpuSaveVo spuSaveVo = new SpuSaveVo();
        spuSaveVo.setSpuName("华为鸿蒙手机");
        spuSaveVo.setSpuDescription("HUAWEI Mate 50 直屏旗舰 超可靠昆仑玻璃 超光变XMAGE影像 北斗卫星消息 256GB曜金黑");
        spuSaveVo.setCatalogId(225L);
        spuSaveVo.setBrandId(3L);
        spuSaveVo.setWeight(new BigDecimal(1));
        spuSaveVo.setPublishStatus(1);
        ArrayList<String> list1 = new ArrayList<>();
        list1.add("https://gulimall-shiyi1.oss-cn-guangzhou.aliyuncs.com/2023-03-03/890d318c-6937-407d-86b3-2662fa11b009_63f9f3c6dfdab6404.jpg_e680.jpg");
        list1.add("https://gulimall-shiyi1.oss-cn-guangzhou.aliyuncs.com/2023-03-03/108b4e5c-6794-4cbc-802f-ee0e12ae1d95_63f9f3c51f1415267.jpg_e680.jpg");
        list1.add("https://gulimall-shiyi1.oss-cn-guangzhou.aliyuncs.com/2023-03-03/02f6cf57-3bd5-4c13-82da-82f94811505b_63f9f3c7976d85606.jpg_e680.jpg");
        spuSaveVo.setDecript(Lists.newArrayList("https://gulimall-shiyi1.oss-cn-guangzhou.aliyuncs.com/2023-03-03/dbc29895-760a-4578-80e7-e99e9a36f6f7_210924114033E52-0-lp.jpg"));
        spuSaveVo.setImages(list1);

        ArrayList<BaseAttrs> list4= new ArrayList<>();

        BaseAttrs baseAttrs = new BaseAttrs();
        baseAttrs.setAttrId(23L);
        baseAttrs.setAttrValues("2023-03-03");
        baseAttrs.setShowDesc(1);
        BaseAttrs baseAttrs1 = new BaseAttrs();
        baseAttrs1.setAttrId(24L);
        baseAttrs1.setAttrValues("华为 Mate50");
        baseAttrs1.setShowDesc(1);
        BaseAttrs baseAttrs2 = new BaseAttrs();
        baseAttrs2.setAttrId(25L);
        baseAttrs2.setAttrValues("宽76.1mm；长161.5mm；厚7.98mm");
        baseAttrs2.setShowDesc(1);
        BaseAttrs baseAttrs3 = new BaseAttrs();
        baseAttrs3.setAttrId(26L);
        baseAttrs3.setAttrValues("第一代骁龙8+ 4G");
        baseAttrs3.setShowDesc(1);
        list4.add(baseAttrs);
        list4.add(baseAttrs1);
        list4.add(baseAttrs2);
        list4.add(baseAttrs3);
        spuSaveVo.setBaseAttrs(list4);

        Skus skus1 = new Skus();
        Skus skus2 = new Skus();
        Skus skus3 = new Skus();

        ArrayList<Skus> list2 = new ArrayList<>();

        ArrayList<Attr> l0 = new ArrayList<>();
        l0.add(new Attr(21L,"颜色","粉色"));
        l0.add(new Attr(22L,"版本","256GB"));
        skus1.setAttr(l0);
        ArrayList<Attr> l1 = new ArrayList<>();
        l1.add(new Attr(21L,"颜色","粉色"));
        l1.add(new Attr(22L,"版本","128GB"));
        skus2.setAttr(l1);
        ArrayList<Attr> l2 = new ArrayList<>();
        l2.add(new Attr(21L,"颜色","粉色"));
        l2.add(new Attr(22L,"版本","64GB"));
        skus3.setAttr(l2);


        skus1.setSkuName("华为鸿蒙手机 粉色 256GB");
        skus2.setSkuName("华为鸿蒙手机 粉色 128GB");
        skus3.setSkuName("华为鸿蒙手机 粉色 64GB");
        skus1.setPrice(new BigDecimal(5999));
        skus2.setPrice(new BigDecimal(4999));
        skus3.setPrice(new BigDecimal(3999));
        skus1.setSkuTitle("华为鸿蒙手机 粉色 256GB");
        skus2.setSkuTitle("华为鸿蒙手机 粉色 128GB");
        skus3.setSkuTitle("华为鸿蒙手机 粉色 64GB");
        skus1.setSkuSubtitle("华为鸿蒙手机 粉色 256GB");
        skus2.setSkuSubtitle("华为鸿蒙手机 粉色 128GB");
        skus3.setSkuSubtitle("华为鸿蒙手机 粉色 64GB");
        skus1.setImages(Lists.newArrayList(
                new Images("https://gulimall-shiyi1.oss-cn-guangzhou.aliyuncs.com/2023-03-03/890d318c-6937-407d-86b3-2662fa11b009_63f9f3c6dfdab6404.jpg_e680.jpg",1),
                new Images("https://gulimall-shiyi1.oss-cn-guangzhou.aliyuncs.com/2023-03-03/108b4e5c-6794-4cbc-802f-ee0e12ae1d95_63f9f3c51f1415267.jpg_e680.jpg",0),
                new Images("https://gulimall-shiyi1.oss-cn-guangzhou.aliyuncs.com/2023-03-03/02f6cf57-3bd5-4c13-82da-82f94811505b_63f9f3c7976d85606.jpg_e680.jpg",0)
        ));
        skus2.setImages(Lists.newArrayList(
                new Images("https://gulimall-shiyi1.oss-cn-guangzhou.aliyuncs.com/2023-03-03/890d318c-6937-407d-86b3-2662fa11b009_63f9f3c6dfdab6404.jpg_e680.jpg",0),
                new Images("https://gulimall-shiyi1.oss-cn-guangzhou.aliyuncs.com/2023-03-03/108b4e5c-6794-4cbc-802f-ee0e12ae1d95_63f9f3c51f1415267.jpg_e680.jpg",1),
                new Images("https://gulimall-shiyi1.oss-cn-guangzhou.aliyuncs.com/2023-03-03/02f6cf57-3bd5-4c13-82da-82f94811505b_63f9f3c7976d85606.jpg_e680.jpg",0)
        ));
        skus3.setImages(Lists.newArrayList(
                new Images("https://gulimall-shiyi1.oss-cn-guangzhou.aliyuncs.com/2023-03-03/890d318c-6937-407d-86b3-2662fa11b009_63f9f3c6dfdab6404.jpg_e680.jpg",0),
                new Images("https://gulimall-shiyi1.oss-cn-guangzhou.aliyuncs.com/2023-03-03/108b4e5c-6794-4cbc-802f-ee0e12ae1d95_63f9f3c51f1415267.jpg_e680.jpg",0),
                new Images("https://gulimall-shiyi1.oss-cn-guangzhou.aliyuncs.com/2023-03-03/02f6cf57-3bd5-4c13-82da-82f94811505b_63f9f3c7976d85606.jpg_e680.jpg",1)
        ));
        list2.add(skus1);
        list2.add(skus2);
        list2.add(skus3);
        spuSaveVo.setSkus(list2);

        spuInfoService.saveSpuInfo(spuSaveVo);
    }


    class ListNode{
        ListNode next;
        int val;

        public ListNode(int val){
            this.val = val;
        }
    }





}
