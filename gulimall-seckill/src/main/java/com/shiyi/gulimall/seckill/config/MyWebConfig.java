package com.shiyi.gulimall.seckill.config;

import com.shiyi.gulimall.seckill.interceptor.LoginInteceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author:shiyi
 * @create: 2023-03-13  18:11
 */
@Configuration
public class MyWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInteceptor()).addPathPatterns("/**");
    }
}
