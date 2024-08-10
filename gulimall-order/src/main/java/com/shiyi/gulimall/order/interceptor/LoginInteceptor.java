package com.shiyi.gulimall.order.interceptor;

import com.shiyi.common.constant.AuthServerConstant;
import com.shiyi.common.vo.MemberRespVo;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author:shiyi
 * @create: 2023-03-08  19:35
 */
public class LoginInteceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //对某些类内部远程调用放行,使用路径匹配，例如给RabbitMq的监听方法 解锁库存等
        String requestURI = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/order/order/status/**", requestURI);
        boolean match1 = new AntPathMatcher().match("/payed/notify", requestURI);
        boolean match2 = new AntPathMatcher().match("/order/**/**/api", requestURI);
        boolean match3 = new AntPathMatcher().match("/order/updateMsgStatus", requestURI);
        if(match || match1 || match2 || match3){
            return true;
        }


        MemberRespVo attribute = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute != null){
            //已登录
            loginUser.set(attribute);
            return true;
        }else{
            //未登录
            request.getSession().setAttribute("msg","请先登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }
}
