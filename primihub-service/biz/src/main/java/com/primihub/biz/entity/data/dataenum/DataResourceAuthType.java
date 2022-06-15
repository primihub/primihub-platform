package com.primihub.biz.entity.data.dataenum;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum DataResourceAuthType {
    PUBLIC(1,"公开"),
    PRIVATE(2,"私有"),
    ASSIGN(3,"指定机构"),
    ;
    private Integer authType;
    private String desc;

    public static Map<Integer, DataResourceAuthType> AUTH_TYPE_MAP=new HashMap(){
        {
            for (DataResourceAuthType e:DataResourceAuthType.values()){
                put(e.authType,e);
            }
        }
    };

    DataResourceAuthType(Integer authType, String desc) {
        this.authType = authType;
        this.desc = desc;
    }
}
