package com.yyds.biz.entity.data.po;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class DataModelTask {
    /**
     * 任务id
     */
    private Long taskId;
    /**
     * 任务状态
     */
    private Integer taskStatus;
    /**
     * 模型id
     */
    private Long modelId;
    /**
     * 运行时间
     */
    private Date costTime;
    /**
     * 数据对其比例
     */
    private BigDecimal alignmentRatio;
    /**
     * 数据对其耗时
     */
    private Integer alignmentCost;
    /**
     * 统计分析比例
     */
    private BigDecimal analyzeRatio;
    /**
     *
     */
    private Integer analyzeCost;
    /**
     * 特征筛选比例
     */
    private BigDecimal featureRatio;
    /**
     * 特征筛选耗时
     */
    private Integer featureCost;
    /**
     * 样本抽样设计比例
     */
    private BigDecimal sampleRatio;
    /**
     * 样本抽样设计耗时
     */
    private Integer sampleCost;
    /**
     * 训练测试设计比例
     */
    private BigDecimal trainRatio;
    /**
     * 训练测试设计耗时
     */
    private Integer trainCost;
    /**
     * 缺失值处理比例
     */
    private BigDecimal lackRatio;
    /**
     * 缺失值处理耗时
     */
    private Integer lackCost;
    /**
     * 异常值处理比例
     */
    private BigDecimal exceptionRatio;
    /**
     * 异常值处理耗时
     */
    private Integer exceptionCost;
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
