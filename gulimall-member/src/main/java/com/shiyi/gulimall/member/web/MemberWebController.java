package com.shiyi.gulimall.member.web;

import com.shiyi.common.utils.R;
import com.shiyi.common.vo.MemberRespVo;
import com.shiyi.gulimall.member.entity.MemberReceiveAddressEntity;
import com.shiyi.gulimall.member.feign.OrderFeignService;
import com.shiyi.gulimall.member.interceptor.LoginInteceptor;
import com.shiyi.gulimall.member.service.MemberReceiveAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author:shiyi
 * @create: 2023-03-10  15:00
 */
@Controller
public class MemberWebController {

    @Autowired
    private OrderFeignService orderFeignService;

    @Autowired
    private MemberReceiveAddressService memberReceiveAddressService;

    @GetMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum",defaultValue = "1") String pageNum,
                                  Model model){

        Map<String, Object> params = new HashMap<>();
        params.put("page",pageNum);
        R r = orderFeignService.listWithItem(params);
        model.addAttribute("orders",r);

        return "orderList";
    }

    @GetMapping("/memberInfo.html")
    public String memberInfoPage(Model model){

        return "myInfo";
    }

    @GetMapping("/memberImg.html")
    public String memberImgPage(Model model){

        return "touxiang";
    }

    @GetMapping("/memberAddress.html")
    public String getAddressList(Model model){
        MemberRespVo memberRespVo = LoginInteceptor.loginUser.get();
        List<MemberReceiveAddressEntity> list = memberReceiveAddressService.getAddress(memberRespVo.getId());
        model.addAttribute("addresses",list);
        return "address";
    }
}
