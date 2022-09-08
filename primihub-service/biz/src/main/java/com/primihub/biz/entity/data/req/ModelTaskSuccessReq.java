package com.primihub.biz.entity.data.req;

import lombok.Data;

@Data
public class ModelTaskSuccessReq extends PageReq {
    private Long modelId;
    private String modelName;
    private String successDate;
    private Long successTime;
    private Long userId;
}
