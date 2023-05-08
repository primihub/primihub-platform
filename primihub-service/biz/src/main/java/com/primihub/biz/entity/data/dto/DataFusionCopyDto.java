package com.primihub.biz.entity.data.dto;

import lombok.Data;

@Data
public class DataFusionCopyDto {
    private String tableName;
    private Long maxOffset;
    private String copyPart;
    private String organId;
}
