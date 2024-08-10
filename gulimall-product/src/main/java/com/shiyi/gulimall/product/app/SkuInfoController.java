package com.shiyi.gulimall.product.app;

import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.R;
import com.shiyi.gulimall.product.entity.SkuInfoEntity;
import com.shiyi.gulimall.product.service.SkuInfoService;
import com.shiyi.gulimall.product.vo.SkuInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * sku信息
 *
 * @author shiyi
 * @email sunlightcs@gmail.com
 * @date 2023-02-21 12:51:36
 */
@RestController
@RequestMapping("product/skuinfo")
public class SkuInfoController {
    @Autowired
    private SkuInfoService skuInfoService;

    @PostMapping("/saleCountAdd")
    public R saleCountBatchAdd(@RequestBody List<SkuInfoVo> skuIds){
        List<SkuInfoEntity> collect = skuIds.stream().map(item -> {
            SkuInfoEntity oldEntity = skuInfoService.getById(item.getSkuId());
            oldEntity.setSaleCount(oldEntity.getSaleCount() + item.getSaleCount());
            return oldEntity;
        }).collect(Collectors.toList());
        skuInfoService.updateBatchById(collect);
        return R.ok();
    }

    @GetMapping({"/{skuId}/price", "/{skuId}/price/api"})
    public R getPrice(@PathVariable("skuId") Long skuId){
        SkuInfoEntity entity = skuInfoService.getById(skuId);
        return R.ok().setData(entity.getPrice().toString());
    }


    /**
     * 列表
     */
    @RequestMapping({"/list", "/list/api"})
    //@RequiresPermissions("product:skuinfo:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = skuInfoService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping({"/info/{skuId}", "/info/{skuId}/api"})
    //@RequiresPermissions("product:skuinfo:info")
    public R info(@PathVariable("skuId") Long skuId){
		SkuInfoEntity skuInfo = skuInfoService.getById(skuId);

        return R.ok().put("skuInfo", skuInfo);
    }

    /**
     * 保存
     */
    @RequestMapping({"/save", "/save/api"})
    //@RequiresPermissions("product:skuinfo:save")
    public R save(@RequestBody SkuInfoEntity skuInfo){
		skuInfoService.save(skuInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping({"/update", "/update/api"})
    //@RequiresPermissions("product:skuinfo:update")
    public R update(@RequestBody SkuInfoEntity skuInfo){
		skuInfoService.updateById(skuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping({"/delete", "/delete/api"})
    //@RequiresPermissions("product:skuinfo:delete")
    public R delete(@RequestBody Long[] skuIds){
		skuInfoService.removeByIds(Arrays.asList(skuIds));

        return R.ok();
    }

}
