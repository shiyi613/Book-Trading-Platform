package com.shiyi.gulimall.member.feign;

import com.shiyi.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author:shiyi
 * @create: 2023-02-21  17:46
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @RequestMapping("/coupon/coupon/member/list")
    public R membercoupons();

    @RequestMapping("/coupon/coupon/receiveCoupon")
    public R receiveCoupon(@RequestParam("couponId") Long couponId);
}
