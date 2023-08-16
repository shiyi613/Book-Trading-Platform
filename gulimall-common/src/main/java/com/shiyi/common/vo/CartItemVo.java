package com.shiyi.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author:shiyi
 * @create: 2023-03-07  19:14
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CartItemVo {

    private Long itemId;

    private Long spuId;

    private Long skuId;

    private Boolean check;

    private String title;

    private String image;

    // 商品套餐属性
    private List<String> skuAttrValues;

    private BigDecimal price;

    private Integer count;

    private BigDecimal totalPrice;


    /**
     * 计算当前购物项总价
     *
     * @return
     */
    public BigDecimal getTotalPrice() {
        return this.price.multiply(new BigDecimal("" + this.count));
    }

    @Override
    public String toString() {
        return "CartItemVo{" +
                "itemId=" + itemId +
                ", spuId=" + spuId +
                ", skuId=" + skuId +
                ", check=" + check +
                ", title='" + title + '\'' +
                ", image='" + image + '\'' +
                ", skuAttrValues=" + skuAttrValues +
                ", price=" + price +
                ", count=" + count +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
