package com.shiyi.common.exception;

/**
 * @Author:shiyi
 * @create: 2023-02-23  8:35
 *
 * 错误码和错误信息定义类
 * 1. 错误码定义规则为 5 为数字
 * 2. 前两位表示业务场景，最后三位表示错误码。例如：100001。10:通用 001:系统未知异常
 * 3. 维护错误码后需要维护错误描述，将他们定义为枚举形式
 * 错误码列表：
 * 10: 通用
 *          001：参数格式校验
 *          002: 短信验证码频率太高
 * 11: 商品
 * 12: 订单
 * 13: 购物车
 * 14: 物流
 * 15: 用户
 * 21: 库存
 **/

public enum BizCodeEnum {

    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    VALID_EXCEPTION(10001,"参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002,"短信验证码获取频率太高"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常"),
    USERNAME_EXIST_EXCEPTION(15001,"用户名已存在"),
    PHONE_EXIST_EXCEPTION(15002,"手机号已存在"),
    LOGIN_INVALID_EXCEPTION(15003,"账号密码错误"),
    NO_STOCK_EXCEPTION(21000,"商品库存不足"),
    EXCEEDED_TIMES(22000, "优惠券领取次数超出限制");

    private Integer code;
    private String message;

    BizCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
