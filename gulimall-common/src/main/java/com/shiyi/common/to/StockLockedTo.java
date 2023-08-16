package com.shiyi.common.to;

import lombok.Data;

import java.util.List;

/**
 * @Author:shiyi
 * @create: 2023-03-09  23:20
 */
@Data
public class StockLockedTo {

    // 订单号
    private String orderSn;
    // 库存工作单id
    private Long id;
    // 该库存工作单下的所有工作单项详情
    private List<StockDetailTo> details;

}
