package com.shiyi.gulimall.cart.handler;

import com.shiyi.gulimall.cart.vo.CartItemVo;

/**
 * handler抽象类，用于加车前的各种校验
 */
public abstract class Handler {

    Handler next;
    boolean skipped;

    abstract public boolean check(CartItemVo cartItem);

    public Handler(){
        this(null, false);
    }

    public Handler(boolean skipped){
        this(null, skipped);
    }

    public Handler(Handler handler, boolean skipped){
        setNext(handler);
        this.skipped = skipped;
    }


    public void setNext(Handler handler){
        this.next = handler;
    }


    public boolean start(CartItemVo cartItem){
        return this.check(cartItem);
    }
}
