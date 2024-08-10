package com.shiyi.gulimall.order.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 支付信息表
 * 
 * @author shiyi
 * @email 511665483@qq.com
 * @date 2023-02-21 16:27:39
 */
@Data
@TableName("oms_payment_info")
public class PaymentInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * 订单号（对外业务号）
	 */
	private String orderSn;
	/**
	 * 订单id
	 */
	private Long orderId;
	/**
	 * 支付宝交易流水号
	 */
	private String alipayTradeNo;
	/**
	 * 支付总金额
	 */
	private BigDecimal totalAmount;
	/**
	 * 交易内容
	 */
	private String subject;
	/**
	 * 支付状态
	 */
	private String paymentStatus;
	/**
	 * 创建时间
	 */
	@TableField(value = "create_time",fill = FieldFill.INSERT)
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime createTime;
	/**
	 * 确认时间
	 */
	private Date confirmTime;
	/**
	 * 回调内容
	 */
	private String callbackContent;
	/**
	 * 回调时间
	 */
	private Date callbackTime;

}
