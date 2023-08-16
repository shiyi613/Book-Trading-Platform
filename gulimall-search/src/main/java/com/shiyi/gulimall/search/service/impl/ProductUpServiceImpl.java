package com.shiyi.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.shiyi.common.to.es.SkuEsModel;
import com.shiyi.gulimall.search.config.ElasticsearchConfig;
import com.shiyi.gulimall.search.constant.EsConstant;
import com.shiyi.gulimall.search.service.ProductUpService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author:shiyi
 * @create: 2023-02-28  12:44
 */
@Slf4j
@Service
public class ProductUpServiceImpl implements ProductUpService {

    @Autowired
    RestHighLevelClient client;

    @Override
    public boolean productUp(List<SkuEsModel> skuEsModelList) throws IOException {

        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModelList) {
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(skuEsModel.getSkuId().toString());
            String s = JSON.toJSONString(skuEsModel);
            indexRequest.source(s, XContentType.JSON);

            bulkRequest.add(indexRequest);
        }

        BulkResponse bulk = client.bulk(bulkRequest, ElasticsearchConfig.COMMON_OPTIONS);

        boolean b = bulk.hasFailures();
        List<String> collect = Arrays.stream(bulk.getItems()).map(item -> {
            return item.getId();
        }).collect(Collectors.toList());
        log.info("商品上架完成：{},返回数据：{}",collect,bulk.toString());

        //true代表有错误
        return b;
    }
}
