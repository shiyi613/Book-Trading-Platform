package com.shiyi.gulimall.order.vo;

import com.shiyi.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @Author:shiyi
 * @create: 2023-03-09  13:36
 */
@Data
public class SubmitOrderResponseVo {

    private OrderEntity order;

    private Integer code;  //0代表成功
}
