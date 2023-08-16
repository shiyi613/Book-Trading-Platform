package com.shiyi.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author:shiyi
 * @create: 2023-02-26  22:25
 */
@Data
public class MergeVo {

    private Long purchaseId;       //采购单Id
    private List<Long> items;       //合并项id集合
}
