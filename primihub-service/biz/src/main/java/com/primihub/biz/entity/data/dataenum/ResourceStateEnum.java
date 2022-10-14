package com.primihub.biz.entity.data.dataenum;

import java.util.HashMap;
import java.util.Map;


public enum ResourceStateEnum {
    AVAILABLE(0,"上线"),
    NOT_AVAILABLE(1,"下线"),
    ;
    private Integer stateType;
    private String stateDesc;
    public static Map<Integer, ResourceStateEnum> RESOURCE_STATE_MAP=new HashMap(){
        {
            for (ResourceStateEnum e: ResourceStateEnum.values()){
                put(e.stateType,e);
            }
        }
    };

    ResourceStateEnum(Integer stateType, String stateDesc) {
        this.stateType = stateType;
        this.stateDesc = stateDesc;
    }

    public Integer getStateType() {
        return stateType;
    }

    public String getStateDesc() {
        return stateDesc;
    }
}
