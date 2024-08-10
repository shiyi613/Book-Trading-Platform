package com.shiyi.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.shiyi.gulimall.order.config.AlipayTemplate;
import com.shiyi.gulimall.order.service.IPublishedMsgService;
import com.shiyi.gulimall.order.service.OrderService;
import com.shiyi.gulimall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author:shiyi
 * @create: 2023-03-10  17:13
 */

@RestController
public class OrderPayedListener {


    @Autowired
    private OrderService orderService;

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private IPublishedMsgService publishedMsgService;

    /**
     * 支付宝异步通知
     * @return
     */
    @PostMapping("/payed/notify")
    public String handleAliPayed(PayAsyncVo vo, HttpServletRequest request) throws AlipayApiException {

        //验签
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            // valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(),
                alipayTemplate.getCharset(), alipayTemplate.getSign_type()); //调用SDK验证签名

        if (signVerified) {
//            System.out.println("签名验证成功...");
            //修改订单状态
            String result = orderService.handlePayResult(vo);
            //修改库存状态(改变库存工作详情单的状态 + 释放商品库存锁定量 + 减少商品库存量)
            try {
                publishedMsgService.saveAndSendMsg(vo.getOut_trade_no(),"stock-event-exchange","stock.payFinish",0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        } else {
//            System.out.println("签名验证失败...");
            return "error";
        }

    }
}
