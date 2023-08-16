package com.shiyi.gulimall.member.dao;

import com.shiyi.gulimall.member.entity.MemberEntity;
import com.shiyi.gulimall.member.entity.MemberLevelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员等级
 * 
 * @author shiyi
 * @email 511665483@qq.com
 * @date 2023-02-21 16:19:28
 */
@Mapper
public interface MemberLevelDao extends BaseMapper<MemberLevelEntity> {

    MemberLevelEntity getDefaultLevel();

}
