package com.shiyi.gulimall.cart.exception;

public class OutOfStockException extends RuntimeException{

    public OutOfStockException(String s) {
        super(s);
    }
}
