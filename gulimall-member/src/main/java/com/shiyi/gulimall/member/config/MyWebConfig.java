package com.shiyi.gulimall.member.config;

import com.shiyi.gulimall.member.interceptor.LoginInteceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author:shiyi
 * @create: 2023-03-10  15:03
 */
@Configuration
public class MyWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInteceptor()).addPathPatterns("/**")
                .excludePathPatterns("/member/member/regist")
                .excludePathPatterns("/member/member/login")
                .excludePathPatterns("/member/member/oauth2/login")
                .excludePathPatterns("/member/memberreceiveaddress/info/**");
    }
}
