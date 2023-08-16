package com.shiyi.gulimall.member.exception;

/**
 * @Author:shiyi
 * @create: 2023-03-06  19:07
 */
public class UsernameExistException extends RuntimeException{

    public UsernameExistException() {
        super("用户名已存在");
    }
}
