package com.shiyi.gulimall.ware.enums;


import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public enum MsgStatusEnum {

    CREATED("新建",0),
    SENT("已发送",1),
    ERROR_REACHED("错误抵达",2),
    SUCCESS_REACHED("正确抵达",3),
    CONSUMED("消费成功",4),
    ARTIFICIAL_PAY("人工补偿",5);


    private String name;
    private Integer code;

    MsgStatusEnum(String name, Integer code) {
        this.name = name;
        this.code = code;
    }
}
