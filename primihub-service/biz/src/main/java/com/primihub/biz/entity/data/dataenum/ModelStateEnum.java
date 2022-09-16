package com.primihub.biz.entity.data.dataenum;

import java.util.HashMap;
import java.util.Map;

public enum ModelStateEnum {
    DRAFT(0,"草稿"),
    SAVE(1,"保存"),
    ;
    private Integer stateType;
    private String stateDesc;
    public static Map<Integer, ModelStateEnum> MODEL_STATE_MAP=new HashMap(){
        {
            for (ModelStateEnum e:ModelStateEnum.values()){
                put(e.stateType,e);
            }
        }
    };

    ModelStateEnum(Integer stateType, String stateDesc) {
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
