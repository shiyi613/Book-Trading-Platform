package com.shiyi.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.gulimall.product.entity.BrandEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌
 *
 * @author shiyi
 * @email sunlightcs@gmail.com
 * @date 2023-02-21 12:25:11
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void updateDetail(BrandEntity brand);

    String getBrandImgById(Long bandId);

    List<BrandEntity> getBrandsByIds(List<Long> brandIds);
}

