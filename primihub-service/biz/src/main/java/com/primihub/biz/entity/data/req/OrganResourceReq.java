package com.primihub.biz.entity.data.req;

import lombok.Data;

@Data
public class OrganResourceReq extends PageReq {
    private String organId;
    private String resourceName;
    private String projectId;
    private Integer auditStatus;
    private Long modelId;
    private String columnStr;
}
