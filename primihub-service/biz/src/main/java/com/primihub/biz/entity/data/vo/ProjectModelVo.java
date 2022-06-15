package com.primihub.biz.entity.data.vo;

import lombok.Data;

@Data
public class ProjectModelVo {
    // 模型id
    private Long modelId;
    // 模型名称
    private String modelName;
    // 模型描述
    private String modelDesc;
    // 模型模板 1.联邦学习ID对齐 2.V-XGBoost 3.V-逻辑回归 4.线性回归
    private int modelType;
}
