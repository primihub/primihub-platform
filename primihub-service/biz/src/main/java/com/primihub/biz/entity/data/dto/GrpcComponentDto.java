package com.primihub.biz.entity.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.File;
import java.util.Map;
import java.util.UUID;

@Data
public class GrpcComponentDto {

    public GrpcComponentDto(Map<String, Integer> columns, String resourceId) {
        this.columns = columns;
        this.dataSetId = resourceId;
        this.newDataSetId = resourceId.substring(0, 12) +"-"+ UUID.randomUUID().toString();
    }

    @JsonIgnore
    private Map<String, Integer> columns;
    @JsonIgnore
    private String jointStatisticalType;
    private String newDataSetId;
    private String dataSetId;
    private String outputFilePath;


    public String getOutputFilePath() {
        if (outputFilePath == null){
            return null;
        }
        if (jointStatisticalType!=null){
            return outputFilePath + File.separator + newDataSetId + "-" +this.jointStatisticalType +".csv";
        }
        return outputFilePath + File.separator + newDataSetId+".csv";
    }
}
