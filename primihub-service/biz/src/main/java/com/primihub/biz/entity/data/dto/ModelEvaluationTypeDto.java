package com.primihub.biz.entity.data.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ModelEvaluationTypeDto {
    private String meanSquaredError;
    private String explainedVariance;
    private String meanAbsoluteError;
    private String meanSquaredLogError;
    private String medianAbsoluteError;
    private String r2Score;
    private String rootMeanSquaredError;
}
