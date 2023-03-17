package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ModelVo {
    // 模型id
    private Long modelId;
    // 模型名称
    private String modelName;
    // 模型描述
    private String modelDesc;
    // 模型模板 1.联邦学习ID对齐 2.V-XGBoost 3.V-逻辑回归 4.线性回归
    private int modelType;
    // 项目id
    private Long projectId;
    // 资源个数
    private Integer resourceNum;
    // y值字段
    private String yValueColumn;
    // 是否草稿
    private Integer isDraft;

    private Long totalTime = 0L;
    /**
     * 创建时间
     */
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;

    private Integer isDel;
}
