package com.primihub.entity.resource.param;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OrganResourceParam extends PageParam {
    private String organId;
    private String resourceName;
    private String projectId;
    private Integer auditStatus;
    private String columnStr;
}
