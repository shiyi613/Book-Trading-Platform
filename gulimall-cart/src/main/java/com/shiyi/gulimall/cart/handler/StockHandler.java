package com.shiyi.gulimall.cart.handler;

import com.shiyi.gulimall.cart.feign.WareFeignService;
import com.shiyi.gulimall.cart.utils.SpringUtil;
import com.shiyi.gulimall.cart.vo.CartItemVo;
import org.springframework.stereotype.Component;

/**
 * 检验是否库存充足
 */
@Component
public class StockHandler extends Handler{


    @Override
    public boolean check(CartItemVo cartItem) {
        if(this.skipped){
            if(this.next != null){
                return this.next.check(cartItem);
            }
        }else{
            WareFeignService wareFeignService = SpringUtil.getBean(WareFeignService.class);
            Boolean hasStock = wareFeignService.getSkuHasStock(cartItem.getSkuId(),cartItem.getCount());
            if(hasStock != null && hasStock && this.next != null){
                return this.next.check(cartItem);
            }
            return hasStock;
        }
        return true;
    }
}
