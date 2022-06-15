package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

//@Data
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
    // 最近运行一次任务id
    private Long latestTaskId;
    // 最近一次运行时间
    private Date latestCostTime;
    //最近一次任务状态
    private Integer latestTaskStatus= 0;
    // 数据对齐比例
    private BigDecimal latestAlignmentRatio = new BigDecimal(0);
    // 数据对齐耗时
    private Integer latestAlignmentCost= 0;
    // 统计分析比例
    private BigDecimal latestAnalyzeRatio= new BigDecimal(0);
    // 统计分析耗时
    private Integer latestAnalyzeCost= 0;
    // 特征筛选比例
    private BigDecimal latestFeatureRatio= new BigDecimal(0);
    // 特征筛选耗时
    private Integer latestFeatureCost= 0;
    // 样本抽样设计比例
    private BigDecimal latestSampleRatio= new BigDecimal(0);
    // 样本抽样设计耗时
    private Integer latestSampleCost= 0;
    //训练测试设计比例
    private BigDecimal latestTrainRatio= new BigDecimal(0);
    // 训练测试设计耗时
    private Integer latestTrainCost= 0;
    // 缺失值处理比例
    private BigDecimal latestLackRatio= new BigDecimal(0);
    // 缺失值处理耗时
    private Integer latestLlackCost= 0;
    // 异常值处理比例
    private BigDecimal latestExceptionRatio= new BigDecimal(0);
    // 异常值处理耗时
    private Integer latestExceptionCost = 0;
    /**
     * 创建时间
     */
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createDate;

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelDesc() {
        return modelDesc;
    }

    public void setModelDesc(String modelDesc) {
        this.modelDesc = modelDesc;
    }

    public int getModelType() {
        return modelType;
    }

    public void setModelType(int modelType) {
        this.modelType = modelType;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Integer getResourceNum() {
        return resourceNum;
    }

    public void setResourceNum(Integer resourceNum) {
        this.resourceNum = resourceNum;
    }

    public String getyValueColumn() {
        return yValueColumn;
    }

    public void setyValueColumn(String yValueColumn) {
        this.yValueColumn = yValueColumn;
    }

    public Long getLatestTaskId() {
        return latestTaskId;
    }

    public void setLatestTaskId(Long latestTaskId) {
        this.latestTaskId = latestTaskId;
    }

    public Integer getLatestTaskStatus() {
        return latestTaskStatus;
    }

    public void setLatestTaskStatus(Integer latestTaskStatus) {
        this.latestTaskStatus = latestTaskStatus;
    }

    public BigDecimal getLatestAlignmentRatio() {
        return latestAlignmentRatio;
    }

    public void setLatestAlignmentRatio(BigDecimal latestAlignmentRatio) {
        this.latestAlignmentRatio = latestAlignmentRatio;
    }

    public Integer getLatestAlignmentCost() {
        return latestAlignmentCost;
    }

    public void setLatestAlignmentCost(Integer latestAlignmentCost) {
        this.latestAlignmentCost = latestAlignmentCost;
    }

    public BigDecimal getLatestAnalyzeRatio() {
        return latestAnalyzeRatio;
    }

    public void setLatestAnalyzeRatio(BigDecimal latestAnalyzeRatio) {
        this.latestAnalyzeRatio = latestAnalyzeRatio;
    }

    public Integer getLatestAnalyzeCost() {
        return latestAnalyzeCost;
    }

    public void setLatestAnalyzeCost(Integer latestAnalyzeCost) {
        this.latestAnalyzeCost = latestAnalyzeCost;
    }

    public BigDecimal getLatestFeatureRatio() {
        return latestFeatureRatio;
    }

    public void setLatestFeatureRatio(BigDecimal latestFeatureRatio) {
        this.latestFeatureRatio = latestFeatureRatio;
    }

    public Integer getLatestFeatureCost() {
        return latestFeatureCost;
    }

    public void setLatestFeatureCost(Integer latestFeatureCost) {
        this.latestFeatureCost = latestFeatureCost;
    }

    public BigDecimal getLatestSampleRatio() {
        return latestSampleRatio;
    }

    public void setLatestSampleRatio(BigDecimal latestSampleRatio) {
        this.latestSampleRatio = latestSampleRatio;
    }

    public Integer getLatestSampleCost() {
        return latestSampleCost;
    }

    public void setLatestSampleCost(Integer latestSampleCost) {
        this.latestSampleCost = latestSampleCost;
    }

    public BigDecimal getLatestTrainRatio() {
        return latestTrainRatio;
    }

    public void setLatestTrainRatio(BigDecimal latestTrainRatio) {
        this.latestTrainRatio = latestTrainRatio;
    }

    public Integer getLatestTrainCost() {
        return latestTrainCost;
    }

    public void setLatestTrainCost(Integer latestTrainCost) {
        this.latestTrainCost = latestTrainCost;
    }

    public BigDecimal getLatestLackRatio() {
        return latestLackRatio;
    }

    public void setLatestLackRatio(BigDecimal latestLackRatio) {
        this.latestLackRatio = latestLackRatio;
    }

    public Integer getLatestLlackCost() {
        return latestLlackCost;
    }

    public void setLatestLlackCost(Integer latestLlackCost) {
        this.latestLlackCost = latestLlackCost;
    }

    public BigDecimal getLatestExceptionRatio() {
        return latestExceptionRatio;
    }

    public void setLatestExceptionRatio(BigDecimal latestExceptionRatio) {
        this.latestExceptionRatio = latestExceptionRatio;
    }

    public Integer getLatestExceptionCost() {
        return latestExceptionCost;
    }

    public void setLatestExceptionCost(Integer latestExceptionCost) {
        this.latestExceptionCost = latestExceptionCost;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getLatestCostTime() {
        return latestCostTime;
    }

    public void setLatestCostTime(Date latestCostTime) {
        this.latestCostTime = latestCostTime;
    }

    public Integer getTotalTime(){
        return latestAlignmentCost + latestAnalyzeCost + latestFeatureCost + latestSampleCost + latestTrainCost + latestLlackCost + latestExceptionCost;
    }

    public Integer getIsDraft() {
        return isDraft;
    }

    public void setIsDraft(Integer isDraft) {
        this.isDraft = isDraft;
    }
}
