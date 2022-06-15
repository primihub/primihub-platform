package com.primihub.entity.copy.dto;

import lombok.Data;

@Data
public class DataFusionCopyDto {
    private String tableName;
    private Long maxOffset;
    private String copyPart;
}
