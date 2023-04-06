package com.primihub.biz.entity.data.dataenum;

import com.primihub.biz.constant.DataConstant;

import java.util.HashMap;
import java.util.Map;

public enum ModelTypeEnum {
    V_XGBOOST(2,"V-XGBoost",1, DataConstant.FREEMARKER_PYTHON_EN_PATH),
    TRANSVERSE_LR(3,"横向LR",1,DataConstant.FREEMARKER_PYTHON_HOMO_LR_PATH),
    MPC_LR(4,"MPC-LR",1,null),
    HETERO_LR(5,"HETERO-LR",1,DataConstant.FREEMARKER_PYTHON_HETERO_LR_PATH),
    BINARY(6,"binary",1,DataConstant.FREEMARKER_PYTHON_HOMO_NN_BINARY_PATH),
    BINARY_DPSGD(7,"binary_dpsgd",1,DataConstant.FREEMARKER_PYTHON_HOMO_NN_BINARY_PATH),
    ;
    private Integer type;
    private String name;
    private Integer trainType;
    private String ftlName;
    public static Map<Integer, ModelTypeEnum> MODEL_TYPE_MAP=new HashMap(){
        {
            for (ModelTypeEnum e: ModelTypeEnum.values()){
                put(e.type,e);
            }
        }
    };

    ModelTypeEnum(Integer type, String name,Integer trainType,String ftlName) {
        this.type = type;
        this.name = name;
        this.trainType = trainType;
        this.ftlName = ftlName;
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

    public String getFtlName() {
        return ftlName;
    }
}
