package com.primihub.biz.entity.data.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ModelEvaluationTypeDto {
    private BigDecimal MEAN_SQUARED_ERROR;
    private BigDecimal EXPLAINED_VARIANCE;
    private BigDecimal MEAN_ABSOLUTE_ERROR;
    private BigDecimal MEAN_SQUARED_LOG_ERROR;
    private BigDecimal MEDIAN_ABSOLUTE_ERROR;
    private BigDecimal R2_SCORE;
    private BigDecimal ROOT_MEAN_SQUARED_ERROR;
}
