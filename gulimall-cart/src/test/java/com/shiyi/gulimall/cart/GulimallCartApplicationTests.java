package com.shiyi.gulimall.cart;

import com.shiyi.gulimall.cart.feign.ProductFeignService;
import com.shiyi.gulimall.cart.feign.WareFeignService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimallCartApplicationTests {

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Test
    void contextLoads() {
        System.out.println(wareFeignService);
        System.out.println(productFeignService);
        System.out.println(wareFeignService.getSkuHasStock(1L, 1));
    }

}
