package com.shiyi.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.shiyi.common.utils.R;
import com.shiyi.gulimall.cart.exception.OutOfStockException;
import com.shiyi.gulimall.cart.feign.ProductFeignService;
import com.shiyi.gulimall.cart.handler.ArgumentsHandler;
import com.shiyi.gulimall.cart.handler.StockHandler;
import com.shiyi.gulimall.cart.interceptor.CartInterceptor;
import com.shiyi.gulimall.cart.service.CartService;
import com.shiyi.gulimall.cart.to.UserInfoTo;
import com.shiyi.gulimall.cart.vo.CartItemVo;
import com.shiyi.gulimall.cart.vo.CartVo;
import com.shiyi.gulimall.cart.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @Author:shiyi
 * @create: 2023-03-07  19:29
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    private static final String CART_PREFIX = "gulimall:cart:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor executor;



    @Override
    public CartItemVo addToCart(Long skuId, Integer num) throws OutOfStockException{

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        // 构造责任链
        StockHandler stockHandler = new StockHandler();
        ArgumentsHandler argumentsHandler = new ArgumentsHandler();
        argumentsHandler.setNext(stockHandler);
        CartItemVo cartItemForCheckStock = new CartItemVo();
        cartItemForCheckStock.setSkuId(skuId);
        cartItemForCheckStock.setCount(num);
        boolean isValid = argumentsHandler.start(cartItemForCheckStock);
        if(!isValid){
            throw new OutOfStockException("库存数量不足");
        }
        String cartItemJson = (String)cartOps.get(skuId.toString());
        if(StringUtils.isEmpty(cartItemJson)){     //新增商品到购物车
            CartItemVo cartItemVo = new CartItemVo();
            //远程查询sku商品信息
            CompletableFuture<Void> skuInfoFuture = CompletableFuture.runAsync(() -> {
                R skuInfo = productFeignService.getSkuInfo(skuId);
                if (skuInfo.getCode() == 0) {
                    SkuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                    });
                    //添加商品信息到购物车
                    cartItemVo.setItemId(skuId + 5L);
                    cartItemVo.setSpuId(data.getSpuId());
                    cartItemVo.setCheck(true);
                    cartItemVo.setCount(num);
                    cartItemVo.setImage(data.getSkuDefaultImg());
                    cartItemVo.setTitle(data.getSkuTitle());
                    cartItemVo.setSkuId(skuId);
                    cartItemVo.setPrice(data.getPrice());
                }
            }, executor);

            CompletableFuture<Void> skuSaleAttrFuture = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItemVo.setSkuAttrValues(skuSaleAttrValues);
            }, executor);

            try {
                CompletableFuture.allOf(skuInfoFuture,skuSaleAttrFuture).get();
                String json = JSON.toJSONString(cartItemVo);
                cartOps.put(skuId.toString(),json);
                return cartItemVo;
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        }else {
            //更新商品到购物车
            CartItemVo cartItemVo = JSON.parseObject(cartItemJson, CartItemVo.class);
            cartItemVo.setCount(cartItemVo.getCount() + num);
            cartOps.put(skuId.toString(),JSON.toJSONString(cartItemVo));
            return cartItemVo;
        }
    }

    @Override
    public CartItemVo getCartItem(Long skuId) {

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String o = (String)cartOps.get(skuId.toString());
        CartItemVo cartItemVo = JSON.parseObject(o, CartItemVo.class);
        return cartItemVo;
    }

    @Override
    public CartVo getCart(){
        CartVo cartVo = new CartVo();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.getUserId() != null){
            //已登录
            String cartkey = CART_PREFIX + userInfoTo.getUserId();
            String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();
            //合并临时购物车
            List<CartItemVo> TempCartItems = getCartItems(tempCartKey);
            if(TempCartItems != null && TempCartItems.size() > 0){
                for (CartItemVo tempCartItem : TempCartItems) {
                    addToCart(tempCartItem.getSkuId(),tempCartItem.getCount());
                }
                //合并完成，清空临时购物车
                clearCart(tempCartKey);
            }
            //获取用户购物车
            List<CartItemVo> cartItems = getCartItems(cartkey);
            if(cartItems != null && TempCartItems.size() > 0){
                cartItems = cartItems.stream().map( item -> {
                    R data = productFeignService.getPrice(item.getSkuId());
                    String price = (String) data.get("data");
                    //更新为最新价格
                    item.setPrice(new BigDecimal(price));
                    return item;
                }).collect(Collectors.toList());
            }
            cartVo.setItems(cartItems);
        }else{
            //未登录
            String cartkey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItemVo> cartItems = getCartItems(cartkey);
            cartVo.setItems(cartItems);
        }
        return cartVo;

    }

    /**
     * 获取当前用户购物车的商品项数量
     * @return
     */
    public int getCartNumber(){
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.getUserId() != null){
            //已登录
            String cartkey = CART_PREFIX + userInfoTo.getUserId();
            //获取用户购物车
            List<CartItemVo> cartItems = getCartItems(cartkey);
            return  cartItems.size();
        }else{
            //未登录
            String cartkey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItemVo> cartItems = getCartItems(cartkey);
            return cartItems.size();
        }
    }

    /**
     * 获取我们要操作的购物车
     * @return
     */
    public BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if(userInfoTo.getUserId() != null){
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        }else{
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }

        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        return operations;
    }

    /**
     * 清空购物车
     * @param cartKey
     */
    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    /**
     * 更新购物项的勾选状态
     * @param skuId
     * @param checked
     */
    @Override
    public void checkItem(Long skuId, Integer checked) {

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItemVo cartItem = getCartItem(skuId);
        cartItem.setCheck(checked == 1 ? true : false);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),s);
    }

    /**
     * 更新商品数量
     * @param skuId
     * @param num
     */
    @Override
    public void countItem(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItemVo cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),s);
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItemVo> getCurrentUserCartItems() {

        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.getUserId() == null){
            //未登录
            return null;
        }else{
            List<CartItemVo> cartItems = getCartItemsByChecked(CART_PREFIX + userInfoTo.getUserId());
            List<CartItemVo> collect = cartItems.stream().map( item -> {
                R data = productFeignService.getPrice(item.getSkuId());
                String price = (String) data.get("data");
                //更新为最新价格
                item.setPrice(new BigDecimal(price));
                return item;
            }).collect(Collectors.toList());
            return collect;
        }

    }

    /**
     * 获取指定key的购物车信息
     */
    public List<CartItemVo> getCartItems(String cartkey) {

        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartkey);
        List<Object> values = operations.values();
        if (values != null && values.size() > 0) {
            List<CartItemVo> collect = values.stream().map(item -> {
                String itemString = (String) item;
                CartItemVo cartItemVo = JSON.parseObject(itemString, CartItemVo.class);
                return cartItemVo;
            }).collect(Collectors.toList());
            return collect;
        }
        return new ArrayList<>();
    }

    /**
     * 获取指定key的购物车信息
     */
    public List<CartItemVo> getCartItemsByChecked(String cartkey) {

        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartkey);
        List<Object> values = operations.values();
        if (values != null && values.size() > 0) {
            List<CartItemVo> collect = values.stream().map(item -> {
                String itemString = (String) item;
                CartItemVo cartItemVo = JSON.parseObject(itemString, CartItemVo.class);
                return cartItemVo;
            }).filter(CartItemVo::getCheck).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

}
