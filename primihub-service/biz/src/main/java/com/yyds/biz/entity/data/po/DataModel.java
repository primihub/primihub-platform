package com.yyds.biz.entity.data.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class DataModel {
    /**
     * 模型id
     */
    private Long modelId;
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
    private Long organId;

    /**
     * 组件json
     */
    private String componentJson;
    /**
     * 最近运行一次任务id
     */
    private Long latestTaskId;
    /**
     * 最近一次运行时间
     */
    private Date latestCostTime;
    /**
     * 最近一次任务状态
     */
    private Integer latestTaskStatus;
    /**
     * 数据对齐比例
     */
    private BigDecimal latestAlignmentRatio;
    /**
     *  数据对齐耗时
     */
    private Integer latestAlignmentCost;
    /**
     *  统计分析比例
     */
    private BigDecimal latestAnalyzeRatio;
    /**
     * 统计分析耗时
     */
    private Integer latestAnalyzeCost;
    /**
     * 特征筛选比例
     */
    private BigDecimal latestFeatureRatio;
    /**
     * 特征筛选耗时
     */
    private Integer latestFeatureCost;
    /**
     * 样本抽样设计比例
     */
    private BigDecimal latestSampleRatio;
    /**
     * 样本抽样设计耗时
     */
    private Integer latestSampleCost;
    /**
     * 训练测试设计比例
     */
    private BigDecimal latestTrainRatio;
    /**
     * 训练测试设计耗时
     */
    private Integer latestTrainCost;
    /**
     * 缺失值处理比例
     */
    private BigDecimal latestLackRatio;
    /**
     * 缺失值处理耗时
     */
    private Integer latestLackCost;
    /**
     * 异常值处理比例
     */
    private BigDecimal latestExceptionRatio;
    /**
     * 异常值处理耗时
     */
    private Integer latestExceptionCost;
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


    public void dataInit(){
        this.latestTaskId = 0L;
        this.latestTaskStatus = 0;
    }



}
