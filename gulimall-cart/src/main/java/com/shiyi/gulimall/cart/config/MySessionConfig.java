package com.shiyi.gulimall.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web  .http.DefaultCookieSerializer;

/**
 * @Author:shiyi
 * @create: 2023-03-07  11:48
 */
@Configuration
public class MySessionConfig {

    /**
     * 解决子域session共享问题
     * @return
     */
    @Bean
    public CookieSerializer cookieSerializer(){

        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setDomainName("gulimall.com");
        cookieSerializer.setCookieName("GULIMALLSESSION");

        return cookieSerializer;
    }

    /**
     * 解决session保存到redis的序列化机制
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer(){
        return new GenericJackson2JsonRedisSerializer();
    }


}
