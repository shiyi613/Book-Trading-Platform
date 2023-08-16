package com.shiyi.common.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author:shiyi
 * @create: 2023-03-07  19:14
 */
public class CartVo {

    private List<CartItemVo> items;

    private Integer countNum;

    private Integer countTpye;

    private BigDecimal totalAmount;

    private BigDecimal reduce = new BigDecimal("0.00");

    public List<CartItemVo> getItems() {
        return items;
    }

    public void setItems(List<CartItemVo> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        if(this.items != null && this.items.size() > 0){
            for (CartItemVo item : this.items) {
                count += item.getCount();
            }
        }
        return count;
    }


    public Integer getCountTpye() {

        int count = 0;
        if(this.items != null && this.items.size() > 0){
            for (CartItemVo item : this.items) {
                count += 1;
            }
        }
        return count;
    }


    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        //1、计算购物项总价
        if(this.items != null && this.items.size() > 0){
            for (CartItemVo item : this.items) {
                if(item.getCheck()){
                    amount = amount.add(item.getTotalPrice());
                }
            }
        }

        //2、减去优惠总价
        amount = amount.subtract(this.getReduce());
        return amount;
    }


    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
