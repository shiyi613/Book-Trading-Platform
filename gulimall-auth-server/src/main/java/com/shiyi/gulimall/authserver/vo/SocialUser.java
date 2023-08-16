package com.shiyi.gulimall.authserver.vo;

import lombok.Data;

/**
 * @Author:shiyi
 * @create: 2023-03-07  0:18
 */
@Data
public class SocialUser {

    private String access_token;

    private String token_type;

    private long expires_in;

    private String refresh_token;

    private String scope;

    private Long created_at;

    private Integer uid;

    private String name;

    private String avatarUrl;

}
