package com.shiyi.gulimall.order.web;

import com.shiyi.common.utils.R;
import com.shiyi.gulimall.order.constant.OrderConstant;
import com.shiyi.gulimall.order.entity.OrderEntity;
import com.shiyi.gulimall.order.service.OrderService;
import com.shiyi.gulimall.order.vo.OrderConfirmVo;
import com.shiyi.gulimall.order.vo.OrderSubmitVo;
import com.shiyi.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * @Author:shiyi
 * @create: 2023-03-08  19:33
 */
@Controller
@RequestMapping("order/order")
public class OrderWebController {

    @Autowired
    private OrderService orderService;


    @ResponseBody
    @GetMapping("/status/{orderSn}")
    public R getOrderStatus(@PathVariable("orderSn")String orderSn){
        OrderEntity orderEntity =  orderService.getOrderStatus(orderSn);
        return R.ok().setData(orderEntity);



    }


    @GetMapping("/toTrade")
    public String toTrade(Model model){

        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData",confirmVo);
        return "confirm";
    }


    /**
     * 下单
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes) {

        SubmitOrderResponseVo responseVo = null;
        try {
            responseVo = orderService.submitOrder(vo);
            if(responseVo.getCode() == 0) {
                //下单成功跳到支付选择页
                model.addAttribute("submitOrderResp", responseVo);
                return "pay";
            }
            return "redirect:http://order.gulimall.com/order/order/toTrade";
        } catch (IOException | RuntimeException | InterruptedException | ExecutionException e) {

            //下单失败跳到订单确认页
            String msg = "下单失败：";
            if(e instanceof ExecutionException){
                msg += e.getMessage();
            }else{
                switch (e.getMessage()){
                    case "1": msg += OrderConstant.ORDER_CODE1_REASON;break;
                    case "2": msg += OrderConstant.ORDER_CODE2_REASON;break;
                    case "3": msg += OrderConstant.ORDER_CODE3_REASON;break;
                }
            }
            redirectAttributes.addFlashAttribute("msg",msg);
            return "redirect:http://order.gulimall.com/order/order/toTrade";
        }
    }

}
