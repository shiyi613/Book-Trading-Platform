package com.shiyi.gulimall.order.pay;

import com.alipay.api.AlipayApiException;
import com.shiyi.gulimall.order.vo.PayVo;

public interface PayStrategy {

    Integer getType();

    String pay(PayVo payVo) throws AlipayApiException;
}
