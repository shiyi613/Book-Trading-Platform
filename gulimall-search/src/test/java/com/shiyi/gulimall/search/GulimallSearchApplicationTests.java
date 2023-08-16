package com.shiyi.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.shiyi.gulimall.search.config.ElasticsearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;


    @Test
    void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        User user = new User();
        user.setUsername("shiyi");
        user.setGender("男");
        user.setAge(18);
        String s = JSON.toJSONString(user);
        indexRequest.source(user, XContentType.JSON);

        client.index(indexRequest,ElasticsearchConfig.COMMON_OPTIONS);
    }

    /**
     * 检索地址中带有 mill 的人员年龄分布和平均薪资
     */
    @Test
    void searchData() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bank");
        //构造DSL
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //1.检索条件address包含mill
        builder.query(QueryBuilders.matchQuery("address","mill"));
        //2、按照年龄分布进行聚合
        builder.aggregation(AggregationBuilders.terms("ageAgg").field("age").size(10));
        //3、计算平均薪资
        builder.aggregation(AggregationBuilders.avg("balanceAvg").field("balance"));

        System.out.println("检索条件："+builder.toString());
        searchRequest.source(builder);

        //执行检索
        SearchResponse search = client.search(searchRequest, ElasticsearchConfig.COMMON_OPTIONS);

        //分析结果
        //1、记录信息
        SearchHits hits = search.getHits();
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit documentFields : hits1) {
            String sourceAsString = documentFields.getSourceAsString();
            System.out.println(sourceAsString);
        }

        //2、聚合信息
        Aggregations aggregations = search.getAggregations();
        Terms ageAgg = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println("年龄："+keyAsString+"    人数："+bucket.getDocCount());
        }

        Avg balanceAvg = aggregations.get("balanceAvg");
        System.out.println("平均薪资："+balanceAvg.getValue());
    }


    @Data
    class User{
        private String username;
        private String gender;
        private Integer age;
    }






    @Test
    void contextLoads() {

        System.out.println(client);
    }

}
