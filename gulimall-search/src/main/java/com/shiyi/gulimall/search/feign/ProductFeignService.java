package com.shiyi.gulimall.search.feign;

import com.shiyi.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Author:shiyi
 * @create: 2023-03-02  20:10
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @GetMapping("/product/attr/info/{attrId}")
    R getAttrinfo(@PathVariable("attrId") Long attrId);

    @GetMapping("/product/brand/infos")
    R getBrandsinfo(@RequestParam("brandIds") List<Long> brandIds);

    @GetMapping("/product/category/info/{catId}")
    R getCataloginfo(@PathVariable("catId") Long catId);
}
