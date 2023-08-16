package com.shiyi.gulimall.product.feign;

import com.shiyi.common.to.es.SkuEsModel;
import com.shiyi.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author:shiyi
 * @create: 2023-02-28  13:03
 */
@FeignClient("gulimall-search")
public interface SearchFeignService {

    @PostMapping("/search/save/product")
    R UpProduct(@RequestBody List<SkuEsModel> skuEsModelList);
}
