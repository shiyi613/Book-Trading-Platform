package com.shiyi.gulimall.member.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shiyi.common.constant.AuthServerConstant;
import com.shiyi.common.exception.BizCodeEnum;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.R;
import com.shiyi.common.vo.MemberCouponVo;
import com.shiyi.common.vo.MemberRespVo;
import com.shiyi.gulimall.member.entity.MemberCouponEntity;
import com.shiyi.gulimall.member.entity.MemberEntity;
import com.shiyi.gulimall.member.exception.PhoneExistException;
import com.shiyi.gulimall.member.exception.UsernameExistException;
import com.shiyi.gulimall.member.feign.CouponFeignService;
import com.shiyi.gulimall.member.interceptor.LoginInteceptor;
import com.shiyi.gulimall.member.service.MemberCouponService;
import com.shiyi.gulimall.member.service.MemberService;
import com.shiyi.gulimall.member.vo.MemberLoginVo;
import com.shiyi.gulimall.member.vo.MemberRegistVo;
import com.shiyi.gulimall.member.vo.SocialUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 会员
 *
 * @author shiyi
 * @email 511665483@qq.com
 * @date 2023-02-21 16:19:27
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private MemberCouponService memberCouponService;

    @RequestMapping("/coupons")
    public R test(){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("小明");
        R membercoupons = couponFeignService.membercoupons();
        return R.ok().put("member",memberEntity).put("coupons",membercoupons.get("coupons"));
    }

    @RequestMapping("/updateUserInfo")
    public void updateUserInfo(@RequestParam("nickName")String nickName,
                               @RequestParam("gender")Integer gender,
                               @RequestParam("birthdayYear")String year,
                               @RequestParam("birthdayMonth")String month,
                               @RequestParam("birthdayDay")String day,
                               @RequestParam("email")String email,
                               @RequestParam("phone")String phone,
                               HttpSession session){
        MemberRespVo memberRespVo = LoginInteceptor.loginUser.get();
        memberRespVo.setNickname(nickName);
        memberRespVo.setGender(gender);
        Date birth = new Date(Integer.valueOf(year) - 1900, Integer.valueOf(month) - 1, Integer.valueOf(day));
        memberRespVo.setBirth(birth);
        memberRespVo.setEmail(email);
        memberRespVo.setMobile(phone);
        session.setAttribute(AuthServerConstant.LOGIN_USER, memberRespVo);
        MemberEntity memberEntity = new MemberEntity();
        BeanUtils.copyProperties(memberRespVo,memberEntity);
        memberService.updateById(memberEntity);
    }

    @RequestMapping("/updateUserImageUrl")
    public void updateUserImageUrl(@RequestParam("imageUrl")String imageUrl, HttpSession session){
        MemberRespVo memberRespVo = LoginInteceptor.loginUser.get();
        memberRespVo.setHeader(imageUrl);
        session.setAttribute(AuthServerConstant.LOGIN_USER, memberRespVo);
        MemberEntity memberEntity = new MemberEntity();
        BeanUtils.copyProperties(memberRespVo,memberEntity);
        memberService.updateById(memberEntity);
    }

    @GetMapping("/getCouponIdsByUserId")
    public List<MemberCouponEntity> getCouponIdsByUserId(@RequestParam("uid")Long userId){
        return memberCouponService.list(new QueryWrapper<MemberCouponEntity>().eq("uid", userId));
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo) {

        MemberEntity entity = memberService.login(vo);
        if (entity != null) {
            return R.ok().setData(entity);
        } else {
            return R.error(BizCodeEnum.LOGIN_INVALID_EXCEPTION.getCode(), BizCodeEnum.LOGIN_INVALID_EXCEPTION.getMessage());
        }
    }

    @RequestMapping("/receiveCoupon")
    public String receiveCoupon(@RequestParam("couponId") Long couponId, @RequestParam("limit") Integer limit){
        Long id = LoginInteceptor.loginUser.get().getId();
        MemberCouponEntity entity = memberCouponService.getBaseMapper().selectOne(
                new QueryWrapper<MemberCouponEntity>().eq("uid", id).eq("cid", couponId));
        if(entity != null && entity.getNum() >= limit){
            return "优惠券领取次数超出限制";
        }

        if(entity == null){
            MemberCouponEntity entity1 = new MemberCouponEntity();
            entity1.setUid(id);
            entity1.setCid(couponId);
            entity1.setNum(1L);
            memberCouponService.save(entity1);
        }else{
            entity.setNum(entity.getNum() + 1);
            memberCouponService.updateById(entity);
        }

        // 修改领取数量
        couponFeignService.receiveCoupon(couponId);

        return "success";
    }

    @RequestMapping("/getCurrentUserCoupons")
    public List<MemberCouponVo> getCurrentUserCoupons(){
        Long id = LoginInteceptor.loginUser.get().getId();
        List<MemberCouponEntity> entities = memberCouponService.getBaseMapper().selectList(new QueryWrapper<MemberCouponEntity>().eq("uid", id));
        return entities.stream().map(item -> {
            MemberCouponVo vo = new MemberCouponVo();
            vo.setId(item.getId());
            vo.setCid(item.getCid());
            vo.setUid(item.getUid());
            vo.setNum(item.getNum());
//            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @PostMapping("/oauth2/login")
    public R oauthLogin(@RequestBody SocialUser socialUser){
        MemberEntity login = memberService.login(socialUser);
        if(login != null){

            return R.ok().setData(login);
        }else{
            return R.error(BizCodeEnum.LOGIN_INVALID_EXCEPTION.getCode(),BizCodeEnum.LOGIN_INVALID_EXCEPTION.getMessage());
        }
    }

    @RequestMapping("/deleteCouponNum")
    public R deleteCouponNum(@RequestParam("couponId") Long couponId){
        Long id = LoginInteceptor.loginUser.get().getId();
        MemberCouponEntity entity = memberCouponService.getBaseMapper().selectOne(new QueryWrapper<MemberCouponEntity>().eq("uid", id).eq("cid", couponId));
        if(entity.getNum() == 1){
            memberCouponService.removeById(entity.getId());
            return R.ok();
        }else{
            entity.setNum(entity.getNum() - 1);
            memberCouponService.updateById(entity);
            return R.ok();
        }
    }

    /**
     * 列表
     */
    @RequestMapping({"/list", "/list/api"})
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo vo){

        try {
            memberService.regist(vo);
            return R.ok();
        }catch (PhoneExistException e){
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(),BizCodeEnum.PHONE_EXIST_EXCEPTION.getMessage());
        }catch (UsernameExistException e){
            return R.error(BizCodeEnum.USERNAME_EXIST_EXCEPTION.getCode(),BizCodeEnum.USERNAME_EXIST_EXCEPTION.getMessage());
        }
    }


    /**
     * 信息
     */
    @RequestMapping({"/info/{id}", "/info/{id}/api"})
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping({"/save", "/save/api"})
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping({"/update", "/update/api"})
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping({"/delete", "/delete/api"})
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
