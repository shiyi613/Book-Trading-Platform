package com.shiyi.gulimall.seckill.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author:shiyi
 * @create: 2023-03-12  22:18
 */
@Data
public class SeckillSessionsWithSkusVo {

    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private Date startTime;
    /**
     * 每日结束时间
     */
    private Date endTime;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 关联的所有商品
     */
    private List<SeckillSkuRelationVo> relationSkus;
}
