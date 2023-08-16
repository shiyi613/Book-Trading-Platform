package com.shiyi.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.gulimall.product.entity.AttrEntity;
import com.shiyi.gulimall.product.vo.AttrGroupReleationVo;
import com.shiyi.gulimall.product.vo.AttrRespVo;
import com.shiyi.gulimall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author shiyi
 * @email sunlightcs@gmail.com
 * @date 2023-02-21 12:25:11
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId,String attrType);

    AttrRespVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelationAttr(Long attrgroupId);

    void deleteRelation(AttrGroupReleationVo[] vos);

    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId);

    List<Long> selectSearchAttrs(List<Long> attrIds);
}

