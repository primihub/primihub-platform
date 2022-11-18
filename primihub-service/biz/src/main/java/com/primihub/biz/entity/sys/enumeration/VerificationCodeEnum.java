package com.primihub.biz.entity.sys.enumeration;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum VerificationCodeEnum {

    REGISTER(1,"注册"),
    FORGET_PASSWORD(2,"忘记密码"),
    CHANGE_ACCOUNT(3,"变更账号"),
    ;
    private Integer code;
    private String description;

    VerificationCodeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static Map<String, VerificationCodeEnum> COPY_MAP=new HashMap(){
        {
            for (VerificationCodeEnum e:VerificationCodeEnum.values()){
                put(e.code,e);
            }
        }
    };
}
