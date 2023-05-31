package com.primihub.simple.base;


public enum BaseResultEnum implements ResultEnumType<Integer,String> {
    SUCCESS(0,"请求成功"),
    FAILURE(-1,"请求异常"),
    LACK_OF_PARAM(100,"缺少参数"),
    ;
    private Integer returnCode;
    private String message;

    BaseResultEnum(Integer returnCode, String message) {
        this.returnCode = returnCode;
        this.message = message;
    }

    @Override
    public Integer getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(Integer returnCode) {
        this.returnCode = returnCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
