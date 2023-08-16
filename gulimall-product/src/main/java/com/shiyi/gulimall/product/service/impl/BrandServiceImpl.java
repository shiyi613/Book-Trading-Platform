package com.shiyi.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.Query;
import com.shiyi.gulimall.product.dao.BrandDao;
import com.shiyi.gulimall.product.entity.BrandEntity;
import com.shiyi.gulimall.product.service.BrandService;
import com.shiyi.gulimall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        String key = (String)params.get("key");
        QueryWrapper<BrandEntity> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(key)){
            wrapper.eq("brand_id",key).or().like("name",key);
        }

        IPage<BrandEntity> page = this.page(new Query<BrandEntity>().getPage(params),wrapper);

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void updateDetail(BrandEntity brand) {
        //保证冗余字段的数据一致性
        this.updateById(brand);
        if(!StringUtils.isEmpty(brand.getName())){
            //同步更新其他关联表的数据
            categoryBrandRelationService.updateBrand(brand.getBrandId(),brand.getName());
            //TODO 更新其他关联
        }
    }

    @Override
    public String getBrandImgById(Long brandId) {
        LambdaQueryWrapper<BrandEntity> wrapper = Wrappers.<BrandEntity>lambdaQuery().select(BrandEntity::getLogo).eq(BrandEntity::getBrandId, brandId);
        BrandEntity brandEntity = this.baseMapper.selectOne(wrapper);
        return brandEntity.getLogo();

    }

    @Cacheable(value = "brand",key = "#root.methodName")
    @Override
    public List<BrandEntity> getBrandsByIds(List<Long> brandIds) {

        return this.baseMapper.selectList(new QueryWrapper<BrandEntity>().in("brand_id",brandIds));
    }

}