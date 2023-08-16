package com.shiyi.gulimall.seckill.feign;

import com.shiyi.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author:shiyi
 * @create: 2023-03-12  23:04
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {


    @RequestMapping("/product/skuinfo/info/{skuId}")
    R getSkuinfoBySkuId(@PathVariable("skuId") Long skuId);

}
