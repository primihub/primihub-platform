package com.primihub.biz.entity.fusion.param;

import com.primihub.biz.entity.data.req.PageReq;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OrganResourceParam extends PageReq {
    private String organId;
    private Long authOrganId;
    private String resourceName;
    private String projectId;
    private Integer auditStatus;
    private String columnStr;
}
