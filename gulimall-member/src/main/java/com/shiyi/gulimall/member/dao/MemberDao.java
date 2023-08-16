package com.shiyi.gulimall.member.dao;

import com.shiyi.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author shiyi
 * @email 511665483@qq.com
 * @date 2023-02-21 16:19:27
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
