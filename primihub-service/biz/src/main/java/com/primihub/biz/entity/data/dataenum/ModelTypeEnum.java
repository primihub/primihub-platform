package com.primihub.biz.entity.data.dataenum;

import java.util.HashMap;
import java.util.Map;

public enum ModelTypeEnum {
    V_XGBOOST(2,"V-XGBoost",1),
    TRANSVERSE_LR(3,"横向LR",1)
    ;
    private Integer type;
    private String name;
    private Integer trainType;
    public static Map<Integer, ModelTypeEnum> MODEL_TYPE_MAP=new HashMap(){
        {
            for (ModelTypeEnum e: ModelTypeEnum.values()){
                put(e.type,e);
            }
        }
    };

    ModelTypeEnum(Integer type, String name,Integer trainType) {
        this.type = type;
        this.name = name;
        this.trainType = trainType;
    }

    public Integer getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Integer getTrainType() {
        return trainType;
    }
}
