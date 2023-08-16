package com.shiyi.gulimall.cart.handler;

import com.shiyi.gulimall.cart.vo.CartItemVo;

/**
 * 检验参数是否合法
 */
public class ArgumentsHandler extends Handler{

    @Override
    public boolean check(CartItemVo cartItem) {
        if(this.next != null){
            return this.next.check(cartItem);
        }
        return true;
    }
}
