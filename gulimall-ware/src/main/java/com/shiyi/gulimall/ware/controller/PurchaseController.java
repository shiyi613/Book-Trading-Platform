package com.shiyi.gulimall.ware.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.shiyi.gulimall.ware.vo.MergeVo;
import com.shiyi.gulimall.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.shiyi.gulimall.ware.entity.PurchaseEntity;
import com.shiyi.gulimall.ware.service.PurchaseService;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.R;



/**
 * 采购信息
 *
 * @author shiyi
 * @email 511665483@qq.com
 * @date 2023-02-21 16:31:11
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 完成采购
     */
    @PostMapping("/done")
    public R finishPurchase(@RequestBody PurchaseDoneVo doneVo){
        purchaseService.finishPurchase(doneVo);
        return R.ok();
    }


    /**
     * 领取采购单
     */
    @PostMapping("/received")
    public R receivePurchase(@RequestBody List<Long> ids){
        purchaseService.receivePurchase(ids);
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:purchase:list")
    public R UnreceivePurchaselist(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPageUnreceivePurchase(params);

        return R.ok().put("page", page);
    }

    @RequestMapping("/unreceive/list")
    //@RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    @RequestMapping("/merge")
    //@RequiresPermissions("ware:purchase:list")
    public R mergePurchase(@RequestBody MergeVo mergeVo){

        purchaseService.mergePurchase(mergeVo);

        return R.ok();
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase){
        purchase.setCreateTime(new Date());
        purchase.setUpdateTime(new Date());
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
