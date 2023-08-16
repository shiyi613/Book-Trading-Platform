package com.shiyi.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.shiyi.gulimall.product.dao.BrandDao;
import com.shiyi.gulimall.product.dao.CategoryDao;
import com.shiyi.gulimall.product.entity.BrandEntity;
import com.shiyi.gulimall.product.entity.CategoryEntity;
import com.shiyi.gulimall.product.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Wrapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.Query;

import com.shiyi.gulimall.product.dao.CategoryBrandRelationDao;
import com.shiyi.gulimall.product.entity.CategoryBrandRelationEntity;
import com.shiyi.gulimall.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    BrandDao brandDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationDao categoryBrandRelationDao;

    @Autowired
    BrandService brandService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();

        BrandEntity brandEntity = brandDao.selectById(brandId);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);

        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());

        this.save(categoryBrandRelation);
    }

    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity relationEntity = new CategoryBrandRelationEntity();
        relationEntity.setBrandId(brandId);
        relationEntity.setBrandName(name);

        this.update(relationEntity,
                new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id",brandId));
    }

    @Override
    public void updateCategory(Long catId, String name) {
//        写法一：
//        CategoryBrandRelationEntity relationEntity = new CategoryBrandRelationEntity();
//        relationEntity.setCatelogId(catId);
//        relationEntity.setCatelogName(name);
//
//        this.update(relationEntity,
//                new UpdateWrapper<CategoryBrandRelationEntity>().eq("catelog_id",catId));

        //写法二：
        this.baseMapper.updateCategory(catId,name);
    }

    @Override
    public List<BrandEntity> getBrandsByCatId(Long catId) {

        List<CategoryBrandRelationEntity> entityList = categoryBrandRelationDao.selectList(
                new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));

        List<BrandEntity> collect = entityList.stream().map((item) -> {
            BrandEntity byId = brandService.getById(item.getBrandId());
            return byId;
        }).collect(Collectors.toList());

        return collect;
    }

}