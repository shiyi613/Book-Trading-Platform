package com.shiyi.gulimall.product.app;

import com.shiyi.common.utils.R;
import com.shiyi.gulimall.product.entity.CategoryEntity;
import com.shiyi.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 商品三级分类
 *
 * @author shiyi
 * @email sunlightcs@gmail.com
 * @date 2023-02-21 12:51:36
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 查出所有分类以及子分类，并以树型结构组装起来
     */
    @RequestMapping({"/list/tree", "/list/tree/api"})
    public R list(@RequestParam Map<String, Object> params){

        List<CategoryEntity> entities = categoryService.listWithTree();

        return R.ok().put("data", entities);
    }


    /**
     * 信息
     */
    @Cacheable(value = "catalog",key = "'catalogId:' + #root.args[0]")
    @RequestMapping({"/info/{catId}", "/info/{catId}/api"})
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("data", category);
    }

    /**
     * 保存
     */
    @RequestMapping({"/save", "/save/api"})
    //@RequiresPermissions("product:category:save")
    public R save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping({"/update/sort", "/update/sort/api"})
    public R updateSort(@RequestBody CategoryEntity[] category){

        categoryService.updateBatchById(Arrays.asList(category));

        return R.ok();
    }

    /**
     * 级联修改
     */
    @RequestMapping({"/update", "/update/api"})
    public R update(@RequestBody CategoryEntity category){

        categoryService.updateCascade(category);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping({"/delete", "/delete/api"})
    public R delete(@RequestBody Long[] catIds){

        categoryService.removeMenuByIds(Arrays.asList(catIds));

        return R.ok();
    }

}
