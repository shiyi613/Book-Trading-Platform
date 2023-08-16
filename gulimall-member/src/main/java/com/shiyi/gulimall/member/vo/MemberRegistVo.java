package com.shiyi.gulimall.member.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @Author:shiyi
 * @create: 2023-03-06  18:53
 */
@Data
public class MemberRegistVo {

    private String username;

    private String password;

    private String phone;
}


