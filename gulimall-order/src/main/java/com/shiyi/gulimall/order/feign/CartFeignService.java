package com.shiyi.gulimall.order.feign;

import com.shiyi.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @Author:shiyi
 * @create: 2023-03-08  21:06
 */
@FeignClient("gulimall-cart")
public interface CartFeignService {

    @GetMapping("/currentUserCartItems")
    List<OrderItemVo> getCurrentUserCartItems();
}
