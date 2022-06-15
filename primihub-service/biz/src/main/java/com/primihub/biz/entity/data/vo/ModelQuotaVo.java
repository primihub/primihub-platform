package com.primihub.biz.entity.data.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ModelQuotaVo {
    // 指标id
    private Long quotaId;
    // 样本类型 1.训练样本集 2.测试样本集
    private Integer quotaType;
    // 样本集图片
    private String quotaImage;
    // 模型id
    private Long modelId;
    // auc
    private BigDecimal auc;
    // ks
    private BigDecimal ks;
    // gini
    private BigDecimal gini;
    // precision
    private BigDecimal precision;
    // recall
    private BigDecimal recall;
    // f1_score
    private BigDecimal f1Score;

    public String getQuotaImage() {
        return quotaImage.replace("/data","");
    }
}
