package com.shiyi.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.gulimall.product.entity.SpuImagesEntity;

import java.util.List;
import java.util.Map;

/**
 * spu图片
 *
 * @author shiyi
 * @email sunlightcs@gmail.com
 * @date 2023-02-21 12:25:11
 */
public interface SpuImagesService extends IService<SpuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);


    void saveSpuInfoImgs(Long id, List<String> images);
}

