package com.shiyi.gulimall.product.web;

import com.shiyi.gulimall.product.service.SkuInfoService;
import com.shiyi.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

/**
 * @Author:shiyi
 * @create: 2023-03-03  12:03
 */
@Controller
public class ItemController {

    @Autowired
    private SkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {

        SkuItemVo skuItemInfo = skuInfoService.item(skuId);
        model.addAttribute("item",skuItemInfo);

        return "item";
    }
}
