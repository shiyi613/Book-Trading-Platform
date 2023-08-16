package com.shiyi.common.constant;

import org.omg.CORBA.PRIVATE_MEMBER;

/**
 * @Author:shiyi
 * @create: 2023-02-23  20:38
 */
public class ProductConstant {

    public enum AttrEnum{
        ATTR_TYPE_BASE(1,"基本属性"),
        ATTR_TYPE_SALE(0,"销售属性");

        private int code;
        private String message;

        AttrEnum(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    public enum StatusEnum{

        CREATED_SPU(0,"新建"),
        UP_SPU(1,"商品上架"),
        DOWN_SPU(2,"商品下架");

        private int code;
        private String message;

        StatusEnum(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
