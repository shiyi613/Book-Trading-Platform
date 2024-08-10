package com.shiyi.gulimall.authserver.controller;

import com.alibaba.fastjson.TypeReference;
import com.shiyi.common.constant.AuthServerConstant;
import com.shiyi.common.exception.BizCodeEnum;
import com.shiyi.common.utils.R;
import com.shiyi.common.vo.MemberRespVo;
import com.shiyi.gulimall.authserver.feign.MemberFeignService;
import com.shiyi.gulimall.authserver.feign.ThirdPartyFeignService;
import com.shiyi.gulimall.authserver.vo.UserLoginVo;
import com.shiyi.gulimall.authserver.vo.UserRegistVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author:shiyi
 * @create: 2023-03-06  14:54
 */
@Controller
public class LoginController implements HandlerInterceptor {

    @Autowired
    ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    /**
     * 使用Spring MVC配置类配置地址映射，避免写空方法
     */
    @GetMapping("/login.html")
    public String loginPage(HttpSession session){

        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute == null){
            return "login";
        }else{
            return "redirect:http://gulimall.com";
        }
    }
//
//    @GetMapping("/reg.html")
//    public String regPage(){
//
//        return "reg";
//    }

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone")String phone){

        //接口防刷

        //避免同一手机号60s内重复获取验证码
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if(!StringUtils.isEmpty(redisCode)){
            long time = Long.parseLong(redisCode.split("_")[1]);
            if(System.currentTimeMillis() - time < 60000){
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(),BizCodeEnum.SMS_CODE_EXCEPTION.getMessage());
            }
        }

        //验证码的再次校验 redis  key-Phone ，value-code       sms:code:手机号  -》 验证码
        String substring = UUID.randomUUID().toString().substring(0, 5);
        String code = substring + "_" + System.currentTimeMillis() ;
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,code,5, TimeUnit.MINUTES);

        thirdPartyFeignService.sendCode(phone,substring);
        return R.ok();
    }


    /**
     * RedirectAttributes: session原理，将数据放在session中，新页面再从session拿，问题是分布式session问题
     * @param vo
     * @param result
     * @param attributes
     * @return
     */
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes attributes){
        //参数校验
        if(result.hasErrors()){
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            //路径映射只有get才能访问，重定向不能直接重定向到resources下，Spring MVC支持重定向携带数据
            attributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        //校验验证码
        String code = vo.getCode();
        //从redis获取验证码
        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if(!StringUtils.isEmpty(s)){
            if(code.equalsIgnoreCase(s.split("_")[0])){
                //验证码通过，用过后需要删除该验证码
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                //调用远程服务注册
                R r = memberFeignService.regist(vo);
                if(r.getCode() == 0){
                    return "redirect:http://auth.gulimall.com/login.html";
                }else{
                    Map<String,String> errors = new HashMap<>();
                    errors.put("errors",r.getData("msg",new TypeReference<String>(){}));
                    attributes.addFlashAttribute("errors",errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }
            }else{
                //验证码过期，或该验证码不存在
                Map<String, String> errors = new HashMap<>();
                errors.put("code","验证码错误");
                attributes.addFlashAttribute("errors",errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        }else{
            //验证码过期，或该验证码不存在
            Map<String, String> errors = new HashMap<>();
            errors.put("code","验证码错误");
            attributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes attributes, HttpSession session){

        R r = memberFeignService.login(vo);
        if(r.getCode() == 0){

            session.setAttribute(AuthServerConstant.LOGIN_USER,r.getData(("data"),new TypeReference<MemberRespVo>(){}));
            return "redirect:http://gulimall.com";
        }else{
            Map<String, String> map = new HashMap<>();
            map.put("msg",r.getData("msg",new TypeReference<String>(){}));
            attributes.addFlashAttribute("errors",map);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

    @RequestMapping("/logout")
    public String logout(HttpSession session){
        session.removeAttribute(AuthServerConstant.LOGIN_USER);
        return "redirect:http://gulimall.com";
    }


}
