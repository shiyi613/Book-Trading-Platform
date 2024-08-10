package com.shiyi.gulimall.order.feign;

import com.shiyi.common.utils.R;
import com.shiyi.gulimall.order.vo.SkuInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author:shiyi
 * @create: 2023-03-09  14:28
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @GetMapping("/product/spuinfo/skuId/{id}")
    R getSpuInfoBySkuId(@PathVariable("id")Long skuId);

    @RequestMapping("/product/spuinfo/infos")
    List<Long> getCategoryIdsBySpuIds(@RequestParam("spuIds") List<Long> spuIds);

    @PostMapping("/product/skuinfo/saleCountAdd")
    R saleCountBatchAdd(List<SkuInfoVo> skuId);
}
