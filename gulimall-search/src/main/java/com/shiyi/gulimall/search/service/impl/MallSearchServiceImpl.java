package com.shiyi.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.shiyi.common.to.es.SkuEsModel;
import com.shiyi.common.utils.R;
import com.shiyi.gulimall.search.config.ElasticsearchConfig;
import com.shiyi.gulimall.search.constant.EsConstant;
import com.shiyi.gulimall.search.feign.ProductFeignService;
import com.shiyi.gulimall.search.service.MallSearchService;
import com.shiyi.gulimall.search.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author:shiyi
 * @create: 2023-03-02  10:16
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public SearchResultVo search(SearchParamVo searchParam) {

        SearchResultVo result = null;

        //1、准备检索请求DSL
        SearchRequest searchRequest = bulidSearchRequest(searchParam);


        try {
            //2、执行检索请求
            SearchResponse search = client.search(searchRequest, ElasticsearchConfig.COMMON_OPTIONS);

            //3、分析响应数据封装成我们需要的格式
            result = bulidSearchResult(search,searchParam);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private SearchResultVo bulidSearchResult(SearchResponse search,SearchParamVo searchParam) {

        SearchResultVo resultVo = new SearchResultVo();
        SearchHits hits = search.getHits();
        //1、返回所有查询到的商品信息
        List<SkuEsModel> productList = new ArrayList<SkuEsModel>();
        if( null != hits.getHits() && hits.getHits().length > 0){
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                //高亮
                if(!StringUtils.isEmpty(searchParam.getKeyword())){
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    skuEsModel.setSkuTitle(string);
                }
                productList.add(skuEsModel);
            }
        }
        resultVo.setProducts(productList);

        //2、当前所有商品涉及到的所有属性信息
        List<SearchResultVo.AttrVo> attrList = new ArrayList<>();
        ParsedNested attrAgg = search.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrId_agg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResultVo.AttrVo attrVo = new SearchResultVo.AttrVo();
            //设置属性ID
            attrVo.setAttrId(bucket.getKeyAsNumber().longValue());
            //设置属性名字
            attrVo.setAttrName(((ParsedStringTerms)bucket.getAggregations().get("attrName_agg")).getBuckets().get(0).getKeyAsString());
            //设置属性值(可能有多个)
            List<String> attrValueAgg = ((ParsedStringTerms) bucket.getAggregations().get("attrValue_agg")).getBuckets().stream().map(item -> {
                return item.getKeyAsString();
            }).collect(Collectors.toList());
            attrVo.setAttrValue(attrValueAgg);

            attrList.add(attrVo);
        }
        resultVo.setAttrs(attrList);

        //3、当前所有商品涉及到的所有品牌信息
        List<SearchResultVo.BrandVo> brandList = new ArrayList<>();
        ParsedLongTerms brandAgg = search.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            SearchResultVo.BrandVo brandVo = new SearchResultVo.BrandVo();
            // 设置品牌ID
            brandVo.setBrandId(bucket.getKeyAsNumber().longValue());
            //设置品牌图片
            ParsedStringTerms brandImgAgg = bucket.getAggregations().get("brandImg_agg");
            brandVo.setBrandImg(brandImgAgg.getBuckets().get(0).getKeyAsString());
            //设置品牌名字
            ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brandName_agg");
            brandVo.setBrandName(brandNameAgg.getBuckets().get(0).getKeyAsString());

            brandList.add(brandVo);
        }
        resultVo.setBrands(brandList);

        //4、当前所有商品涉及到的所有分类信息
        List<SearchResultVo.CatalogVo> catalogList = new ArrayList<>();
        ParsedLongTerms catalogAgg = search.getAggregations().get("catalog_agg");
        List<? extends Terms.Bucket> buckets = catalogAgg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResultVo.CatalogVo catalogVo = new SearchResultVo.CatalogVo();
            //设置分类ID
            catalogVo.setCatalogId(Long.parseLong(bucket.getKeyAsString()));
            //设置分类名字
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalogName_agg");
            catalogVo.setCatalogName(catalogNameAgg.getBuckets().get(0).getKeyAsString());
            catalogList.add(catalogVo);
        }
        resultVo.setCatalogs(catalogList);

        //5、分页信息
        long total = hits.getTotalHits().value;     //总记录数
        int totalPages = (int)total % EsConstant.PRODUCT_PAGESIZE == 0 ?     //总页码，11/2 = 5...1 = 6
                (int)total / EsConstant.PRODUCT_PAGESIZE : ((int)total / EsConstant.PRODUCT_PAGESIZE)+ 1;
        resultVo.setPageNum(searchParam.getPageNum());
        resultVo.setTotal(total);
        resultVo.setTotalPages(totalPages);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        resultVo.setPageNavs(pageNavs);


        //6、面包屑导航
        if(searchParam.getAttrs() != null && searchParam.getAttrs().size() > 0){
            List<SearchResultVo.NavVo> collect = searchParam.getAttrs().stream().map(item -> {
                SearchResultVo.NavVo navVo = new SearchResultVo.NavVo();
                String[] s = item.split("_");
                navVo.setNavValue(s[1]);
                R info = productFeignService.getAttrinfo(Long.parseLong(s[0]));
                //记录筛选了的属性ID，以便前端消失
                resultVo.getAttrIds().add(Long.parseLong(s[0]));
                if (info.getCode() == 0) {
                    AttrResponseVo attr = info.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(attr.getAttrName());
                }else{
                    navVo.setNavName(s[0]);
                }

                String uri = replaceQueryString(searchParam, "attrs",item);
                if(StringUtils.isEmpty(uri)){
                    navVo.setLink("http://search.gulimall.com/list.html");
                }else{
                    navVo.setLink("http://search.gulimall.com/list.html?" + uri);
                }

                return navVo;
            }).collect(Collectors.toList());
            resultVo.setNavs(collect);
        }

        //品牌上面包屑
        if(searchParam.getBrandId() != null && searchParam.getBrandId().size() > 0){
            List<SearchResultVo.NavVo> navs = resultVo.getNavs();
            SearchResultVo.NavVo navVo = new SearchResultVo.NavVo();

            navVo.setNavName("品牌");
            //远程查询
            R brandsInfo = productFeignService.getBrandsinfo(searchParam.getBrandId());
            if(brandsInfo.getCode() == 0){
                List<BrandVo> brandVoList = brandsInfo.getData("brand", new TypeReference<List<BrandVo>>() {});
                StringBuffer stringBuffer = new StringBuffer();
                String uri = "";
                for (BrandVo brandVo : brandVoList) {
                    stringBuffer.append(brandVo.getName()+";");
                    uri = replaceQueryString(searchParam, "brandId",brandVo.getBrandId()+"");
                }
                navVo.setNavValue(stringBuffer.toString());
                if(StringUtils.isEmpty(uri)){
                    navVo.setLink("http://search.gulimall.com/list.html");
                }else{
                    navVo.setLink("http://search.gulimall.com/list.html?" + uri);
                }
            }
            navs.add(navVo);
        }

        //分类上面包屑
        if(searchParam.getCatalog3Id() != null){
            List<SearchResultVo.NavVo> navs = resultVo.getNavs();
            SearchResultVo.NavVo navVo = new SearchResultVo.NavVo();

            navVo.setNavName("分类");
            R cataloginfo = productFeignService.getCataloginfo(searchParam.getCatalog3Id());
            if(cataloginfo.getCode() == 0){
                Catalog3Vo data = cataloginfo.getData("data", new TypeReference<Catalog3Vo>() {});
                navVo.setNavValue(data.getName());
                String uri = replaceQueryString(searchParam, "catalog3Id", data.getCatId() + "");
                if(StringUtils.isEmpty(uri)){
                    navVo.setLink("http://search.gulimall.com/list.html");
                }else{
                    navVo.setLink("http://search.gulimall.com/list.html?" + uri);
                }
            }
            navs.add(navVo);
        }

        return resultVo;
    }

    private String replaceQueryString(SearchParamVo searchParam, String key, String value) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value,"UTF-8");
            encode = encode.replace("+","%20"); //浏览器对空格编码和java不一样
            encode = encode.replace("%3B",";");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String uri = null;
        if(searchParam.get_queryString().contains("&")){
            if(searchParam.get_queryString().startsWith(key)){
                uri = searchParam.get_queryString().replace(key + "=" + encode , "");
            }else{
                uri = searchParam.get_queryString().replace("&" + key + "=" + encode, "");
            }
        }else{
            uri = searchParam.get_queryString().replace( key + "=" + encode, "");
        }
        return uri;
    }

    private SearchRequest bulidSearchRequest(SearchParamVo searchParam) {

        SearchSourceBuilder builder = new SearchSourceBuilder();

        /**
         * 查询：模糊匹配，过滤（属性、分类、品牌、价格区间、库存）
         */
        //1、构建bool - query
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //1.1、must-模糊匹配
        if(!StringUtils.isEmpty(searchParam.getKeyword())){
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle",searchParam.getKeyword()));
        }
        //1.2、filter-分类
        if( null != searchParam.getCatalog3Id()){
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId",searchParam.getCatalog3Id()));
        }
        //1.3、filter-品牌
        if( null != searchParam.getBrandId() && searchParam.getBrandId().size() > 0){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId",searchParam.getBrandId()));
        }
        //1.4、filter-属性
        if( null != searchParam.getAttrs() && searchParam.getAttrs().size() > 0){

            //&1_5寸:8寸&2_骁龙:A14
            for (String attr : searchParam.getAttrs()) {

                String[] s = attr.split("_");
                String attrId = s[0];       //属性ID
                String[] attrValues = s[1].split(":");     //属性值

                BoolQueryBuilder boolQuery = new BoolQueryBuilder();
                boolQuery.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                boolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));
                //每一个attr生成一个nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", boolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQuery);
            }

        }

        //1.5、filter-库存
        if(searchParam.getHasStock() != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock",searchParam.getHasStock()));
        }

        //1.6、filter-价格区间    格式1_500/500_/_500
        if(!StringUtils.isEmpty(searchParam.getSkuPrice())){

            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");
            String[] s = searchParam.getSkuPrice().split("_");
            if(s.length == 2){
                //1_500
                rangeQueryBuilder.gte(s[0]).lte(s[1]);
            }else if(s.length == 1){
                if(searchParam.getSkuPrice().startsWith("_")){
                    //_500
                    rangeQueryBuilder.lte(s[0]);
                }else if(searchParam.getSkuPrice().endsWith("_")){
                    //500_
                    rangeQueryBuilder.gte(s[0]);
                }
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }

        builder.query(boolQueryBuilder);


        /**
         * 排序、分页、高亮
         */
        //2.1、排序
        if(!StringUtils.isEmpty(searchParam.getSort())){
            //saleCount_asc/desc
            String[] s = searchParam.getSort().split("_");
            SortOrder sortOrder = "asc".equalsIgnoreCase(s[1]) ? SortOrder.ASC : SortOrder.DESC;
            builder.sort(s[0],sortOrder);
        }
        //2.2、分页   pageSize = 5
        /**
         *  pageNum = 1  from 0  [0,1,2,3,4]
         *  pageNum = 2  from 5  [5,6,7,8,9]
         *  ......
         */
        builder.from((searchParam.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        builder.size(EsConstant.PRODUCT_PAGESIZE);
        //2.3 高亮(有模糊匹配的情况下）
        if(!StringUtils.isEmpty(searchParam.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");

            builder.highlighter(highlightBuilder);
        }

        /**
         * 聚合分析
         */
        //3.1、品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        brandAgg.subAggregation(AggregationBuilders.terms("brandName_agg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brandImg_agg").field("brandImg").size(1));
        builder.aggregation(brandAgg);

        //3.2、分类聚合
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalogName_agg").field("catalogName").size(1));
        builder.aggregation(catalogAgg);

        //3.3、属性聚合
        NestedAggregationBuilder nestedAggBuilder = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attrId_agg").field("attrs.attrId").size(50);
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrName_agg").field("attrs.attrName").size(50));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrValue_agg").field("attrs.attrValue").size(50));
        nestedAggBuilder.subAggregation(attrIdAgg);
        builder.aggregation(nestedAggBuilder);

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, builder);

        return searchRequest;
    }
}
