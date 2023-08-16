package com.shiyi.gulimall.product.web;

import com.shiyi.gulimall.product.entity.CategoryEntity;
import com.shiyi.gulimall.product.service.CategoryService;
import com.shiyi.gulimall.product.vo.Catelog2Vo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @Author:shiyi
 * @create: 2023-02-28  15:23
 */
@Slf4j
@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){

        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();
        model.addAttribute("categorys",categoryEntities);
        return "index";

    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatelogJson(){
        long l = System.currentTimeMillis();
        Map<String, List<Catelog2Vo>> catelogJson = categoryService.getCatelogJson();
        long l1 = System.currentTimeMillis();
        log.error("{},执行时间为{}",Thread.currentThread().getId(),l1-l);
        return catelogJson;
    }
}
