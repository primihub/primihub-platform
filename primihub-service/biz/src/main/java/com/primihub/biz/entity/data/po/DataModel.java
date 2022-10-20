package com.primihub.biz.entity.data.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class DataModel {
    /**
     * 模型id
     */
    @JsonIgnore
    private Long modelId;

    /**
     * 模型uuid 同步id
     */
    private String modelUUID;
    /**
     * 模型名称
     */
    private String modelName;
    /**
     * 模型描述
     */
    private String modelDesc;
    /**
     * 模型模板 1.联邦学习ID对齐 2.V-XGBoost 3.V-逻辑回归 4.线性回归
     */
    private int modelType;
    /**
     * 项目id
     */
    private Long projectId;
    /**
     *  资源个数
     */
    private Integer resourceNum;
    /**
     *  y值字段
     */
    private String yValueColumn;
    /**
     * 组件执行进度id
     */
    private String componentSpeed;
    /**
     * 训练类型 0纵向 1横向 默认纵向
     */
    private Integer trainType;
    /**
     * 是否草稿 0是 1不是 默认是
     */
    private Integer isDraft;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 机构id
     */
    private String organId;

    /**
     * 组件json
     */
//    @JsonIgnore
    private String componentJson;

    /**
     * 是否删除
     */
    private Integer isDel;
    /**
     * 创建时间
     */
    private Date createDate;
    /**
     * 修改时间
     */
    private Date updateDate;



}
