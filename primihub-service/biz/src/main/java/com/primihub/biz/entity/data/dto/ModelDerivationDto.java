package com.primihub.biz.entity.data.dto;

import lombok.Data;

@Data
public class ModelDerivationDto {

    public ModelDerivationDto(String resourceId, String type,String derivationType, String newResourceId) {
        this.resourceId = resourceId;
        this.derivationType = derivationType;
        this.type = type;
        this.newResourceId = newResourceId;
    }
    public ModelDerivationDto(String resourceId, String type,String derivationType, String newResourceId,String path) {
        this.resourceId = resourceId;
        this.derivationType = derivationType;
        this.type = type;
        this.newResourceId = newResourceId;
        this.path = path;
    }

    private String resourceId;
    private String type;
    private String derivationType;
    private String newResourceId;
    private String path;
}
