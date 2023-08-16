package com.shiyi.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author:shiyi
 * @create: 2023-02-27  16:50
 */
@Data
public class PurchaseDoneVo {

    @NotNull
    private Long id; //采购单Id

    private List<PurchaseItemDoneVo> items;
}
