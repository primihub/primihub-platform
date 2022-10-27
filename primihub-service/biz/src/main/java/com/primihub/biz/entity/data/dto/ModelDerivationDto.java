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

    private String resourceId;
    private String type;
    private String derivationType;
    private String newResourceId;
}
