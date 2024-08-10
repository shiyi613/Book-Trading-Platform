package com.shiyi.gulimall.cart.service;

import com.shiyi.gulimall.cart.vo.CartItemVo;
import com.shiyi.gulimall.cart.vo.CartVo;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @Author:shiyi
 * @create: 2023-03-07  19:29
 */
public interface CartService {
    CartItemVo addToCart(Long skuId, Integer num);

    CartItemVo getCartItem(Long skuId);

    CartVo getCart() throws InterruptedException, ExecutionException, TimeoutException;

    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer checked);

    void countItem(Long skuId, Integer num);

    void deleteItem(Long skuId);

    List<CartItemVo> getCurrentUserCartItems();

    List<CartItemVo> getCartItems(String cartkey);

    int getCartNumber();
}
