package com.shiyi.gulimall.ware.exception;

/**
 * @Author:shiyi
 * @create: 2023-03-09  16:20
 */
public class NoStockException extends RuntimeException{

    private Long skuId;

    public NoStockException(Long skuId){
        super("商品id：" + skuId + "--> 没有库存了！");
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
