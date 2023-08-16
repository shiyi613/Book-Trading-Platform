package com.shiyi.gulimall.product.exception;

import com.shiyi.common.exception.BizCodeEnum;
import com.shiyi.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author:shiyi
 * @create: 2023-02-23  8:42
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.shiyi.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e){
        log.error("数据校验出现问题{},异常类型：{}",e.getMessage(),e.getClass());
        BindingResult result = e.getBindingResult();
        Map<String, String> map = new HashMap<>();

        //1、获取校验的错误结果
        result.getFieldErrors().forEach(item->{
            //获取错误属性的名字
            String field = item.getField();
            //获取错误提示
            String message = item.getDefaultMessage();
            map.put(field,message);
        });

        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(),BizCodeEnum.VALID_EXCEPTION.getMessage()).put("data",map);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable){

        log.error("出现问题{},异常类型：{}",throwable.getMessage(),throwable.getClass());
        return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(),BizCodeEnum.UNKNOW_EXCEPTION.getMessage());
    }
}
