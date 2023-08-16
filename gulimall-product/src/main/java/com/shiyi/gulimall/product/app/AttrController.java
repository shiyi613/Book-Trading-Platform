package com.shiyi.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.shiyi.gulimall.product.entity.ProductAttrValueEntity;
import com.shiyi.gulimall.product.service.ProductAttrValueService;
import com.shiyi.gulimall.product.vo.AttrRespVo;
import com.shiyi.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.shiyi.gulimall.product.service.AttrService;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.R;



/**
 * 商品属性
 *
 * @author shiyi
 * @email sunlightcs@gmail.com
 * @date 2023-02-21 12:51:36
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrListforSpu(@PathVariable("spuId")Long spuId){
        List<ProductAttrValueEntity> entityList = productAttrValueService.baseAttrListforSpu(spuId);
        return R.ok().put("data",entityList);
    }

    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params,
                          @PathVariable("catelogId")Long catelogId,
                          @PathVariable("attrType")String attrType){

        PageUtils page = attrService.queryBaseAttrPage(params,catelogId,attrType);
        return R.ok().put("page",page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId){
		AttrRespVo respVo = attrService.getAttrInfo(attrId);

        return R.ok().put("attr", respVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVo attr){
		attrService.saveAttr(attr);

        return R.ok();
    }


    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVo attr){
		attrService.updateAttr(attr);

        return R.ok();
    }

    @RequestMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable("spuId")Long spuId,
                           @RequestBody List<ProductAttrValueEntity> entities){
        productAttrValueService.updateSpuAttr(spuId,entities);
        return R.ok();
    }



    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
