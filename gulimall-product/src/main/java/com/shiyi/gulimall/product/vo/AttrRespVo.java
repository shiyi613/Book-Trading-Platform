package com.shiyi.gulimall.product.vo;

import lombok.Data;

/**
 * @Author:shiyi
 * @create: 2023-02-23  17:08
 */
@Data
public class AttrRespVo extends AttrVo{

    /**
     * 所属分类名称
     */
    private String catelogName;

    /**
     * 所属分组名称
     */
    private String groupName;

    private Long[] catelogPath;

}
