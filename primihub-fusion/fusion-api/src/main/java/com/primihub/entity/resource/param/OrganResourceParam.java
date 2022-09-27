package com.primihub.entity.resource.param;

import lombok.Data;

@Data
public class OrganResourceParam extends PageParam {
    private String organId;
    private String resourceName;
    private String projectId;
    private Integer auditStatus;
    private String columnStr;
}
