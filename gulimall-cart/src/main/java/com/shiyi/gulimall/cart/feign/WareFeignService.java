package com.shiyi.gulimall.cart.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/lock/hasStock")
    Boolean getSkuHasStock(@RequestParam("skuId")Long skuId,@RequestParam("num")int num);

}
