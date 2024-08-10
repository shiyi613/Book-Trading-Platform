package com.shiyi.gulimall.member.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 会员收货地址
 * 
 * @author shiyi
 * @email 511665483@qq.com
 * @date 2023-02-21 16:19:28
 */
@Data
@TableName("ums_member_receive_address")
public class MemberReceiveAddressEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	public Long id;
	/**
	 * member_id
	 */
	public Long memberId;
	/**
	 * 收货人姓名
	 */
	public String name;
	/**
	 * 电话
	 */
	public String phone;
	/**
	 * 邮政编码
	 */
	public String postCode;
	/**
	 * 省份/直辖市
	 */
	public String province;
	/**
	 * 城市
	 */
	public String city;
	/**
	 * 区
	 */
	public String region;
	/**
	 * 详细地址(街道)
	 */
	public String detailAddress;
	/**
	 * 省市区代码
	 */
	public String areacode;
	/**
	 * 是否默认
	 */
	public Integer defaultStatus;

}
