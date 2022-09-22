package com.primihub.biz.entity.data.req;

import lombok.Data;

@Data
public class OrganResourceReq extends PageReq {
    private String serverAddress;
    private String organId;
    private String resourceName;
    private String projectId;
    private Integer auditStatus;
}
