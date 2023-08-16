package com.shiyi.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author:shiyi
 * @create: 2023-03-09  8:41
 */
@Configuration
public class MyFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor(){

        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                //使用RequestContextHolder拿到刚进来的请求,
                ServletRequestAttributes requestAttributes =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if(requestAttributes != null){
                    //拿到了老请求
                    HttpServletRequest request = requestAttributes.getRequest();

                    if(request != null){
                        //将老请求的信息拷贝到新请求里
                        String cookie = request.getHeader("Cookie");
                        template.header("Cookie",cookie);
                    }
                }
            }
        };
    }



}
