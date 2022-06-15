package com.primihub.biz.entity.data.req;

import lombok.Data;

@Data
public class DataModelReq extends PageReq {
    private Long projectId;
    private String modelName;
    private String modelDesc;
    /**
     * 模型模板：1.联邦学习ID对齐 2.V-XGBoost 3.V-逻辑回归 4.线性回归
     */
    private Integer modelType;
    private String yValueColumn;
    private Long resourceId;

}
