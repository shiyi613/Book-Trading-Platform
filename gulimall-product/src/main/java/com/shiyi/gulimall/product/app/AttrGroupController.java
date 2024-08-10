package com.shiyi.gulimall.product.app;

import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.R;
import com.shiyi.gulimall.product.entity.AttrEntity;
import com.shiyi.gulimall.product.entity.AttrGroupEntity;
import com.shiyi.gulimall.product.service.AttrAttrgroupRelationService;
import com.shiyi.gulimall.product.service.AttrGroupService;
import com.shiyi.gulimall.product.service.AttrService;
import com.shiyi.gulimall.product.service.CategoryService;
import com.shiyi.gulimall.product.vo.AttrGroupReleationVo;
import com.shiyi.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 属性分组
 *
 * @author shiyi
 * @email sunlightcs@gmail.com
 * @date 2023-02-21 12:51:36
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;

    @PostMapping({"/attr/relation", "/attr/relation/api"})
    public R addAttrRelation(@RequestBody List<AttrGroupReleationVo> vos){
        relationService.saveBatch(vos);

        return R.ok();
    }

    @GetMapping({"/{catelogId}/withattr", "/{catelogId}/withattr/api"})
    public R getAttrGroupWithAttrs(@PathVariable("catelogId")Long catelogId){
        //1、查出当前分类下的所有属性分组
        //2、查出每个属性分组的所有属性
        List<AttrGroupWithAttrsVo> vos = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);
        return R.ok().put("data",vos);
    }


    @GetMapping({"/{attrgroupId}/attr/relation", "/{attrgroupId}/attr/relation/api"})
    public R attrRelation(@PathVariable("attrgroupId")Long attrgroupId){
        List<AttrEntity> entities = attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data",entities);
    }

    @GetMapping({"/{attrgroupId}/noattr/relation", "/{attrgroupId}/noattr/relation/api"})
    public R attrNoRelation(@RequestParam Map<String, Object> params,
                            @PathVariable("attrgroupId")Long attrgroupId){

        PageUtils page = attrService.getNoRelationAttr(params,attrgroupId);
        return R.ok().put("page",page);
    }


    /**
     * 列表
     */
    @RequestMapping({"/list/{catelogId}", "/list/{catelogId}/api"})
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("catelogId") Long catelogId){

        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping({"/info/{attrGroupId}", "/info/{attrGroupId}/api"})
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        Long catelogId = attrGroup.getCatelogId();
        Long[] path = categoryService.findCatelogPath(catelogId);
		attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping({"/save", "/save/api"})
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 删除属性与分组的关联关系,记得加@RequestBody
     */
    @PostMapping({"/attr/relation/delete", "/attr/relation/delete/api"})
    public R deleteRelation(@RequestBody AttrGroupReleationVo[] vos){
        attrService.deleteRelation(vos);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping({"/update", "/update/api"})
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping({"/delete", "/delete/api"})
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
