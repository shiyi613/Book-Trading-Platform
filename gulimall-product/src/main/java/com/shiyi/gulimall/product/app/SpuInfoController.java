package com.shiyi.gulimall.product.app;

import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.R;
import com.shiyi.gulimall.product.entity.SpuInfoEntity;
import com.shiyi.gulimall.product.service.SpuInfoService;
import com.shiyi.gulimall.product.vo.SpuSaveVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * spu信息
 *
 * @author shiyi
 * @email sunlightcs@gmail.com
 * @date 2023-02-21 12:51:36
 */
@RestController
@RequestMapping("product/spuinfo")
public class SpuInfoController {
    @Autowired
    private SpuInfoService spuInfoService;

    @RequestMapping({"/infos", "/infos/api"})
    public List<Long> getCategoryIdsBySpuIds(@RequestParam("spuIds") List<Long> spuIds){
        List<SpuInfoEntity> spuInfoEntities = spuInfoService.getBaseMapper().selectBatchIds(spuIds);
        return spuInfoEntities.stream().map(item -> item.getCatalogId()).distinct().collect(Collectors.toList());
    }


    /**
     * 列表
     */
    @RequestMapping({"/list", "/list/api"})
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = spuInfoService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }


    @GetMapping({"/skuId/{id}", "/skuId/{id}/api"})
    public R getSpuInfoBySkuId(@PathVariable("id")Long skuId){
        SpuInfoEntity spuInfoEntity =  spuInfoService.getSpuInfoBySkuId(skuId);
        return R.ok().setData(spuInfoEntity);
    }

    /**
     * 商品上架
     * @param spuId
     * @return
     */
    @PostMapping({"/{spuId}/up", "/{spuId}/up/api"})
    public R spuUp(@PathVariable("spuId") Long spuId){
        spuInfoService.up(spuId);
        return R.ok();
    }


    /**
     * 信息
     */
    @RequestMapping({"/info/{id}", "/info/{id}/api"})
    //@RequiresPermissions("product:spuinfo:info")
    public R info(@PathVariable("id") Long id){
		SpuInfoEntity spuInfo = spuInfoService.getById(id);

        return R.ok().put("spuInfo", spuInfo);
    }

    /**
     * 保存
     */
    @RequestMapping({"/save", "/save/api"})
    public R save(@RequestBody SpuSaveVo vo){
//		spuInfoService.save(spuInfo);
        spuInfoService.saveSpuInfo(vo);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping({"/update", "/update/api"})
    //@RequiresPermissions("product:spuinfo:update")
    public R update(@RequestBody SpuInfoEntity spuInfo){
		spuInfoService.updateById(spuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping({"/delete", "/delete/api"})
    //@RequiresPermissions("product:spuinfo:delete")
    public R delete(@RequestBody Long[] ids){
		spuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
