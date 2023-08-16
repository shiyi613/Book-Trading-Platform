package com.shiyi.gulimall.authserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.shiyi.common.utils.HttpUtils;
import com.shiyi.common.utils.R;
import com.shiyi.common.vo.MemberRespVo;
import com.shiyi.gulimall.authserver.feign.MemberFeignService;
import com.shiyi.gulimall.authserver.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author:shiyi
 * @create: 2023-03-06  23:57
 */
@Slf4j
@Controller
public class OAuth2Controller {

    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0/gitee/success")
    public String gitee(@RequestParam("code")String code, HttpSession session) throws Exception {

        //1、根据code换取access token

        Map<String, String> map = new HashMap<>();
        map.put("grant_type","authorization_code");
        map.put("code",code);
        map.put("client_id","67065126e6972b1bd00a0fe2d6d6ea99094c5c408785e8823e2215b25e15b925");
        map.put("redirect_uri","http://auth.gulimall.com/oauth2.0/gitee/success");
        map.put("client_secret","070672d03c0ad709da2bcaa94a567dcd376ba7610dc9429160617487db58c316");
        HttpResponse response = HttpUtils.doPost("https://gitee.com", "/oauth/token", "post",new HashMap<String,String>(),map,new HashMap<String,String>());

        if(response.getStatusLine().getStatusCode() == 200){
            //获取到了access token
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);

            //判断该用户是否注册过,若没注册过，自动注册进来
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put("access_token",socialUser.getAccess_token());
            HttpResponse response1 = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", new HashMap<String, String>(), queryMap);

            if(response1.getStatusLine().getStatusCode() == 200){
                String s = EntityUtils.toString(response1.getEntity());
                JSONObject jsonObject = JSON.parseObject(s);
                Integer id = jsonObject.getInteger("id");
                String name = jsonObject.getString("name");
                String avatarUrl = jsonObject.getString("avatar_url");
                socialUser.setUid(id);
                socialUser.setName(name);
                socialUser.setAvatarUrl(avatarUrl);
            }

            R r = memberFeignService.oauthLogin(socialUser);
            if(r.getCode() == 0){
                MemberRespVo data = r.getData("data", new TypeReference<MemberRespVo>() {});
                log.info("登录成功，用户信息：{}",data.toString());

                session.setAttribute("loginUser",data);
                return "redirect:http://gulimall.com";
            }else{
                return "redirect:http://auth.gulimall.com/login.html";
            }
        }else{
            return "redirect:http://auth.gulimall.com/login.html";
        }


    }
}
