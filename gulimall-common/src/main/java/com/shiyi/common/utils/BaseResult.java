package com.shiyi.common.utils;

public class BaseResult<T> {

    private static final String DEFAULT_ERR_CODE = "Err";
    private static final String DEFAULT_ERR_MSG = "操作失败";
    private static final String DEFAULT_SUCC_MSG = "操作成功";
    private static final String DEFAULT_SUCC_CODE = "0";
    private String msg;
    private String code;
    private boolean state;
    private T data;

    public BaseResult() {
    }

    public static <E> BaseResult<E> buildSuccessfulResult(E data){
        BaseResult<E> baseResult = new BaseResult<>();
        baseResult.setData(data);
        baseResult.setState(true);
        baseResult.setCode(DEFAULT_SUCC_CODE);
        baseResult.setMsg(DEFAULT_SUCC_MSG);
        return baseResult;
    }

    public static BaseResult buildFailedResult(){
        return buildFailedResult(DEFAULT_ERR_CODE,DEFAULT_ERR_MSG);
    }

    public static BaseResult buildFailedResult(String errMsg){
        return buildFailedResult(DEFAULT_ERR_CODE, errMsg);
    }

    public static BaseResult buildFailedResult(String errCode, String errMsg) {
        BaseResult baseResult = new BaseResult();
        baseResult.setCode(errCode);
        baseResult.setMsg(errMsg);
        baseResult.setState(false);
        return baseResult;
    }


    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
