package com.shiyi.gulimall.ware.vo;

import lombok.Data;

/**
 * @Author:shiyi
 * @create: 2023-02-27  16:50
 */
@Data
public class PurchaseItemDoneVo {

    private Long itemId;

    private Integer status;

    private String reason;
}
