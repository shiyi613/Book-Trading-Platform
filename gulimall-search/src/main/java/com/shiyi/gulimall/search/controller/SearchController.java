package com.shiyi.gulimall.search.controller;

import com.shiyi.gulimall.search.feign.CartFeignService;
import com.shiyi.gulimall.search.service.MallSearchService;
import com.shiyi.gulimall.search.vo.SearchParamVo;
import com.shiyi.gulimall.search.vo.SearchResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author:shiyi
 * @create: 2023-03-02  10:00
 */
@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @Autowired
    CartFeignService cartFeignService;

    @ResponseBody
    @GetMapping("/getCartNumber")
    public int getCartNumber(){
        int currentUserCartNumber = cartFeignService.getCurrentUserCartNumber();
        return currentUserCartNumber;
    }


    @GetMapping("/list.html")
    public String listPage(SearchParamVo searchParam, Model model, HttpServletRequest request){

        searchParam.set_queryString(request.getQueryString());
        SearchResultVo result = mallSearchService.search(searchParam);
        model.addAttribute("result",result);

        return "list";
    }
}
