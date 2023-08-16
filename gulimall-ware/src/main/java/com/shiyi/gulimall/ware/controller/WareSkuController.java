package com.shiyi.gulimall.ware.controller;

import com.google.common.collect.Lists;
import com.shiyi.common.exception.BizCodeEnum;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.R;
import com.shiyi.gulimall.ware.entity.WareSkuEntity;
import com.shiyi.gulimall.ware.exception.NoStockException;
import com.shiyi.gulimall.ware.service.WareSkuService;
import com.shiyi.gulimall.ware.vo.SkuHasStockVo;
import com.shiyi.gulimall.ware.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 商品库存
 *
 * @author shiyi
 * @email 511665483@qq.com
 * @date 2023-02-21 16:31:11
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;


    /**
     * 为某个订单锁住商品的库存
     */
    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo){

        try{
            wareSkuService.orderLockStock(vo);
            return R.ok();
        }catch (NoStockException | IOException e ){
            return R.error(BizCodeEnum.NO_STOCK_EXCEPTION.getCode(),BizCodeEnum.NO_STOCK_EXCEPTION.getMessage());
        }
    }

    @PostMapping("/lock/hasStock")
    public Boolean getSkuHasStock(@RequestParam("skuId")Long skuId,@RequestParam("num")int num){
        return wareSkuService.getSkuHasStock(skuId,num);
    }


    @PostMapping("/hasStock")
    public R getSkuHasStock(@RequestParam("skuId")Long skuId){

        List<SkuHasStockVo> vo = wareSkuService.getSkuHasStock(Lists.newArrayList(skuId));
        return R.ok().setData(vo.get(0));
    }

    /**
     * 查询sku是否有库存
     */
    @PostMapping("/hasstock")
    public R getSkuHasStock(@RequestBody List<Long> skuIds){

        List<SkuHasStockVo> vos = wareSkuService.getSkuHasStock(skuIds);
        return R.ok().setData(vos);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
