package com.shiyi.gulimall.member.exception;

/**
 * @Author:shiyi
 * @create: 2023-03-06  19:08
 */
public class PhoneExistException extends RuntimeException{

    public PhoneExistException() {
        super("手机号已存在");
    }
}
