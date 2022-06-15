package com.primihub.biz.entity.base;

import lombok.Getter;

@Getter
public enum BaseParamEnum {

    TIMESTAMP("timestamp","时间戳",true),
    NONCE("nonce","随机数",true),
    TOKEN("token","登陆码",false),
    SIGN("sign","验签码",false),
    ;

    BaseParamEnum(String columnName, String columnDesc, Boolean isNecessary) {
        this.columnName = columnName;
        this.columnDesc = columnDesc;
        this.isNecessary = isNecessary;
    }

    private String columnName;
    private String columnDesc;
    private Boolean isNecessary;

}
