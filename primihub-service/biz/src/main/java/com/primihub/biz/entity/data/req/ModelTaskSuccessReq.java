package com.primihub.biz.entity.data.req;

import lombok.Data;

@Data
public class ModelTaskSuccessReq extends PageReq {
    private Long modelId;
    private String modelName;
    private String startDate;
    private String endDate;
    private Long startTime;
    private Long endTime;
    private Long userId;
    private Integer isAdmin = 0;
}
