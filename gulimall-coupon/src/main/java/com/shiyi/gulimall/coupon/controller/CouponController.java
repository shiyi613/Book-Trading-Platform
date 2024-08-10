package com.shiyi.gulimall.coupon.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.R;
import com.shiyi.common.vo.CouponVo;
import com.shiyi.common.vo.MemberCouponVo;
import com.shiyi.gulimall.coupon.entity.CouponEntity;
import com.shiyi.gulimall.coupon.entity.CouponSpuCategoryRelationEntity;
import com.shiyi.gulimall.coupon.entity.CouponSpuRelationEntity;
import com.shiyi.gulimall.coupon.feign.MemberFeignService;
import com.shiyi.gulimall.coupon.interceptor.LoginInteceptor;
import com.shiyi.gulimall.coupon.service.CouponService;
import com.shiyi.gulimall.coupon.service.CouponSpuCategoryRelationService;
import com.shiyi.gulimall.coupon.service.CouponSpuRelationService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 优惠券信息
 *
 * @author shiyi
 * @email 511665483@qq.com
 * @date 2023-02-21 16:06:49
 */

@Controller
@RequestMapping("coupon/coupon")
public class CouponController {
    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponSpuCategoryRelationService couponSpuCategoryRelationService;

    @Autowired
    private CouponSpuRelationService couponSpuRelationService;

    @Autowired
    private MemberFeignService memberFeignService;

    @ResponseBody
    @RequestMapping({"/member/list"})
    public R membercoupons(){
        CouponEntity couponEntity = new CouponEntity();
        couponEntity.setCouponName("满100减10");
        return R.ok().put("coupons",Arrays.asList(couponEntity));
    }


    @RequestMapping("/coupon.html")
    public String couponPage(Model model,String keyword){

        List<CouponEntity> couponInfo;
        QueryWrapper<CouponEntity> queryWrapper = new QueryWrapper<>();
        if(keyword != null){
            queryWrapper = queryWrapper.like("coupon_name",keyword);
        }
        couponInfo = couponService.getBaseMapper().selectList(queryWrapper);
        List<CouponVo> lastCouponInfo = couponInfo.stream().filter(item -> {
            if(item.getReceiveCount() >= item.getNum()){
                return false;
            }
            return true;
        }).map(item -> {
            CouponVo couponVo = new CouponVo();
            BeanUtils.copyProperties(item, couponVo);
            StringBuilder sb = new StringBuilder();
            if(item.getUseType() == 1){
                List<CouponSpuCategoryRelationEntity> entities = couponSpuCategoryRelationService.getCategoryByCouponId(item.getId());
                for(int i = 0; i < entities.size() - 1; i++){
                    sb.append(entities.get(i).getCategoryName()).append(",");
                }
                sb.append(entities.get(entities.size() - 1).getCategoryName());
                couponVo.setLimitCate(sb.toString());
            }else if(item.getUseType() == 2){
                List<CouponSpuRelationEntity> entities = couponSpuRelationService.getSpuByCouponId(item.getId());
                for(int i = 0; i < entities.size() - 1; i++){
                    sb.append(entities.get(i).getSpuName()).append(",");
                }
                sb.append(entities.get(entities.size() - 1).getSpuName());
                couponVo.setLimitPro(sb.toString());
            }
            return couponVo;
        }).collect(Collectors.toList());
        model.addAttribute("coupon", lastCouponInfo);

        return "couponList";
    }

    @RequestMapping("/myCoupons.html")
    public String myCouponsPage(Model model){

        Long uid = LoginInteceptor.loginUser.get().getId();

        // 先根据用户id查询出其拥有哪些优惠券id
        List<MemberCouponVo> memberCouponVoList = memberFeignService.getCouponIdsByUserId(uid);
        List<Long> couponIds = memberCouponVoList.stream().map(MemberCouponVo::getCid).collect(Collectors.toList());
        // 做一个优惠券id和数量的映射
        Map<Long, Long> map = memberCouponVoList.stream().collect(Collectors.toMap(MemberCouponVo::getCid, MemberCouponVo::getNum));
        List<CouponEntity> couponInfo;
        couponInfo = couponService.getBaseMapper().selectList(new QueryWrapper<CouponEntity>().in("id",couponIds));
        List<CouponVo> lastCouponInfo = couponInfo.stream().map(item -> {
            CouponVo couponVo = new CouponVo();
            BeanUtils.copyProperties(item, couponVo);
            couponVo.setNum(Integer.parseInt(map.get(item.getId()).toString()));
            StringBuilder sb = new StringBuilder();
            if(item.getUseType() == 1){
                List<CouponSpuCategoryRelationEntity> entities = couponSpuCategoryRelationService.getCategoryByCouponId(item.getId());
                for(int i = 0; i < entities.size() - 1; i++){
                    sb.append(entities.get(i).getCategoryName()).append(",");
                }
                sb.append(entities.get(entities.size() - 1).getCategoryName());
                couponVo.setLimitCate(sb.toString());
            }else if(item.getUseType() == 2){
                List<CouponSpuRelationEntity> entities = couponSpuRelationService.getSpuByCouponId(item.getId());
                for(int i = 0; i < entities.size() - 1; i++){
                    sb.append(entities.get(i).getSpuName()).append(",");
                }
                sb.append(entities.get(entities.size() - 1).getSpuName());
                couponVo.setLimitPro(sb.toString());
            }
            return couponVo;
        }).collect(Collectors.toList());
        model.addAttribute("coupon", lastCouponInfo);

        return "myCoupons";
    }

    @ResponseBody
    @RequestMapping("/getDetails")
        public List<CouponVo> getInfosByIds(@RequestParam("ids") List<Long> ids){
        List<CouponEntity> couponEntities = couponService.getBaseMapper().selectBatchIds(ids);
        List<CouponVo> collect = couponEntities.stream().map(item -> {
            CouponVo couponVo = new CouponVo();
            BeanUtils.copyProperties(item, couponVo);
            return couponVo;
        }).collect(Collectors.toList());
        return collect;
    }

    @ResponseBody
    @RequestMapping("/getCategoryId")
    public List<Long> getCategoryId(@RequestParam("couponId") Long couponId){
        List<CouponSpuCategoryRelationEntity> entities = couponSpuCategoryRelationService.getCategoryByCouponId(couponId);
        return entities.stream().map(item -> item.getCategoryId()).collect(Collectors.toList());
    }

    @ResponseBody
    @RequestMapping("/getSpuId")
    public List<Long> getSpuId(@RequestParam("couponId") Long couponId){
        List<CouponSpuRelationEntity> entities = couponSpuRelationService.getSpuByCouponId(couponId);
        return entities.stream().map(item -> item.getSpuId()).collect(Collectors.toList());
    }


    @ResponseBody
    @RequestMapping("/receiveCoupon")
    public R receiveCoupon(@RequestParam("couponId") Long couponId){
        CouponEntity entity = couponService.getOne(new QueryWrapper<CouponEntity>().eq("id", couponId));
        entity.setReceiveCount(entity.getReceiveCount() + 1);
        couponService.updateById(entity);
        return R.ok();
    }

    /**
     * 列表
     */
    @ResponseBody
    @RequestMapping({"/list", "/list/api"})
    //@RequiresPermissions("coupon:coupon:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = couponService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @ResponseBody
    @RequestMapping({"/info/{id}", "/info/{id}/api"})
    //@RequiresPermissions("coupon:coupon:info")
    public R info(@PathVariable("id") Long id){
		CouponEntity coupon = couponService.getById(id);

        return R.ok().put("coupon", coupon);
    }

    /**
     * 保存
     */
    @ResponseBody
    @RequestMapping({"/save", "/save/api"})
    //@RequiresPermissions("coupon:coupon:save")
    public R save(@RequestBody CouponEntity coupon){
		couponService.save(coupon);

        return R.ok();
    }

    /**
     * 修改
     */
    @ResponseBody
    @RequestMapping({"/update", "/update/api"})
    //@RequiresPermissions("coupon:coupon:update")
    public R update(@RequestBody CouponEntity coupon){
		couponService.updateById(coupon);

        return R.ok();
    }

    /**
     * 删除
     */
    @ResponseBody
    @RequestMapping({"/delete", "/delete/api"})
    //@RequiresPermissions("coupon:coupon:delete")
    public R delete(@RequestBody Long[] ids){
		couponService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
