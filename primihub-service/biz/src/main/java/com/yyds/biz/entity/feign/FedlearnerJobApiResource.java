package com.yyds.biz.entity.feign;

import lombok.Data;

@Data
public class FedlearnerJobApiResource {
    private Long resourceId;
    private String filePath;

    public FedlearnerJobApiResource(Long resourceId, String filePath) {
        this.resourceId = resourceId;
        this.filePath = filePath;
    }

    public FedlearnerJobApiResource() {
    }
}
