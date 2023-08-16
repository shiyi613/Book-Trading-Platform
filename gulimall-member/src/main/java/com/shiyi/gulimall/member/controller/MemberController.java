package com.shiyi.gulimall.member.controller;

import java.rmi.server.ExportException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Map;
import java.util.Queue;

import com.shiyi.common.exception.BizCodeEnum;
import com.shiyi.gulimall.member.exception.PhoneExistException;
import com.shiyi.gulimall.member.exception.UsernameExistException;
import com.shiyi.gulimall.member.feign.CouponFeignService;
import com.shiyi.gulimall.member.vo.MemberLoginVo;
import com.shiyi.gulimall.member.vo.MemberRegistVo;
import com.shiyi.gulimall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.shiyi.gulimall.member.entity.MemberEntity;
import com.shiyi.gulimall.member.service.MemberService;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.R;



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

    @RequestMapping("/coupons")
    public R test(){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("小明");
        R membercoupons = couponFeignService.membercoupons();
        return R.ok().put("member",memberEntity).put("coupons",membercoupons.get("coupons"));
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

    @PostMapping("/oauth2/login")
    public R oauthLogin(@RequestBody SocialUser socialUser){
        MemberEntity login = memberService.login(socialUser);
        if(login != null){

            return R.ok().setData(login);
        }else{
            return R.error(BizCodeEnum.LOGIN_INVALID_EXCEPTION.getCode(),BizCodeEnum.LOGIN_INVALID_EXCEPTION.getMessage());
        }
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
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
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
