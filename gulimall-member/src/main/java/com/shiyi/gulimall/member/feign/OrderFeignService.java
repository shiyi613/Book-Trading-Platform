package com.shiyi.gulimall.member.feign;

import com.shiyi.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @Author:shiyi
 * @create: 2023-03-10  16:14
 */
@FeignClient("gulimall-order")
public interface OrderFeignService {

    @PostMapping("/order/order/listWithItems")
    R listWithItem(@RequestBody Map<String, Object> params);
}
