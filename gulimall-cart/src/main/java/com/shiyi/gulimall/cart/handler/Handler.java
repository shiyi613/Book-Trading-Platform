package com.shiyi.gulimall.cart.handler;

import com.shiyi.gulimall.cart.vo.CartItemVo;
import org.springframework.stereotype.Component;

/**
 * handler抽象类，用于加车前的各种校验
 */
public abstract class Handler {

    Handler next;

    abstract public boolean check(CartItemVo cartItem);

    public void setNext(Handler handler){
        this.next = handler;
    }

    public boolean start(CartItemVo cartItem){
        return this.check(cartItem);
    }
}
