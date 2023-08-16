package com.shiyi.gulimall.cart.to;

import lombok.Data;

/**
 * @Author:shiyi
 * @create: 2023-03-07  19:42
 */
@Data
public class UserInfoTo {

    private Long userId;

    private String userKey;

    private boolean tempUser = false;
}
