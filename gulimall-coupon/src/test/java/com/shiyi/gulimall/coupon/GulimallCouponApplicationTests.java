package com.shiyi.gulimall.coupon;

import com.shiyi.gulimall.coupon.service.CouponSpuCategoryRelationService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimallCouponApplicationTests {

    @Autowired
    private CouponSpuCategoryRelationService couponSpuCategoryRelationService;

    @Test
    public void contextLoads() {
        System.out.println(couponSpuCategoryRelationService.getCategoryByCouponId(1L));
    }

}
