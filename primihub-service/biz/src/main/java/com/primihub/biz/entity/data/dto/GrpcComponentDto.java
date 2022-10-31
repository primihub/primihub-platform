package com.primihub.biz.entity.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class GrpcComponentDto {

    public GrpcComponentDto(Map<String, Integer> columns, String resourceId) {
        this.columns = columns;
        this.newDataSetId = resourceId.substring(0, 12) +"-"+ UUID.randomUUID().toString();
    }

    @JsonIgnore
    private Map<String, Integer> columns;
    private String newDataSetId;
}
