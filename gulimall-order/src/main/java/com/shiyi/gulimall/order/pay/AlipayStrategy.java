package com.shiyi.gulimall.order.pay;

import com.alipay.api.AlipayApiException;
import com.shiyi.gulimall.order.config.AlipayTemplate;
import com.shiyi.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AlipayStrategy implements PayStrategy{

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Override
    public Integer getType() {
        return payMethodConstant.AliPay;
    }

    @Override
    public String pay(PayVo payVo) throws AlipayApiException {
        return alipayTemplate.pay(payVo);
    }
}
