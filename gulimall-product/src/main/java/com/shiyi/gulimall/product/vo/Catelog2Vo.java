package com.shiyi.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author:shiyi
 * @create: 2023-02-28  15:49
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Catelog2Vo {

    private String catalog1Id;      //1级父分类id
    private List<Catelog3Vo> catalog3List;    //三级子分类
    private String id;
    private String name;


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Catelog3Vo{
        private String catalog2Id;     //2级父分类id
        private String id;
        private String name;
    }
}
