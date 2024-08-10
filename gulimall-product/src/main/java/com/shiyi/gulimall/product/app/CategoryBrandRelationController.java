package com.shiyi.gulimall.product.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.R;
import com.shiyi.gulimall.product.entity.BrandEntity;
import com.shiyi.gulimall.product.entity.CategoryBrandRelationEntity;
import com.shiyi.gulimall.product.service.CategoryBrandRelationService;
import com.shiyi.gulimall.product.vo.BrandVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



/**
 * 品牌分类关联
 *
 * @author shiyi
 * @email sunlightcs@gmail.com
 * @date 2023-02-21 12:51:36
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 获取当前品牌关联的所有分类列表
     */
    @GetMapping({"/catelog/list", "/catelog/list/api"})
    public R cateloglist(@RequestParam("brandId") Long brandId){

        QueryWrapper<CategoryBrandRelationEntity> wrapper = new QueryWrapper<>();
        List<CategoryBrandRelationEntity> data = categoryBrandRelationService.list(wrapper.eq("brand_id",brandId));

        return R.ok().put("data", data);
    }

    /**
     *
     */
    @GetMapping({"/brands/list", "/brands/list/api"})
    public R relationBrandsList(@RequestParam(value = "catId",required = true)Long catId){

        List<BrandEntity> vos = categoryBrandRelationService.getBrandsByCatId(catId);
        List<BrandVo> collect = vos.stream().map((item) -> {
            BrandVo brandVo = new BrandVo();
            brandVo.setBrandId(item.getBrandId());
            brandVo.setBrandName(item.getName());
            return brandVo;
        }).collect(Collectors.toList());

        return R.ok().put("data",collect);
    }



    /**
     * 列表
     */
    @RequestMapping({"/list", "/list/api"})
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping({"/info/{id}", "/info/{id}/api"})
    //@RequiresPermissions("product:categorybrandrelation:info")
    public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping({"/save", "/save/api"})
    //@RequiresPermissions("product:categorybrandrelation:save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.saveDetail(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping({"/update", "/update/api"})
    //@RequiresPermissions("product:categorybrandrelation:update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping({"/delete", "/delete/api"})
    //@RequiresPermissions("product:categorybrandrelation:delete")
    public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
