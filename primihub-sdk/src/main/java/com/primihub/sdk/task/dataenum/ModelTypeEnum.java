package com.primihub.sdk.task.dataenum;

import java.util.HashMap;
import java.util.Map;

public enum ModelTypeEnum {
    V_XGBOOST(2,"taskModel-v_xgboost",0,"hetero_xgb.ftl","hetero_xgb_infer.ftl"),
    TRANSVERSE_LR(3,"HFL_logistic_regression",1,"homo_lr.ftl","homo_lr_infer.ftl"),
    MPC_LR(4,"taskModel-mpc_lr",1,null,null),
    HETERO_LR(5,"VFL_logistic_regression",0,"hetero_lr.ftl","hetero_lr_infer.ftl"),
    CLASSIFICATION_BINARY(6,"taskModel-nn_classification",1,"homo_nn_binary.ftl","homo_nn_binary_infer.ftl"),
    REGRESSION_BINARY(7,"taskModel-nn_regression",1,"homo_nn_binary.ftl","homo_nn_binary_infer.ftl"),
    HFL_LINEAR_REGRESSION(8,"HFL_linear_regression",1,"homo_lr.ftl","homo_lr_infer.ftl"),
    VFL_LINEAR_REGRESSION(9,"VFL_linear_regression",0,"homo_lr.ftl","homo_lr_infer.ftl"),
    ;
    private Integer type;
    private Integer trainType;
    private String typeName;
    private String modelFtlPath;
    private String inferFtlPath;

    public static Map<Integer, ModelTypeEnum> MODEL_TYPE_MAP=new HashMap(){
        {
            for (ModelTypeEnum e: ModelTypeEnum.values()){
                put(e.type,e);
            }
        }
    };

    ModelTypeEnum(Integer type, String typeName, Integer trainType, String modelFtlPath, String inferFtlPath) {
        this.type = type;
        this.typeName = typeName;
        this.trainType = trainType;
        this.modelFtlPath = modelFtlPath;
        this.inferFtlPath = inferFtlPath;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }

    public Integer getTrainType() {
        return trainType;
    }

    public void setTrainType(Integer trainType) {
        this.trainType = trainType;
    }

    public String getModelFtlPath() {
        return modelFtlPath;
    }

    public void setModelFtlPath(String modelFtlPath) {
        this.modelFtlPath = modelFtlPath;
    }

    public String getInferFtlPath() {
        return inferFtlPath;
    }

    public void setInferFtlPath(String inferFtlPath) {
        this.inferFtlPath = inferFtlPath;
    }
}
