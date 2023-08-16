package com.shiyi.gulimall.seckill.feign;

import com.shiyi.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Author:shiyi
 * @create: 2023-03-12  21:49
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @GetMapping("/coupon/seckillsession/latest3DaySession")
    R getLatest3DaySession();
}
