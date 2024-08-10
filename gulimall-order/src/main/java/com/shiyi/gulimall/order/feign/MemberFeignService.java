package com.shiyi.gulimall.order.feign;

import com.shiyi.common.utils.R;
import com.shiyi.common.vo.MemberCouponVo;
import com.shiyi.gulimall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Author:shiyi
 * @create: 2023-03-08  20:43
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @GetMapping("/member/memberreceiveaddress/{memberId}/addresses")
    List<MemberAddressVo> getAddress(@PathVariable("memberId") Long memberId);

    @RequestMapping("/member/member/getCurrentUserCoupons")
    List<MemberCouponVo> getCurrentUserCoupons();

    @RequestMapping("/member/member/deleteCouponNum")
    R deleteCouponNum(@RequestParam("couponId") Long couponId);
}
