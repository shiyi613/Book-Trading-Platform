package com.shiyi.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.shiyi.gulimall.order.pay.PayContext;
import com.shiyi.gulimall.order.pay.PayStrategy;
import com.shiyi.gulimall.order.pay.PayStrategyFactory;
import com.shiyi.gulimall.order.service.OrderService;
import com.shiyi.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author:shiyi
 * @create: 2023-03-10  14:21
 */
@Controller
public class PayWebController {

    @Autowired
    private OrderService orderService;

    @ResponseBody
    @GetMapping(value = "/payOrder",produces = "text/html")
    public String payOrder(@RequestParam("payType")Integer payType,@RequestParam("orderSn")String orderSn) throws AlipayApiException {
        PayStrategy strategy = PayStrategyFactory.getInstance().getStrategy(payType);
        PayVo payVo = orderService.getOrderPay(orderSn);
        PayContext payContext = new PayContext(strategy);
        // 这里调用支付宝的工具类后生成的是一个支付宝支付的页面，故直接交给浏览器渲染
        return payContext.executePay(payVo);
    }
}
