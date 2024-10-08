package com.shiyi.gulimall.ware.feign;

import com.shiyi.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Author:shiyi
 * @create: 2023-03-10  8:50
 */
@FeignClient("gulimall-order")
public interface OrderFeignService {

    @GetMapping("/order/order/status/{orderSn}")
    R getOrderStatus(@PathVariable("orderSn")String orderSn);

    @PostMapping("/order/updateMsgStatus")
    R updateMsgStatusByMsgId(@RequestBody String messageId);
}
