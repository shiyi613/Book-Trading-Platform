package com.shiyi.gulimall.thirdparty.controller;

import com.shiyi.common.utils.R;
import com.shiyi.gulimall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author:shiyi
 * @create: 2023-03-06  16:28
 */
@RestController
@RequestMapping("/sms")
public class SmsSendController {

    @Autowired
    SmsComponent smsComponent;

    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code){

        smsComponent.sendSmsCode(phone,code);
        return R.ok();
    }
}
