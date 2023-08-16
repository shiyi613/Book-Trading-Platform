package com.shiyi.gulimall.search.controller;

import com.shiyi.common.exception.BizCodeEnum;
import com.shiyi.common.to.es.SkuEsModel;
import com.shiyi.common.utils.R;
import com.shiyi.gulimall.search.service.ProductUpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author:shiyi
 * @create: 2023-02-28  12:39
 */

@Slf4j
@RequestMapping("/search/save")
@RestController
public class ElasticSaveController {

    @Autowired
    private ProductUpService productUpService;

    /**
     * 上架商品
     */
    @PostMapping("/product")
    public R UpProduct(@RequestBody List<SkuEsModel> skuEsModelList){

        boolean flag = false;
        try {
            flag = productUpService.productUp(skuEsModelList);
        } catch (Exception e) {
            log.error("ElasticSaveController商品上架异常：{}",e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }

        if(!flag){
            return R.ok();
        }else{
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }
    }
}
