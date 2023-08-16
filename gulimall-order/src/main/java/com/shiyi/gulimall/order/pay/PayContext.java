package com.shiyi.gulimall.order.pay;

import com.alipay.api.AlipayApiException;
import com.shiyi.gulimall.order.vo.PayVo;

public class PayContext {

    private PayStrategy payStrategy;

    public PayContext(PayStrategy payStrategy){
        this.payStrategy = payStrategy;
    }

    public String executePay(PayVo payVo) throws AlipayApiException {
        return payStrategy.pay(payVo);
    }
}
