package com.primihub.biz.entity.data.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ModelEvaluationTypeDto {
    private BigDecimal meanSquaredError;
    private BigDecimal explainedVariance;
    private BigDecimal meanAbsoluteError;
    private BigDecimal meanSquaredLogError;
    private BigDecimal medianAbsoluteError;
    private BigDecimal r2Score;
    private BigDecimal rootMeanSquaredError;
}
