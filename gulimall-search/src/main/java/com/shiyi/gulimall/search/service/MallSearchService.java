package com.shiyi.gulimall.search.service;

import com.shiyi.gulimall.search.vo.SearchParamVo;
import com.shiyi.gulimall.search.vo.SearchResultVo;

/**
 * @Author:shiyi
 * @create: 2023-03-02  10:16
 */
public interface MallSearchService {

    SearchResultVo search(SearchParamVo searchParam);
}
