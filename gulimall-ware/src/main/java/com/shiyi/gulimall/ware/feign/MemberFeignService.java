package com.shiyi.gulimall.ware.feign;

import com.shiyi.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author:shiyi
 * @create: 2023-03-09  10:58
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {


    @RequestMapping("/member/memberreceiveaddress/info/{id}")
    R getAddressInfo(@PathVariable("id") Long id);
}
