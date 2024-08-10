package com.shiyi.gulimall.cart.controller;

import com.shiyi.gulimall.cart.exception.OutOfStockException;
import com.shiyi.gulimall.cart.service.CartService;
import com.shiyi.gulimall.cart.vo.CartItemVo;
import com.shiyi.gulimall.cart.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @Author:shiyi
 * @create: 2023-03-07  19:32
 */
@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @ResponseBody
    @GetMapping("/currentUserCartItems")
    public List<CartItemVo> getCurrentUserCartItems(){

        return cartService.getCurrentUserCartItems();
    }

    @ResponseBody
    @GetMapping("/currentUserCartNumber")
    public int getCurrentUserCartNumber(){

        return cartService.getCartNumber();
    }

    @GetMapping("/cart.html")
    public String cartListPage(Model model){

        CartVo cartInfo = null;
        try {
            cartInfo = cartService.getCart();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.out.println(e.getMessage() + e.getCause());
        }
        model.addAttribute("cart",cartInfo);


        return "cartList";
    }

    @GetMapping("/addCartItem")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            ModelAndView modelAndView,
                            RedirectAttributes ra){

        CartItemVo cartItemVo = null;
        try {
            cartItemVo = cartService.addToCart(skuId, num);
        } catch (OutOfStockException e) {
            modelAndView.addObject("msg",e.getMessage());
            return "error";
        }
        if(cartItemVo == null){
            return "error";
        }
        ra.addAttribute("skuId",skuId);
        ra.addAttribute("num",num);
        //重定向到成功页面，避免重复提交请求
        return "redirect:http://cart.gulimall.com/addToCartSuccessPage";
    }

    @GetMapping("/addToCartSuccessPage")
    public String addToCartSuccessPage(@RequestParam("skuId")Long skuId,@RequestParam("num")Integer num, Model model){

        CartItemVo cartItem = cartService.getCartItem(skuId);
        cartItem.setCount(num);
        model.addAttribute("item",cartItem);
        return "success";
    }

    /**
     * 将商品勾选状态持久化到Redis
     * @param skuId
     * @param checked
     * @return
     */
    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId")Long skuId,@RequestParam("checked")Integer checked){
        cartService.checkItem(skuId,checked);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 改变商品数量持久化到Redis
     * @return
     */
    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId")Long skuId,@RequestParam("num")Integer num){
        cartService.countItem(skuId,num);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 删除购物车所选购物项
     * @return
     */
    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId")Long skuId){
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }
}
