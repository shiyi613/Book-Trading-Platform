package com.shiyi.gulimall.authserver.feign;

import com.shiyi.common.utils.R;
import com.shiyi.gulimall.authserver.vo.SocialUser;
import com.shiyi.gulimall.authserver.vo.UserLoginVo;
import com.shiyi.gulimall.authserver.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Author:shiyi
 * @create: 2023-03-06  19:41
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo vo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    R oauthLogin(@RequestBody SocialUser socialUser);
}
