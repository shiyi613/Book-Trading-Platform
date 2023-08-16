package com.shiyi.gulimall.order.pay;

import com.alipay.api.AlipayApiException;
import com.shiyi.gulimall.order.vo.PayVo;
import org.springframework.stereotype.Component;

@Component
public class WechatPayStrategy implements PayStrategy{

    @Override
    public Integer getType() {
        return payMethodConstant.WeCharPay;
    }

    @Override
    public String pay(PayVo payVo) throws AlipayApiException {
        return null;
    }
}
