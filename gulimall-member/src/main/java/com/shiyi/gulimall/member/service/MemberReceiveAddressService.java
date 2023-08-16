package com.shiyi.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.gulimall.member.entity.MemberReceiveAddressEntity;

import java.util.List;
import java.util.Map;

/**
 * 会员收货地址
 *
 * @author shiyi
 * @email 511665483@qq.com
 * @date 2023-02-21 16:19:28
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<MemberReceiveAddressEntity> getAddress(Long memberId);
}

