package com.shiyi.gulimall.search.service;

import com.shiyi.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @Author:shiyi
 * @create: 2023-02-28  12:42
 */
public interface ProductUpService {

    boolean productUp(List<SkuEsModel> skuEsModelList) throws IOException;
}
