package com.shiyi.gulimall.product.app;

import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.R;
import com.shiyi.common.valid.AddGroup;
import com.shiyi.common.valid.UpdateGroup;
import com.shiyi.common.valid.UpdateStatusGroup;
import com.shiyi.gulimall.product.entity.BrandEntity;
import com.shiyi.gulimall.product.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 品牌
 *
 * @author shiyi
 * @email sunlightcs@gmail.com
 * @date 2023-02-21 12:51:36
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping({"/list", "/list/api"})
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping({"/info/{brandId}", "/info/{brandId}/api"})
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }


    @GetMapping({"/infos", "/infos/api"})
    public R info(@RequestParam("brandIds") List<Long> brandIds){

        List<BrandEntity> list = brandService.getBrandsByIds(brandIds);
        return R.ok().put("brand",list);
    }

    /**
     * 保存
     */
    @RequestMapping({"/save", "/save/api"})
    public R save(@Validated(AddGroup.class) @RequestBody BrandEntity brand){

        brandService.save(brand);

        return R.ok();

    }

    /**
     * 修改
     */
    @RequestMapping({"/update", "/update/api"})
    public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand){
		brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 修改状态
     */
    @RequestMapping({"/update/status", "/update/status/api"})
    public R updateStatus(@Validated(UpdateStatusGroup.class) @RequestBody BrandEntity brand){
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping({"/delete", "/delete/api"})
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
