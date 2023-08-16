package com.shiyi.common.constant;

/**
 * @Author:shiyi
 * @create: 2023-02-26  22:32
 */
public class WareConstant {

    public enum  purchaseEnum{
        CREATED(0,"新建"),
        ASSIGNED(1,"已分配"),
        RECEIVED(2,"已领取"),
        FINISHED(3,"已完成"),
        HASERROR(4,"有异常");

        private int code;
        private String message;

        purchaseEnum(int code, String message) {
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

    public enum  purchaseDetailStatusEnum{
        CREATED(0,"新建"),
        ASSIGNED(1,"已分配"),
        BUYING(2,"正在采购"),
        FINISHED(3,"已完成"),
        HASERROR(4,"有异常");

        private int code;
        private String message;

        purchaseDetailStatusEnum(int code, String message) {
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
