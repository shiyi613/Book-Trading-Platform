package com.shiyi.gulimall.order.vo;

import com.shiyi.common.vo.CouponTradeVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Author:shiyi
 * @create: 2023-03-08  20:32
 */
@Data
public class OrderConfirmVo {

    List<MemberAddressVo> address;

    List<OrderItemVo> items;

    //京豆(积分)
    Integer integration;

    //订单总额
    BigDecimal total;

    //实付金额
    BigDecimal payPrice;

    //是否有库存
    Map<Long,Boolean> stocks;

    //防重令牌
    String orderToken;

    //可用优惠券
    List<CouponTradeVo> couponTradeVo;


    public Integer getCount(){
        Integer count = 0;
        if(this.items != null && this.items.size() > 0){
            for (OrderItemVo item : this.items) {
                count += item.getCount();
            }
        }
        return count;
    }


    public BigDecimal getTotal() {
        BigDecimal total = new BigDecimal("0");
        if(this.items != null && this.items.size() > 0){
            for (OrderItemVo item : this.items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                total = total.add(multiply);
            }
        }
        return total;
    }

    public BigDecimal getPayPrice() {
        return getTotal();
    }


}
