package com.primihub.entity.resource.enumeration;

import java.util.HashMap;
import java.util.Map;

public enum AuthTypeEnum {
    PUBLIC(1,"公开"),
    PRIVATE(2,"私有"),
    VISIBILITY(3,"可见性"),
            ;
    private Integer authType;
    private String authName;
    public static Map<Integer, AuthTypeEnum> AUTH_TYPE_MAP=new HashMap(){
        {
            for (AuthTypeEnum e:AuthTypeEnum.values()){
                put(e.authType,e);
            }
        }
    };

    AuthTypeEnum(Integer authType, String authName) {
        this.authType = authType;
        this.authName = authName;
    }

    public Integer getAuthType() {
        return authType;
    }

    public String getAuthName() {
        return authName;
    }
}
