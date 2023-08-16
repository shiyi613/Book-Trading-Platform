package com.shiyi.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author:shiyi
 * @create: 2023-03-02  10:14
 */
@Data
public class SearchParamVo {


    private String keyword;      //全文匹配关键字
    private Long catalog3Id;     //三级分类ID

    /**
     * sort = saleCount_asc/desc
     * sort = skuPrice_asc/desc
     * sort = hotScore_asc/desc(综合排序)
     * 只能三选一
     */
    private String sort;       //排序条件
    private Integer hasStock;    //是否只显示有货
    private String skuPrice;     //价格区间,skuPrice = 1_500/500_/_500
    private List<Long> brandId;     //品牌Id,支持多选
    private List<String> attrs;      //按照属性进行筛选， 多选以":"分割，属性以"1_"标识
    private Integer pageNum = 1;        //页码,默认第一页

    private String _queryString;


}
