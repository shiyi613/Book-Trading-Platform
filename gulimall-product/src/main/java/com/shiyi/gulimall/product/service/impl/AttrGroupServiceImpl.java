package com.shiyi.gulimall.product.service.impl;

import com.shiyi.gulimall.product.entity.AttrEntity;
import com.shiyi.gulimall.product.service.AttrService;
import com.shiyi.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.shiyi.gulimall.product.vo.SkuItemVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.Query;

import com.shiyi.gulimall.product.dao.AttrGroupDao;
import com.shiyi.gulimall.product.entity.AttrGroupEntity;
import com.shiyi.gulimall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {

        String key = (String)params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();

        if(!StringUtils.isEmpty(key)){
            wrapper.and((obj)->{
                obj.eq("attr_group_id",key).or().like("attr_group_name",key);
            });
        }

        if(catelogId == 0){
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),wrapper);
            return new PageUtils(page);
        }else{
            wrapper.eq("catelog_Id", catelogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),wrapper);
            return new PageUtils(page);
        }
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        //1、查出当前分类的所有分组信息
        List<AttrGroupEntity> attrGroupEntityList = this.baseMapper.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        //2、查出所有分组的所有属性
        List<AttrGroupWithAttrsVo> collect = attrGroupEntityList.stream().map((item) -> {
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(item, attrGroupWithAttrsVo);
            //设置某个分组下的所有属性
            List<AttrEntity> relationAttrs = attrService.getRelationAttr(item.getAttrGroupId());
            attrGroupWithAttrsVo.setAttrs(relationAttrs);

            return attrGroupWithAttrsVo;
        }).collect(Collectors.toList());

        return collect;
    }

    @Override
    public List<SkuItemVo.SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {

        return this.baseMapper.getAttrGroupWithAttrsBySpuId(spuId,catalogId);
    }

}