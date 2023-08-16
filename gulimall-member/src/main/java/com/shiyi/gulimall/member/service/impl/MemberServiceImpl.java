package com.shiyi.gulimall.member.service.impl;

import com.shiyi.gulimall.member.dao.MemberLevelDao;
import com.shiyi.gulimall.member.entity.MemberLevelEntity;
import com.shiyi.gulimall.member.exception.PhoneExistException;
import com.shiyi.gulimall.member.exception.UsernameExistException;
import com.shiyi.gulimall.member.vo.MemberLoginVo;
import com.shiyi.gulimall.member.vo.MemberRegistVo;
import com.shiyi.gulimall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.Query;

import com.shiyi.gulimall.member.dao.MemberDao;
import com.shiyi.gulimall.member.entity.MemberEntity;
import com.shiyi.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelDao memberLevelDao;

    @Autowired
    private MemberDao memberDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) {
        MemberEntity memberEntity = new MemberEntity();

        //检查用户名和手机号唯一，为了让controller感知异常，使用异常机制
        checkPhoneUnique(vo.getPhone());
        checkUsernameUnique(vo.getUsername());

        memberEntity.setMobile(vo.getPhone());
        memberEntity.setUsername(vo.getUsername());

        //设置默认会员等级
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());
        memberEntity.setNickname(vo.getUsername());

        //设置密码(加密处理)采用MD5盐值加密
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);

        memberDao.insert(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException{

        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if(count > 0){
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException{

        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if(count > 0){
            throw new UsernameExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {

        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();

        MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().
                eq("username", loginacct).or().eq("mobile", loginacct));

        if(entity == null){     //查无此用户
            return null;
        }else{      //匹配密码
            String passwordDB = entity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean matches = passwordEncoder.matches(password, passwordDB);
            if(matches){
                return entity;
            }else{    //密码不正确
                return null;
            }
        }
    }

    @Override
    public MemberEntity login(SocialUser socialUser) {

        Integer uid = socialUser.getUid();
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if(memberEntity != null){
            //已经注册过
            MemberEntity updateEntity = new MemberEntity();
            updateEntity.setId(memberEntity.getId());
            updateEntity.setAccessToken(socialUser.getAccess_token());
            updateEntity.setExpiresIn(socialUser.getExpires_in());

            this.baseMapper.updateById(updateEntity);

            memberEntity.setAccessToken(socialUser.getAccess_token());
            memberEntity.setExpiresIn(socialUser.getExpires_in());
            return memberEntity;
        }else{
            //未注册过，进行注册流程
            MemberEntity registEntity = new MemberEntity();
            registEntity.setSocialUid(uid.toString());
            registEntity.setExpiresIn(socialUser.getExpires_in());
            registEntity.setAccessToken(socialUser.getAccess_token());
            registEntity.setNickname(socialUser.getName());
            registEntity.setHeader(socialUser.getAvatarUrl());
            registEntity.setLevelId(1L);
            registEntity.setCreateTime(new Date());

            this.baseMapper.insert(registEntity);
            return registEntity;
        }
    }
}