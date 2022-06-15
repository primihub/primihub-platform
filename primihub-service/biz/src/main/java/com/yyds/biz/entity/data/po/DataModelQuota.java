package com.yyds.biz.entity.data.po;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class DataModelQuota {
    /**
     * 指标id
     */
    private Long quotaId;
    /**
     * 样本类型 1.训练样本集 2.测试样本集
     */
    private Integer quotaType;
    /**
     * 样本集图片
     */
    private String quotaImage;
    /**
     * 模型id
     */
    private Long modelId;
    /**
     * auc
     */
    private BigDecimal auc;
    /**
     * ks
     */
    private BigDecimal ks;
    /**
     * gini
     */
    private BigDecimal gini;
    /**
     * precision
     */
    private BigDecimal precision;
    /**
     * recall
     */
    private BigDecimal recall;
    /**
     * f1_score
     */
    private BigDecimal f1Score;
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
