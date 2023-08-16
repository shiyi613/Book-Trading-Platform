package com.shiyi.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.gulimall.member.entity.MemberEntity;
import com.shiyi.gulimall.member.exception.PhoneExistException;
import com.shiyi.gulimall.member.exception.UsernameExistException;
import com.shiyi.gulimall.member.vo.MemberLoginVo;
import com.shiyi.gulimall.member.vo.MemberRegistVo;
import com.shiyi.gulimall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author shiyi
 * @email 511665483@qq.com
 * @date 2023-02-21 16:19:27
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo vo);

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkUsernameUnique(String username) throws UsernameExistException;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity login(SocialUser socialUser);
}

