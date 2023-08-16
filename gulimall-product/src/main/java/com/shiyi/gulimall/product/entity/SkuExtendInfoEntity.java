package com.shiyi.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pms_sku_extend_info")
public class SkuExtendInfoEntity {

    @TableId(value = "id",type = IdType.INPUT)
    private Long id;

    @TableField("brand_img")
    private String brandImg;

    @TableField("attrs")
    private String attrs;

    @TableField("hasStock")
    private byte hasStock;

    @TableField("hotScore")
    private Long hotScore;

}
