package com.shiyi.common.to.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author:shiyi
 * @create: 2023-02-28  10:45
 */
@Data
public class SkuEsModel {

    private Long skuId;
    private Long spuId;
    private String skuTitle;
    private BigDecimal skuPrice;
    private String skuImg;
    private Long saleCount;
    /**
     * 是否有库存
     */
    private Boolean hasStock;
    /**
     * 热度
     */
    private Long hotScore;
    private Long brandId;
    private Long catalogId;
    private String brandName;
    private String brandImg;
    private String catalogName;
    private List<Attrs> attrs;
    @Data
    public static class Attrs {
        private Long attrId;
        private String attrName;
        private String attrValue;
    }
}
